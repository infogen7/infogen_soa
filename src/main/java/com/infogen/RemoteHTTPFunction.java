package com.infogen;

import java.util.IdentityHashMap;
import java.util.Map;

import com.infogen.core.json.Return;
import com.infogen.exception.Node_Unavailable_Exception;
import com.infogen.exception.Service_Notfound_Exception;
import com.infogen.http_client.HTTP_LoadBalancing;
import com.infogen.http_client.callback.HTTP_Callback;

import okhttp3.Callback;

/**
 * http协议下远程服务的映射,实现调度,错误重试,同步异步处理等
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年7月22日 下午5:28:57
 * @since 1.0
 * @version 1.0
 */
public class RemoteHTTPFunction {
	private HTTP_LoadBalancing http_loadbalancing = new HTTP_LoadBalancing();
	private Service service;
	private String function;
	private String seed;
	private NetType net_type = NetType.LOCAL;

	public enum RequestType {
		POST, GET, POST_JSON, POST_FORM_DATA
	}

	public enum NetType {
		NET, LOCAL
	}

	public RemoteHTTPFunction(Service service, String method) {
		this.service = service;
		if (method.startsWith("/")) {
			method = method.substring(1);
		}
		this.function = method;
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
		return http_loadbalancing.http_blocking(service, function, name_value_pair, net_type, RequestType.GET, seed);
	}

	public HTTP_Callback<Return> get_async(Map<String, String> name_value_pair) {
		return http_loadbalancing.http_async(service, function, name_value_pair, net_type, RequestType.GET, seed);
	}

	public void get_async(Map<String, String> name_value_pair, Callback callback) throws Service_Notfound_Exception, Node_Unavailable_Exception {
		http_loadbalancing.http_async(service, function, name_value_pair, net_type, RequestType.GET, callback, seed);
	}

	//////////////////////////////////////////// POST/////////////////////////////////////////
	public Return post(Map<String, String> name_value_pair) {
		return http_loadbalancing.http_blocking(service, function, name_value_pair, net_type, RequestType.POST, seed);
	}

	public HTTP_Callback<Return> post_async(Map<String, String> name_value_pair) {
		return http_loadbalancing.http_async(service, function, name_value_pair, net_type, RequestType.POST, seed);
	}

	public void post_async(Map<String, String> name_value_pair, Callback callback) throws Service_Notfound_Exception, Node_Unavailable_Exception {
		http_loadbalancing.http_async(service, function, name_value_pair, net_type, RequestType.POST, callback, seed);
	}

	//////////////////////////////////////////// POST JSON/////////////////////////////////////////
	public Return post_json(Map<String, String> name_value_pair) {
		return http_loadbalancing.http_blocking(service, function, name_value_pair, net_type, RequestType.POST_JSON, seed);
	}

	public HTTP_Callback<Return> post_json_async(Map<String, String> name_value_pair) {
		return http_loadbalancing.http_async(service, function, name_value_pair, net_type, RequestType.POST_JSON, seed);
	}

	public void post_json_async(Map<String, String> name_value_pair, Callback callback) throws Service_Notfound_Exception, Node_Unavailable_Exception {
		http_loadbalancing.http_async(service, function, name_value_pair, net_type, RequestType.POST_JSON, callback, seed);
	}

	//////////////////////////////////////////// POST FORM DATA///////////////////////////////////////////
	public Return post_form_data(Map<String, String> name_value_pair) {
		return http_loadbalancing.http_blocking(service, function, name_value_pair, net_type, RequestType.POST_FORM_DATA, seed);
	}

	public HTTP_Callback<Return> post_form_data_async(Map<String, String> name_value_pair) {
		return http_loadbalancing.http_async(service, function, name_value_pair, net_type, RequestType.POST_FORM_DATA, seed);
	}

	public void post_form_data_async(IdentityHashMap<String, String> name_value_pair, Callback callback) throws Service_Notfound_Exception, Node_Unavailable_Exception {
		http_loadbalancing.http_async(service, function, name_value_pair, net_type, RequestType.POST_FORM_DATA, callback, seed);
	}
}
