/**
 * 
 */
package com.infogen;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.infogen.aop.annotation.Invoke;
import com.infogen.cache.InfoGen_Cache_Server;
import com.infogen.http.callback.Http_Callback;
import com.infogen.rpc.callback.RPC_Callback;
import com.infogen.server.model.NativeNode;
import com.infogen.server.model.NativeNode.NetType;
import com.infogen.server.model.NativeNode.RequestType;
import com.infogen.server.model.NativeServer;
import com.infogen.thrift.Response;
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
	public static final Logger logger = Logger.getLogger(Service.class.getName());
	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private static InfoGen instance = InfoGen.getInstance();
	private String server_name;
	private ConcurrentHashMap<String, NativeServer> depend_server = InfoGen_Cache_Server.getInstance().depend_server;
	private NetType net_type = NetType.LOCAL;

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

	// //////////////////////////////////////////////////RPC///////////////////////////////////////////////////////////////////////
	/**
	 * 根据错误码生成一个Response 用于rpc调用
	 * 
	 * @param code
	 * @return
	 */
	private Response fail_response(CODE code) {
		Response call = new Response();
		call.success = false;
		call.code = code.code;
		call.note = code.note;
		return call;
	}

	/**
	 * 同步rpc调用
	 * 
	 * @param method
	 * @param map
	 * @return
	 */
	@Deprecated
	@Invoke
	public Response call(String method, Map<String, String> map) {
		return blocking_rpc(method, map);
	}

	/**
	 * 异步rpc调用
	 * 
	 * @param method
	 * @param map
	 * @return
	 */
	@Deprecated
	@Invoke
	public RPC_Callback async_call(String method, Map<String, String> map) {
		return async_rpc(method, map);
	}

	/**
	 * 同步RPC调用,支持错误重试,及随机调度
	 * 
	 * @param method
	 * @param map
	 * @return
	 */
	@Deprecated
	private Response blocking_rpc(String method, Map<String, String> map) {
		NativeServer server = depend_server.get(server_name);
		if (server == null) {
			return fail_response(CODE._402);
		}
		NativeNode node = null;
		// 调用出错重试3次
		for (int i = 0; i < 3; i++) {
			try {
				node = server.random_node();
				if (node == null) {
					return fail_response(CODE._403);
				}
				return node.call("", method, map);
			} catch (Exception e) {
				server.disabled(node);
				logger.error("调用失败", e);
				continue;
			}
		}
		return fail_response(CODE._500);
	}

	/**
	 * 异步rpc调用,支持错误重试,及随机调度
	 * 
	 * @param method
	 * @param map
	 * @return
	 */
	@Deprecated
	private RPC_Callback async_rpc(String method, Map<String, String> map) {
		RPC_Callback callback = new RPC_Callback();
		NativeServer server = depend_server.get(server_name);
		if (server == null) {
			callback.add(fail_response(CODE._402));
			return callback;
		}
		NativeNode node = null;
		// 调用出错重试3次
		for (int i = 0; i < 3; i++) {
			try {
				node = server.random_node();
				if (node == null) {
					callback.add(fail_response(CODE._403));
					return callback;
				}
				return node.async_call("", method, map);
			} catch (Exception e) {
				server.disabled(node);
				logger.error("调用失败", e);
				continue;
			}
		}
		callback.add(fail_response(CODE._500));
		return callback;
	}

	// //////////////////////////////////////////////////HTTP///////////////////////////////////////////////////////////////////////

	/**
	 * 同步get调用
	 * 
	 * @param url
	 * @param map
	 * @return
	 */
	@Invoke
	public Return get(String url, Map<String, String> name_value_pair) {
		return blocking_http(url, name_value_pair, RequestType.GET, net_type);
	}

	/**
	 * 同步post调用
	 * 
	 * @param url
	 * @param map
	 * @return
	 */
	@Invoke
	public Return post(String url, Map<String, String> name_value_pair) {
		return blocking_http(url, name_value_pair, RequestType.POST, net_type);
	}

	/**
	 * 异步get调用
	 * 
	 * @param url
	 * @param map
	 * @return
	 */
	@Invoke
	public Http_Callback async_get(String url, Map<String, String> name_value_pair) {
		return async_http(url, name_value_pair, RequestType.GET, net_type);
	}

	/**
	 * 异步post调用
	 * 
	 * @param url
	 * @param map
	 * @return
	 */
	@Invoke
	public Http_Callback async_post(String url, Map<String, String> name_value_pair) {
		return async_http(url, name_value_pair, RequestType.POST, net_type);
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
	private Return blocking_http(String method, Map<String, String> name_value_pair, RequestType request_type, NetType net_type) {
		NativeServer server = depend_server.get(server_name);
		if (server == null) {
			return Return.FAIL(CODE._402.code, CODE._402.note);
		}
		NativeNode node = null;
		// 调用出错重试3次
		for (int i = 0; i < 3; i++) {
			try {
				node = server.random_node();
				if (node == null) {
					return Return.FAIL(CODE._403.code, CODE._403.note);
				}
				return Return.create(node.http(method, name_value_pair, request_type, net_type));
			} catch (IOException e) {
				logger.error("调用失败", e);
				server.disabled(node);
				continue;
			}
		}
		return Return.FAIL(CODE._500.code, CODE._500.note);
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
	private Http_Callback async_http(String method, Map<String, String> name_value_pair, RequestType request_type, NetType net_type) {
		Http_Callback callback = new Http_Callback();

		NativeServer server = depend_server.get(server_name);
		if (server == null) {
			callback.add(Return.FAIL(CODE._402.code, CODE._402.note).toJson());
			return callback;
		}
		NativeNode node = null;
		// 调用出错重试3次
		for (int i = 0; i < 3; i++) {
			node = server.random_node();
			if (node == null) {
				callback.add(Return.FAIL(CODE._403.code, CODE._403.note).toJson());
				return callback;
			}
			try {
				return node.async_http(method, name_value_pair, request_type, net_type);
			} catch (IOException e) {
				logger.error("调用失败", e);
				server.disabled(node);
				continue;
			}
		}
		callback.add(Return.FAIL(CODE._500.code, CODE._500.note).toJson());
		return callback;
	}

}
