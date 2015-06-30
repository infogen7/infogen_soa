package com.infogen;

import java.io.IOException;
import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import com.infogen.http.callback.Http_Callback;
import com.infogen.rpc.callback.RPC_Callback;
import com.infogen.rpc.exception.impl.Node_Notfound_Exception;
import com.infogen.rpc.exception.impl.Service_Notfound_Exception;
import com.infogen.rpc.handler.Thrift_Async_Client_Handler;
import com.infogen.rpc.handler.Thrift_Client_Handler;
import com.infogen.server.cache.InfoGen_Cache_Server;
import com.infogen.server.model.NativeNode;
import com.infogen.server.model.NativeServer;
import com.infogen.server.model.NativeNode.NetType;
import com.infogen.server.model.NativeNode.RequestType;
import com.infogen.util.BasicNameValuePair;
import com.infogen.util.CODE;
import com.infogen.util.Return;

/**
 * 调用服务的封装 实现调度,错误重试,同步异步处理等
 * 
 * @author larry
 * @email larrylv@outlook.com
 * @version 创建时间 2014年10月30日 下午1:48:19
 */
public class Service {
	private static final Logger LOGGER = Logger.getLogger(Service.class.getName());
	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private static final InfoGen instance = InfoGen.getInstance();
	private static ConcurrentMap<String, NativeServer> depend_server = InfoGen_Cache_Server.getInstance().depend_server;
	private String server_name;
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

	// ///////////////////////////////////////////////////NODE/////////////////////////////////////////////////////
	public NativeNode get_node(String seed) {
		NativeServer server = depend_server.get(server_name);
		return server.random_node(seed);
	}

	public void disabled_node(NativeNode node) {
		NativeServer server = depend_server.get(server_name);
		server.disabled(node);
	}

	// //////////////////////////////////////////////////RPC////////////////////////////////////////////////////////////////////////
	public <T> T call(Thrift_Client_Handler<T> handle) throws Service_Notfound_Exception, Node_Notfound_Exception {
		return call(handle, String.valueOf(Clock.systemDefaultZone().millis()));
	}

	public <T> T call(Thrift_Client_Handler<T> handle, String seed) throws Service_Notfound_Exception, Node_Notfound_Exception {
		NativeServer server = depend_server.get(server_name);
		if (server == null) {
			throw new Service_Notfound_Exception();
		}
		NativeNode node = null;
		// 调用出错重试3次
		for (int i = 0; i < 3; i++) {
			try {
				node = server.random_node(seed);
				if (node == null) {
					throw new Node_Notfound_Exception();
				}
				return node.call(handle);
			} catch (TException | IOException e) {
				LOGGER.error("调用失败", e);
				server.disabled(node);
				continue;
			}
		}
		return null;
	}

	public <T> T call_once(Thrift_Client_Handler<T> handle) throws Service_Notfound_Exception, Node_Notfound_Exception {
		return call_once(handle, String.valueOf(Clock.systemDefaultZone().millis()));
	}

	public <T> T call_once(Thrift_Client_Handler<T> handle, String seed) throws Service_Notfound_Exception, Node_Notfound_Exception {
		NativeServer server = depend_server.get(server_name);
		if (server == null) {
			throw new Service_Notfound_Exception();
		}
		NativeNode node = null;
		// 调用出错重试3次
		for (int i = 0; i < 3; i++) {
			try {
				node = server.random_node(seed);
				if (node == null) {
					throw new Node_Notfound_Exception();
				}
				return node.call_once(handle);
			} catch (TException e) {
				LOGGER.error("调用失败", e);
				server.disabled(node);
				continue;
			}
		}
		return null;
	}

	public <T> RPC_Callback<T> call_async(Thrift_Async_Client_Handler<T> handle) throws Service_Notfound_Exception, Node_Notfound_Exception {
		return call_async(handle, String.valueOf(Clock.systemDefaultZone().millis()));
	}

	public <T> RPC_Callback<T> call_async(Thrift_Async_Client_Handler<T> handle, String seed) throws Service_Notfound_Exception, Node_Notfound_Exception {
		NativeServer server = depend_server.get(server_name);
		if (server == null) {
			throw new Service_Notfound_Exception();
		}
		NativeNode node = null;
		// 调用出错重试3次
		for (int i = 0; i < 3; i++) {
			try {
				node = server.random_node(seed);
				if (node == null) {
					throw new Node_Notfound_Exception();
				}
				return node.call_async(handle);
			} catch (TException | IOException e) {
				LOGGER.error("调用失败", e);
				server.disabled(node);
				continue;
			}
		}
		return null;
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
		return http_blocking(url, name_value_pair, RequestType.GET, net_type, String.valueOf(Clock.systemDefaultZone().millis()));
	}

	@Deprecated
	public Return get(String url, List<BasicNameValuePair> name_value_pair) {
		return http_blocking(url, pair_to_map(name_value_pair), RequestType.GET, net_type, String.valueOf(Clock.systemDefaultZone().millis()));
	}

	public Return get(String url, Map<String, String> name_value_pair, String seed) {
		return http_blocking(url, name_value_pair, RequestType.GET, net_type, seed);
	}

	/**
	 * 同步post调用
	 * 
	 * @param url
	 * @param map
	 * @return
	 */
	public Return post(String url, Map<String, String> name_value_pair) {
		return http_blocking(url, name_value_pair, RequestType.POST, net_type, String.valueOf(Clock.systemDefaultZone().millis()));
	}

	@Deprecated
	public Return post(String url, List<BasicNameValuePair> name_value_pair) {
		return http_blocking(url, pair_to_map(name_value_pair), RequestType.POST, net_type, String.valueOf(Clock.systemDefaultZone().millis()));
	}

	public Return post(String url, Map<String, String> name_value_pair, String seed) {
		return http_blocking(url, name_value_pair, RequestType.POST, net_type, seed);
	}

	/**
	 * 异步get调用
	 * 
	 * @param url
	 * @param map
	 * @return
	 */
	public Http_Callback get_async(String url, Map<String, String> name_value_pair) {
		return http_async(url, name_value_pair, RequestType.GET, net_type, String.valueOf(Clock.systemDefaultZone().millis()));
	}

	public Http_Callback get_async(String url, Map<String, String> name_value_pair, String seed) {
		return http_async(url, name_value_pair, RequestType.GET, net_type, seed);
	}

	/**
	 * 异步post调用
	 * 
	 * @param url
	 * @param map
	 * @return
	 */
	public Http_Callback post_async(String url, Map<String, String> name_value_pair) {
		return http_async(url, name_value_pair, RequestType.POST, net_type, String.valueOf(Clock.systemDefaultZone().millis()));
	}

	public Http_Callback post_async(String url, Map<String, String> name_value_pair, String seed) {
		return http_async(url, name_value_pair, RequestType.POST, net_type, seed);
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
	private Return http_blocking(String method, Map<String, String> name_value_pair, RequestType request_type, NetType net_type, String seed) {
		NativeServer server = depend_server.get(server_name);
		if (server == null) {
			return Return.FAIL(CODE.service_notfound);
		}
		NativeNode node = null;
		// 调用出错重试3次
		for (int i = 0; i < 3; i++) {
			try {
				node = server.random_node(seed);
				if (node == null) {
					return Return.FAIL(CODE.node_notfound);
				}
				String http = node.http(method, name_value_pair, request_type, net_type);
				return Return.create(http);
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
	 */
	private Http_Callback http_async(String method, Map<String, String> name_value_pair, RequestType request_type, NetType net_type, String seed) {
		Http_Callback callback = new Http_Callback();

		NativeServer server = depend_server.get(server_name);
		if (server == null) {
			callback.add(Return.FAIL(CODE.service_notfound).toJson());
			return callback;
		}
		NativeNode node = null;
		// 调用出错重试3次
		for (int i = 0; i < 3; i++) {
			node = server.random_node(seed);
			if (node == null) {
				callback.add(Return.FAIL(CODE.node_notfound).toJson());
				return callback;
			}
			try {
				return node.http_async(method, name_value_pair, request_type, net_type);
			} catch (IOException e) {
				LOGGER.error("调用失败", e);
				server.disabled(node);
				continue;
			}
		}
		callback.add(Return.FAIL(CODE.error).toJson());
		return callback;
	}

}
