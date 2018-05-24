package com.infogen;

import java.util.Map;

import com.infogen.http_client.HTTP_LoadBalancing;
import com.infogen.http_client.callback.HTTP_Callback;
import com.infogen.http_idl.Return;

/**
 * http协议下远程服务的映射,实现调度,错误重试,同步异步处理等
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年7月22日 下午5:28:57
 * @since 1.0
 * @version 1.0
 */
public class RemoteHTTPFunction {
	private HTTP_LoadBalancing http_loadbalancing;

	private String function;
	private String seed;

	public enum RequestType {
		POST, GET, POST_JSON, POST_XML
	}

	public enum NetType {
		NET, LOCAL
	}

	public RemoteHTTPFunction(Service service, String function) {
		this.http_loadbalancing = new HTTP_LoadBalancing(service);
		if (function.startsWith("/")) {
			function = function.substring(1);
		}
		this.function = function;
	}

	public RemoteHTTPFunction(Service service, String method, String seed) {
		this(service, method);
		this.seed = seed;
	}

	//////////////////////////////////////////// GET/////////////////////////////////////////
	public Return get(Map<String, Object> name_value_pair) {
		return http_loadbalancing.http_blocking(function, name_value_pair, RequestType.GET, seed);
	}

	public HTTP_Callback<Return> get_async(Map<String, Object> name_value_pair) {
		return http_loadbalancing.http_async(function, name_value_pair, RequestType.GET, seed);
	}

	//////////////////////////////////////////// POST/////////////////////////////////////////
	public Return post(Map<String, Object> name_value_pair) {
		return http_loadbalancing.http_blocking(function, name_value_pair, RequestType.POST, seed);
	}

	public HTTP_Callback<Return> post_async(Map<String, Object> name_value_pair) {
		return http_loadbalancing.http_async(function, name_value_pair, RequestType.POST, seed);
	}

	//////////////////////////////////////////// POST JSON/////////////////////////////////////////
	public Return post_json(Map<String, Object> name_value_pair) {
		return http_loadbalancing.http_blocking(function, name_value_pair, RequestType.POST_JSON, seed);
	}

	public HTTP_Callback<Return> post_json_async(Map<String, Object> name_value_pair) {
		return http_loadbalancing.http_async(function, name_value_pair, RequestType.POST_JSON, seed);
	}

	//////////////////////////////////////////// POST FORM DATA///////////////////////////////////////////
	public Return post_xml(Map<String, Object> name_value_pair) {
		return http_loadbalancing.http_blocking(function, name_value_pair, RequestType.POST_XML, seed);
	}

	public HTTP_Callback<Return> post_xml_async(Map<String, Object> name_value_pair) {
		return http_loadbalancing.http_async(function, name_value_pair, RequestType.POST_XML, seed);
	}
}
