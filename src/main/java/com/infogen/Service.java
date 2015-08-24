package com.infogen;

import java.io.IOException;
import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;

import com.infogen.exception.Node_Notfound_Exception;
import com.infogen.exception.Service_Notfound_Exception;
import com.infogen.http.exception.HTTP_Fail_Exception;
import com.infogen.server.cache.InfoGen_Cache_Server;
import com.infogen.server.model.RemoteNode;
import com.infogen.server.model.RemoteNode.NetType;
import com.infogen.server.model.RemoteNode.RequestType;
import com.infogen.server.model.RemoteServer;
import com.infogen.util.BasicNameValuePair;
import com.infogen.util.CODE;
import com.infogen.util.Return;
import com.squareup.okhttp.Callback;

/**
 * http协议下远程服务的映射,实现调度,错误重试,同步异步处理等
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年7月29日 下午5:34:17
 * @since 1.0
 * @version 1.0
 */
public class Service {
	private static final Logger LOGGER = Logger.getLogger(Service.class.getName());
	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private static final InfoGen instance = InfoGen.getInstance();
	private static ConcurrentMap<String, RemoteServer> depend_server = InfoGen_Cache_Server.getInstance().depend_server;
	private String server_name;
	private String node_version;
	private NetType net_type = NetType.LOCAL;

	public static Service create(String server_name) {
		return new Service(server_name);
	}

	public static Service create(String server_name, NetType net_type) {
		return new Service(server_name, net_type);
	}

	public Service(String server_name) {
		this.server_name = server_name;
		// 初始化
		instance.get_server(server_name);
	}
	
	
	public Service(String server_name,String node_version) {
		this.server_name = server_name;
		this.node_version = node_version;
		// 初始化
		//instance.get_server(server_name);
		instance.get_server(server_name,node_version);
	}

	/**
	 * 指定网络类型是内网还是外网
	 * 
	 * @param server_name
	 * @param net_type
	 */
	public Service(String server_name, NetType net_type) {
		this.net_type = net_type;
		this.server_name = server_name;
		// 初始化
		instance.get_server(server_name);
	}
	
	public Service(String server_name,NetType net_type,String node_version) {
		this.server_name = server_name;
		this.net_type = net_type;
		this.node_version = node_version;
		// 初始化
		//instance.get_server(server_name);
		instance.get_server(server_name,node_version);
	}

	// /////////////////////////////////////////////////HTTPFunction/////////////////////////////////////////////
	public RemoteHTTPFunction get_http_function(String url) {
		return new RemoteHTTPFunction(this, url);
	}

	// ///////////////////////////////////////////////////NODE/////////////////////////////////////////////////////
	public RemoteNode get_node(String seed) {
		RemoteServer server = depend_server.get(server_name);
		return server.random_node(seed);
	}

	public RemoteNode get_node_byip(String ip) {
		RemoteServer server = depend_server.get(server_name);
		List<RemoteNode> available_nodes = server.getAvailable_nodes();
		for (RemoteNode nativeNode : available_nodes) {
			if (nativeNode.getIp().equals(ip)) {
				return nativeNode;
			}
		}
		return null;
	}

	public void disabled_node(RemoteNode node) {
		RemoteServer server = depend_server.get(server_name);
		server.disabled(node);
	}

	// //////////////////////////////////////////////////HTTP///////////////////////////////////////////////////////////////////////
	private Map<String, String> pair_to_map(List<BasicNameValuePair> name_value_pair) {
		Map<String, String> map = new HashMap<>();
		name_value_pair.forEach(pair -> {
			map.put(pair.getName(), pair.getValue());
		});
		return map;
	}

	/**
	 * 同步get调用
	 * 
	 * @param url
	 * @param map
	 * @return
	 */
	public Return get(String url, Map<String, String> name_value_pair) {
		return http_blocking(url, name_value_pair, RequestType.GET, String.valueOf(Clock.systemDefaultZone().millis()));
	}

	@Deprecated
	public Return get(String url, List<BasicNameValuePair> name_value_pair) {
		return http_blocking(url, pair_to_map(name_value_pair), RequestType.GET, String.valueOf(Clock.systemDefaultZone().millis()));
	}

	public Return get(String url, Map<String, String> name_value_pair, String seed) {
		return http_blocking(url, name_value_pair, RequestType.GET, seed);
	}

	/**
	 * 同步post调用
	 * 
	 * @param url
	 * @param map
	 * @return
	 */
	public Return post(String url, Map<String, String> name_value_pair) {
		return http_blocking(url, name_value_pair, RequestType.POST, String.valueOf(Clock.systemDefaultZone().millis()));
	}

	@Deprecated
	public Return post(String url, List<BasicNameValuePair> name_value_pair) {
		return http_blocking(url, pair_to_map(name_value_pair), RequestType.POST, String.valueOf(Clock.systemDefaultZone().millis()));
	}

	public Return post(String url, Map<String, String> name_value_pair, String seed) {
		return http_blocking(url, name_value_pair, RequestType.POST, seed);
	}

	/**
	 * 异步get调用
	 * 
	 * @param url
	 * @param map
	 * @return
	 * @throws Node_Notfound_Exception
	 * @throws Service_Notfound_Exception
	 */
	public void get_async(String url, Map<String, String> name_value_pair, Callback callback) throws Service_Notfound_Exception, Node_Notfound_Exception {
		http_async(url, name_value_pair, RequestType.GET, callback, String.valueOf(Clock.systemDefaultZone().millis()));
	}

	public void get_async(String url, Map<String, String> name_value_pair, Callback callback, String seed) throws Service_Notfound_Exception, Node_Notfound_Exception {
		http_async(url, name_value_pair, RequestType.GET, callback, seed);
	}

	/**
	 * 异步post调用
	 * 
	 * @param url
	 * @param map
	 * @return
	 * @throws Node_Notfound_Exception
	 * @throws Service_Notfound_Exception
	 */
	public void post_async(String url, Map<String, String> name_value_pair, Callback callback) throws Service_Notfound_Exception, Node_Notfound_Exception {
		http_async(url, name_value_pair, RequestType.POST, callback, String.valueOf(Clock.systemDefaultZone().millis()));
	}

	public void post_async(String url, Map<String, String> name_value_pair, Callback callback, String seed) throws Service_Notfound_Exception, Node_Notfound_Exception {
		http_async(url, name_value_pair, RequestType.POST, callback, seed);
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
	private Return http_blocking(String method, Map<String, String> name_value_pair, RequestType request_type, String seed) {
		RemoteServer server = depend_server.get(server_name);
		if (server == null) {
			LOGGER.error(CODE.service_notfound.note);
			return Return.FAIL(CODE.service_notfound);
		}
		if (method.startsWith("/")) {
			method = method.substring(1);
		}
		RemoteNode node = null;
		// 调用出错重试3次
		for (int i = 0; i < 3; i++) {
			try {
				node = server.random_node(seed);
				if (node == null) {
					LOGGER.error(CODE.node_notfound.note);
					return Return.FAIL(CODE.node_notfound);
				}
				LOGGER.debug(new StringBuilder(node.getIp()).append("-->").append(method).toString());
				String http = node.http(method, name_value_pair, request_type, net_type);
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
	private void http_async(String method, Map<String, String> name_value_pair, RequestType request_type, Callback callback, String seed) throws Service_Notfound_Exception, Node_Notfound_Exception {
		RemoteServer server = depend_server.get(server_name);
		if (server == null) {
			LOGGER.error(CODE.service_notfound.note);
			throw new Service_Notfound_Exception();
		}
		if (method.startsWith("/")) {
			method = method.substring(1);
		}
		RemoteNode node = null;
		// 调用出错重试3次
		for (int i = 0; i < 3; i++) {
			node = server.random_node(seed);
			if (node == null) {
				LOGGER.error(CODE.node_notfound.note);
				throw new Node_Notfound_Exception();
			}
			try {
				node.http_async(method, name_value_pair, request_type, net_type, callback);
			} catch (IOException e) {
				LOGGER.error("调用失败", e);
				server.disabled(node);
				continue;
			}
		}
	}

}
