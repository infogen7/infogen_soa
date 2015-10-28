package com.infogen;

import java.io.IOException;
import java.time.Clock;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.infogen.configuration.InfoGen_Configuration;
import com.infogen.core.json.Return;
import com.infogen.core.util.CODE;
import com.infogen.exception.HTTP_Fail_Exception;
import com.infogen.exception.Node_Unavailable_Exception;
import com.infogen.exception.Service_Notfound_Exception;
import com.infogen.http.InfoGen_HTTP;
import com.infogen.http.callback.HTTP_Callback;
import com.infogen.server.model.RemoteNode;
import com.infogen.server.model.RemoteServer;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

/**
 * http协议下远程服务的映射,实现调度,错误重试,同步异步处理等
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年7月22日 下午5:28:57
 * @since 1.0
 * @version 1.0
 */
public class RemoteHTTPFunction {
	private static final Logger LOGGER = LogManager.getLogger(Service.class.getName());
	private Service service;
	private NetType net_type = NetType.LOCAL;
	private String method;
	private String seed;

	public RemoteHTTPFunction(Service service, String method) {
		this.service = service;
		if (method.startsWith("/")) {
			method = method.substring(1);
		}
		this.method = method;
	}

	public RemoteHTTPFunction(Service service, String method, String seed) {
		this(service, method);
		this.seed = seed;
	}

	public RemoteHTTPFunction(Service service, String method, NetType net_type) {
		this(service, method);
		this.net_type = net_type;
	}

	public RemoteHTTPFunction(Service service, String method, NetType net_type, String seed) {
		this(service, method);
		this.net_type = net_type;
		this.seed = seed;
	}

	//////////////////////////////////////////// GET/////////////////////////////////////////
	public Return get(Map<String, String> name_value_pair) {
		return http_blocking(name_value_pair, RequestType.GET, seed);
	}

	public HTTP_Callback<Return> get_async(Map<String, String> name_value_pair) {
		return http_async_callback(name_value_pair, RequestType.GET, seed);
	}

	public void get_async(Map<String, String> name_value_pair, Callback callback) throws Service_Notfound_Exception, Node_Unavailable_Exception {
		http_async(name_value_pair, RequestType.GET, callback, seed);
	}

	//////////////////////////////////////////// POST/////////////////////////////////////////
	public Return post(Map<String, String> name_value_pair) {
		return http_blocking(name_value_pair, RequestType.POST, seed);
	}

	public HTTP_Callback<Return> post_async(Map<String, String> name_value_pair) {
		return http_async_callback(name_value_pair, RequestType.POST, seed);
	}

	public void post_async(Map<String, String> name_value_pair, Callback callback) throws Service_Notfound_Exception, Node_Unavailable_Exception {
		http_async(name_value_pair, RequestType.POST, callback, seed);
	}

	//////////////////////////////////////////// POST JSON/////////////////////////////////////////
	public Return post_json(Map<String, String> name_value_pair) {
		return http_blocking(name_value_pair, RequestType.POST_JSON, seed);
	}

	public HTTP_Callback<Return> post_json_async(Map<String, String> name_value_pair) {
		return http_async_callback(name_value_pair, RequestType.POST_JSON, seed);
	}

	public void post_json_async(Map<String, String> name_value_pair, Callback callback) throws Service_Notfound_Exception, Node_Unavailable_Exception {
		http_async(name_value_pair, RequestType.POST_JSON, callback, seed);
	}

	//////////////////////////////////////////// POST FORM DATA///////////////////////////////////////////
	public void post_form_data_async(Map<String, List<String>> name_value_pair, Callback callback) throws Service_Notfound_Exception, Node_Unavailable_Exception {
		IdentityHashMap<String, String> identityhashmap = new IdentityHashMap<>();
		name_value_pair.forEach((key, values) -> {
			values.forEach(value -> {
				identityhashmap.put(new String(key), value);
			});
		});
		http_async(identityhashmap, RequestType.POST_FORM_DATA, callback, seed);
	}

	public void post_form_data_async(IdentityHashMap<String, String> name_value_pair, Callback callback) throws Service_Notfound_Exception, Node_Unavailable_Exception {
		http_async(name_value_pair, RequestType.POST_FORM_DATA, callback, seed);
	}

	////////////////////////////////////////////////// HTTP LOAD BALANCING//////////////////////////
	private static String RETURN_KEY_SERVICE = "service";

	/**
	 * 同步http调用
	 * 
	 * @param method
	 * @param name_value_pair
	 * @param request_type
	 * @param net_type
	 * @return
	 */
	public Return http_blocking(Map<String, String> name_value_pair, RequestType request_type, String seed) {
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
				String http = http(node, name_value_pair, request_type);
				Return create = Return.create(http);
				if (create.get_code() == CODE.limit.code) {
					LOGGER.info(new StringBuilder("接口调用超过限制:").append(method).toString());
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

	public HTTP_Callback<Return> http_async_callback(Map<String, String> name_value_pair, RequestType request_type, String seed) {
		HTTP_Callback<Return> callback = new HTTP_Callback<>();
		try {
			http_async(name_value_pair, request_type, new Callback() {
				@Override
				public void onFailure(Request request, IOException e) {
					callback.run(Return.FAIL(CODE.error).add(RETURN_KEY_SERVICE, service.get_server()));
					LOGGER.error("do_async_post_bytype 报错:".concat(request.urlString()), e);
				}

				@Override
				public void onResponse(Response response) throws IOException {
					if (response.isSuccessful()) {
						callback.run(Return.create(response.body().string()));
					} else {
						callback.run(Return.FAIL(response.code(), response.message()).add(RETURN_KEY_SERVICE, service.get_server()));
						LOGGER.error("do_async_post_bytype 错误-返回非2xx:".concat(response.request().urlString()));
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

	/**
	 * 异步http调用
	 * 
	 * @param method
	 * @param name_value_pair
	 * @param request_type
	 * @param net_type
	 * @return
	 * @throws Service_Notfound_Exception
	 * @throws Node_Notfound_Exception
	 */
	public void http_async(Map<String, String> name_value_pair, RequestType request_type, Callback callback, String seed) throws Service_Notfound_Exception, Node_Unavailable_Exception {
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
				http_async(node, name_value_pair, request_type, callback);
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

	// ///////////////////////////////////////////http////////////////////////////////////////////////
	public enum RequestType {
		POST, GET, POST_JSON, POST_FORM_DATA
	}

	public enum NetType {
		NET, LOCAL
	}

	public String http(RemoteNode node, Map<String, String> name_value_pair, RequestType request_type) throws IOException {
		String url;
		if (net_type == NetType.LOCAL) {
			url = new StringBuilder().append(node.getHttp_protocol()).append("://").append(node.getIp()).append(":").append(node.getHttp_port()).append("/").append(method).toString();
		} else {
			url = new StringBuilder().append(node.getHttp_protocol()).append("://").append(node.getNet_ip()).append(":").append(node.getHttp_port()).append("/").append(method).toString();
		}
		if (request_type == RequestType.GET) {
			LOGGER.debug(new StringBuilder("get -> ").append(url).toString());
			return InfoGen_HTTP.do_get(url, name_value_pair);
		} else if (request_type == RequestType.POST) {
			LOGGER.debug(new StringBuilder("post -> ").append(url).toString());
			return InfoGen_HTTP.do_post(url, name_value_pair);
		} else {
			LOGGER.debug(new StringBuilder("post json -> ").append(url).toString());
			return InfoGen_HTTP.do_post_json(url, name_value_pair);
		}
	}

	// 通过异步callback取得返回值,不阻塞返回值
	public void http_async(RemoteNode node, Map<String, String> name_value_pair, RequestType request_type, Callback callback) throws IOException {
		String url;
		if (net_type == NetType.LOCAL) {
			url = new StringBuilder().append(node.getHttp_protocol()).append("://").append(node.getIp()).append(":").append(node.getHttp_port()).append("/").append(method).toString();
		} else {
			url = new StringBuilder().append(node.getHttp_protocol()).append("://").append(node.getNet_ip()).append(":").append(node.getHttp_port()).append("/").append(method).toString();
		}
		if (request_type == RequestType.POST) {
			LOGGER.debug(new StringBuilder("post async -> ").append(url).toString());
			InfoGen_HTTP.do_post_async(url, name_value_pair, callback);
		} else if (request_type == RequestType.POST_JSON) {
			LOGGER.debug(new StringBuilder("post json async -> ").append(url).toString());
			InfoGen_HTTP.do_post_json_async(url, name_value_pair, callback);
		} else if (request_type == RequestType.POST_FORM_DATA) {
			LOGGER.debug(new StringBuilder("post form data async -> ").append(url).toString());
			InfoGen_HTTP.do_post_form_data_async(url, name_value_pair, callback);
		} else {
			LOGGER.debug(new StringBuilder("get async -> ").append(url).toString());
			InfoGen_HTTP.do_get_async(url, name_value_pair, callback);
		}
	}

}
