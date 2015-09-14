package com.infogen;

import java.io.IOException;
import java.time.Clock;
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
import com.infogen.server.model.RemoteNode;
import com.infogen.server.model.RemoteServer;
import com.squareup.okhttp.Callback;

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

	/////////////////////////////////////////////////////////////////////////////////////
	public Return get(Map<String, String> name_value_pair) {
		return http_blocking(name_value_pair, RequestType.GET, seed);
	}

	public void get_async(Map<String, String> name_value_pair, Callback callback) throws Service_Notfound_Exception, Node_Unavailable_Exception {
		http_async(name_value_pair, RequestType.GET, callback, seed);
	}

	public Return post(Map<String, String> name_value_pair) {
		return http_blocking(name_value_pair, RequestType.POST, seed);
	}

	public void post_async(Map<String, String> name_value_pair, Callback callback) throws Service_Notfound_Exception, Node_Unavailable_Exception {
		http_async(name_value_pair, RequestType.POST, callback, seed);
	}

	public Return post_json(Map<String, String> name_value_pair) {
		return http_blocking(name_value_pair, RequestType.POST_JSON, seed);
	}

	public void post_json_async(Map<String, String> name_value_pair, Callback callback) throws Service_Notfound_Exception, Node_Unavailable_Exception {
		http_async(name_value_pair, RequestType.POST_JSON, callback, seed);
	}

	/**
	 * 同步http调用
	 * 
	 * @param method
	 * @param name_value_pair
	 * @param request_type
	 * @param net_type
	 * @return
	 */
	private Return http_blocking(Map<String, String> name_value_pair, RequestType request_type, String seed) {
		RemoteServer server = service.get_server();
		if (server == null) {
			LOGGER.error(CODE.service_notfound.note);
			return Return.FAIL(CODE.service_notfound);
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
					return Return.FAIL(CODE.node_unavailable);
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
				return Return.FAIL(e.getCode(), e.getMessage());
			} catch (IOException e) {
				LOGGER.error("调用失败", e);
				server.disabled(node);
				continue;
			}
		}
		return Return.FAIL(CODE.error);
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
	private void http_async(Map<String, String> name_value_pair, RequestType request_type, Callback callback, String seed) throws Service_Notfound_Exception, Node_Unavailable_Exception {
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
		POST, GET, POST_JSON
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

	public void http_async(RemoteNode node, Map<String, String> name_value_pair, RequestType request_type, Callback callback) throws IOException {
		String url;
		if (net_type == NetType.LOCAL) {
			url = new StringBuilder().append(node.getHttp_protocol()).append("://").append(node.getIp()).append(":").append(node.getHttp_port()).append("/").append(method).toString();
		} else {
			url = new StringBuilder().append(node.getHttp_protocol()).append("://").append(node.getNet_ip()).append(":").append(node.getHttp_port()).append("/").append(method).toString();
		}
		if (request_type == RequestType.GET) {
			LOGGER.debug(new StringBuilder("get async -> ").append(url).toString());
			InfoGen_HTTP.do_get_async(url, name_value_pair, callback);
		} else if (request_type == RequestType.POST) {
			LOGGER.debug(new StringBuilder("post async -> ").append(url).toString());
			InfoGen_HTTP.do_post_async(url, name_value_pair, callback);
		} else {
			LOGGER.debug(new StringBuilder("post json async -> ").append(url).toString());
			InfoGen_HTTP.do_post_json_async(url, name_value_pair, callback);
		}
	}
}
