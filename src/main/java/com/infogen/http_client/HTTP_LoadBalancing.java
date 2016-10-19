package com.infogen.http_client;

import java.io.IOException;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.infogen.RemoteHTTPFunction.NetType;
import com.infogen.RemoteHTTPFunction.RequestType;
import com.infogen.Service;
import com.infogen.configuration.InfoGen_Configuration;
import com.infogen.core.json.Return;
import com.infogen.core.util.CODE;
import com.infogen.exception.Node_Unavailable_Exception;
import com.infogen.exception.Service_Notfound_Exception;
import com.infogen.http_client.callback.HTTP_Callback;
import com.infogen.http_client.exception.HTTP_Fail_Exception;
import com.infogen.server.model.RemoteNode;
import com.infogen.server.model.RemoteServer;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年12月30日 下午3:21:38
 * @since 1.0
 * @version 1.0
 */
public class HTTP_LoadBalancing {
	private static final Logger LOGGER = LogManager.getLogger(Service.class.getName());

	//////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////// HTTP LOAD BALANCING//////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////
	private static String RETURN_KEY_SERVICE = "service";

	// 同步http调用
	public Return http_blocking(Service service, String function, Map<String, String> name_value_pair, NetType net_type, RequestType request_type, String seed) {
		RemoteServer server = service.get_server();
		if (server == null) {
			LOGGER.error(CODE.service_notfound.note);
			return Return.FAIL(CODE.service_notfound).add(RETURN_KEY_SERVICE, service.get_server());
		}

		RemoteNode node = null;
		if (seed == null) {
			seed = String.valueOf(Clock.system(InfoGen_Configuration.zoneid).millis());
		}
		// 调用出错重试3次
		for (int i = 0; i < 3; i++) {
			try {
				node = server.random_node(seed);
				if (node == null) {
					LOGGER.error(CODE.node_unavailable.note);
					return Return.FAIL(CODE.node_unavailable).add(RETURN_KEY_SERVICE, service.get_server());
				}
				String http = do_http(node, function, name_value_pair, net_type, request_type);
				Return create = Return.create(http);
				if (create.get_code() == CODE.limit.code) {
					LOGGER.info(new StringBuilder("接口调用超过限制:").append(function).toString());
					continue;
				}
				return create;
			} catch (HTTP_Fail_Exception e) {
				LOGGER.warn("调用失败", e);
				return Return.FAIL(e.getCode(), e.getMessage()).add(RETURN_KEY_SERVICE, service.get_server());
			} catch (IOException e) {
				LOGGER.error("调用失败", e);
				server.disabled(node);
				continue;
			}
		}
		return Return.FAIL(CODE.error).add(RETURN_KEY_SERVICE, service.get_server());
	}

	// 异步http调用
	public void http_async(Service service, String function, Map<String, String> name_value_pair, NetType net_type, RequestType request_type, Callback callback, String seed) throws Service_Notfound_Exception, Node_Unavailable_Exception {
		RemoteServer server = service.get_server();
		if (server == null) {
			LOGGER.error(CODE.service_notfound.note);
			throw new Service_Notfound_Exception();
		}
		RemoteNode node = null;
		if (seed == null) {
			seed = String.valueOf(Clock.system(InfoGen_Configuration.zoneid).millis());
		}
		// 调用出错重试3次
		for (int i = 0; i < 3; i++) {
			node = server.random_node(seed);
			if (node == null) {
				LOGGER.error(CODE.node_unavailable.note);
				throw new Node_Unavailable_Exception();
			}
			try {
				do_http_async(node, function, name_value_pair, net_type, request_type, callback);
				return;
			} catch (IOException e) {
				LOGGER.error("调用失败", e);
				server.disabled(node);
				continue;
			}
		}
		LOGGER.error(CODE.node_unavailable.note);
		throw new Node_Unavailable_Exception();
	}

	public HTTP_Callback<Return> http_async(Service service, String function, Map<String, String> name_value_pair, NetType net_type, RequestType request_type, String seed) {
		HTTP_Callback<Return> callback = new HTTP_Callback<>();
		try {
			http_async(service, function, name_value_pair, net_type, request_type, new Callback() {
				@Override
				public void onFailure(Call call, IOException e) {
					Request request = call.request();
					callback.run(Return.FAIL(CODE.error).add(RETURN_KEY_SERVICE, service.get_server()));
					LOGGER.error("do_async_post_bytype 报错:".concat(request.url().toString()), e);
				}

				@Override
				public void onResponse(Call call, Response response) throws IOException {
					if (response.isSuccessful()) {
						callback.run(Return.create(response.body().string()));
					} else {
						callback.run(Return.FAIL(response.code(), response.message()).add(RETURN_KEY_SERVICE, service.get_server()));
						LOGGER.error("do_async_post_bytype 错误-返回非2xx:".concat(response.request().url().toString()));
					}
				}
			}, seed);
		} catch (Service_Notfound_Exception e) {
			callback.run(Return.FAIL(e.code(), e.note()).add(RETURN_KEY_SERVICE, service.get_server()));
		} catch (Node_Unavailable_Exception e) {
			callback.run(Return.FAIL(e.code(), e.note()).add(RETURN_KEY_SERVICE, service.get_server()));
		}
		return callback;
	}
	// ///////////////////////////////////////////http////////////////////////////////////////////////

	private String do_http(RemoteNode node, String function, Map<String, String> name_value_pair, NetType net_type, RequestType request_type) throws IOException, HTTP_Fail_Exception {
		String url;
		if (net_type == NetType.LOCAL) {
			url = new StringBuilder().append(node.getHttp_protocol()).append("://").append(node.getIp()).append(":").append(node.getHttp_port()).append("/").append(function).toString();
		} else {
			url = new StringBuilder().append(node.getHttp_protocol()).append("://").append(node.getNet_ip()).append(":").append(node.getHttp_port()).append("/").append(function).toString();
		}
		if (request_type == RequestType.POST) {
			LOGGER.debug(new StringBuilder("post -> ").append(url).toString());
			return InfoGen_HTTP.do_post(url, name_value_pair, new HashMap<>());
		} else if (request_type == RequestType.POST_JSON) {
			LOGGER.debug(new StringBuilder("post json -> ").append(url).toString());
			return InfoGen_HTTP.do_post_json(url, name_value_pair, new HashMap<>());
		} else if (request_type == RequestType.POST_FORM_DATA) {
			LOGGER.debug(new StringBuilder("post form data-> ").append(url).toString());
			return InfoGen_HTTP.do_post_form_data(url, name_value_pair, new HashMap<>());
		} else {
			LOGGER.debug(new StringBuilder("get -> ").append(url).toString());
			return InfoGen_HTTP.do_get(url, name_value_pair, new HashMap<>());
		}
	}

	// 通过异步callback取得返回值,不阻塞返回值
	private void do_http_async(RemoteNode node, String function, Map<String, String> name_value_pair, NetType net_type, RequestType request_type, Callback callback) throws IOException {
		String url;
		if (net_type == NetType.LOCAL) {
			url = new StringBuilder().append(node.getHttp_protocol()).append("://").append(node.getIp()).append(":").append(node.getHttp_port()).append("/").append(function).toString();
		} else {
			url = new StringBuilder().append(node.getHttp_protocol()).append("://").append(node.getNet_ip()).append(":").append(node.getHttp_port()).append("/").append(function).toString();
		}
		if (request_type == RequestType.POST) {
			LOGGER.debug(new StringBuilder("post async -> ").append(url).toString());
			InfoGen_HTTP.do_post_async(url, name_value_pair, callback, new HashMap<>());
		} else if (request_type == RequestType.POST_JSON) {
			LOGGER.debug(new StringBuilder("post json async -> ").append(url).toString());
			InfoGen_HTTP.do_post_json_async(url, name_value_pair, callback, new HashMap<>());
		} else if (request_type == RequestType.POST_FORM_DATA) {
			LOGGER.debug(new StringBuilder("post form data async -> ").append(url).toString());
			InfoGen_HTTP.do_post_form_data_async(url, name_value_pair, callback, new HashMap<>());
		} else {
			LOGGER.debug(new StringBuilder("get async -> ").append(url).toString());
			InfoGen_HTTP.do_get_async(url, name_value_pair, callback, new HashMap<>());
		}
	}

}
