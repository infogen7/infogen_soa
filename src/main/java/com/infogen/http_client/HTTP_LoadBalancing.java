package com.infogen.http_client;

import java.io.IOException;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.infogen.InfoGen_CODE;
import com.infogen.Service;
import com.infogen.configuration.InfoGen_Configuration;
import com.infogen.http_client.callback.HTTP_Callback;
import com.infogen.http_client.exception.HTTP_Fail_Exception;
import com.infogen.http_idl.Return;
import com.infogen.json.Jackson;
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
	private static final Logger LOGGER = LogManager.getLogger(HTTP_LoadBalancing.class.getName());

	public enum RequestType {
		POST, GET, POST_JSON, POST_XML
	}

	private Service service;

	public HTTP_LoadBalancing(Service service) {
		this.service = service;
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////// HTTP LOAD BALANCING//////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////

	// 同步http调用
	public Return http_blocking(String function, Map<String, Object> name_value_pair, RequestType request_type, String seed) {
		function = function.startsWith("/") ? function.substring(1) : function;
		seed = seed == null ? String.valueOf(Clock.system(InfoGen_Configuration.zoneid).millis()) : seed;

		RemoteServer server = service.get_server();
		if (server == null) {
			LOGGER.error(InfoGen_CODE.service_notfound.message);
			return Return.create(InfoGen_CODE.service_notfound.code, InfoGen_CODE.service_notfound.message).put("service", service.get_server());
		}

		RemoteNode node = null;
		// 调用出错重试3次
		for (int i = 0; i < 3; i++) {
			try {
				node = server.random_node(seed);
				if (node == null) {
					LOGGER.error(InfoGen_CODE.node_unavailable.message);
					return Return.create(InfoGen_CODE.node_unavailable.code, InfoGen_CODE.node_unavailable.message).put("service", service.get_server());
				}
				
				String http = do_http(node, function, name_value_pair, request_type);
				return Return.create(http);
			} catch (HTTP_Fail_Exception e) {
				LOGGER.warn("调用失败", e);
				return Return.create(e.getCode(), e.getMessage()).put("service", service.get_server());
			} catch (IOException e) {
				LOGGER.error("调用失败", e);
				server.disabled(node);
				continue;
			}
		}
		
		return Return.create(InfoGen_CODE.error.code, InfoGen_CODE.error.message).put("service", service.get_server());
	}

	// 异步http调用
	public HTTP_Callback<Return> http_async(String function, Map<String, Object> name_value_pair, RequestType request_type, String seed) {
		function = function.startsWith("/") ? function.substring(1) : function;
		seed = seed == null ? String.valueOf(Clock.system(InfoGen_Configuration.zoneid).millis()) : seed;

		HTTP_Callback<Return> callback = new HTTP_Callback<>();

		RemoteServer server = service.get_server();
		if (server == null) {
			LOGGER.error(InfoGen_CODE.service_notfound.message);
			callback.run(Return.create(InfoGen_CODE.service_notfound.code, InfoGen_CODE.service_notfound.message).put("service", service.get_server()));
			return callback;
		}

		RemoteNode node = null;
		// 调用出错重试3次
		for (int i = 0; i < 3; i++) {
			node = server.random_node(seed);
			if (node == null) {
				LOGGER.error(InfoGen_CODE.node_unavailable.message);
				callback.run(Return.create(InfoGen_CODE.node_unavailable.code, InfoGen_CODE.node_unavailable.message).put("service", service.get_server()));
				return callback;
			}
			
			try {
				do_http_async(node, function, name_value_pair, request_type, new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {
						Request request = call.request();
						callback.run(Return.create(InfoGen_CODE.error.code, InfoGen_CODE.error.message).put("service", service.get_server()));
						LOGGER.error("do_async_post_bytype 报错:".concat(request.url().toString()), e);
					}

					@Override
					public void onResponse(Call call, Response response) throws IOException {
						if (response.isSuccessful()) {
							callback.run(Return.create(response.body().string()));
						} else {
							callback.run(Return.create(response.code(), response.message()).put("service", service.get_server()));
							LOGGER.error("do_async_post_bytype 错误-返回非2xx:".concat(response.request().url().toString()));
						}
					}
				});
				return callback;
			} catch (IOException e) {
				LOGGER.error("调用失败", e);
				server.disabled(node);
				continue;
			}
		}

		callback.run(Return.create(InfoGen_CODE.error.code, InfoGen_CODE.error.message).put("service", service.get_server()));
		return callback;
	}

	// ///////////////////////////////////////////http////////////////////////////////////////////////

	private String do_http(RemoteNode node, String function, Map<String, Object> name_value_pair, RequestType request_type) throws IOException, HTTP_Fail_Exception {
		String url = new StringBuilder().append("http://").append(node.getIp()).append(":").append(node.getHttp_port()).append("/").append(function).toString();
		if (request_type == RequestType.POST) {
			LOGGER.debug(new StringBuilder("post -> ").append(url).toString());
			return InfoGen_HTTP.do_post(url, name_value_pair, new HashMap<>());
		} else if (request_type == RequestType.POST_JSON) {
			LOGGER.debug(new StringBuilder("post json -> ").append(url).toString());
			return InfoGen_HTTP.do_post_json(url, Jackson.toJson(name_value_pair), new HashMap<>());
		} else if (request_type == RequestType.POST_XML) {
			LOGGER.debug(new StringBuilder("post xml-> ").append(url).toString());
			return InfoGen_HTTP.do_post_xml(url, Jackson.toJson(name_value_pair), new HashMap<>());
		} else {
			LOGGER.debug(new StringBuilder("get -> ").append(url).toString());
			return InfoGen_HTTP.do_get(url, name_value_pair, new HashMap<>());
		}
	}

	// 通过异步callback取得返回值,不阻塞返回值
	private void do_http_async(RemoteNode node, String function, Map<String, Object> name_value_pair, RequestType request_type, Callback callback) throws IOException {
		String url = new StringBuilder().append("http://").append(node.getIp()).append(":").append(node.getHttp_port()).append("/").append(function).toString();
		if (request_type == RequestType.POST) {
			LOGGER.debug(new StringBuilder("post async -> ").append(url).toString());
			InfoGen_HTTP.do_post_async(url, name_value_pair, callback, new HashMap<>());
		} else if (request_type == RequestType.POST_JSON) {
			LOGGER.debug(new StringBuilder("post json async -> ").append(url).toString());
			InfoGen_HTTP.do_post_json_async(url, Jackson.toJson(name_value_pair), callback, new HashMap<>());
		} else if (request_type == RequestType.POST_XML) {
			LOGGER.debug(new StringBuilder("post xml async -> ").append(url).toString());
			InfoGen_HTTP.do_post_xml_async(url, Jackson.toJson(name_value_pair), callback, new HashMap<>());
		} else {
			LOGGER.debug(new StringBuilder("get async -> ").append(url).toString());
			InfoGen_HTTP.do_get_async(url, name_value_pair, callback, new HashMap<>());
		}
	}

}
