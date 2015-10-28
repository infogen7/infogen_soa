package com.infogen;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import com.infogen.core.json.Return;
import com.infogen.exception.Node_Unavailable_Exception;
import com.infogen.exception.Service_Notfound_Exception;
import com.infogen.http.callback.HTTP_Callback;
import com.infogen.rpc.RemoteRPCChannel;
import com.infogen.server.cache.InfoGen_Cache_Server;
import com.infogen.server.model.RemoteNode;
import com.infogen.server.model.RemoteServer;
import com.squareup.okhttp.Callback;

/**
 * 
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年7月29日 下午5:34:17
 * @since 1.0
 * @version 1.0
 */
public class Service {

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private static final InfoGen instance = InfoGen.getInstance();
	private static ConcurrentMap<String, RemoteServer> depend_server = InfoGen_Cache_Server.getInstance().depend_server;
	private String server_name;

	public static Service create(String server_name) {
		return new Service(server_name) {
		};
	}

	private Service(String server_name) {
		this.server_name = server_name;
		// 初始化
		instance.get_server(server_name);
	}

	///////////////////////////////////////////////// Server///////////////////////////////////////////////////////
	public String getServer_name() {
		return server_name;
	}

	public RemoteServer get_server() {
		return depend_server.get(server_name);
	}

	// ///////////////////////////////////////////////////NODE/////////////////////////////////////////////////////
	public RemoteNode get_node(String seed) {
		RemoteServer server = depend_server.get(server_name);
		return server.random_node(seed);
	}

	public List<RemoteNode> get_available_nodes(String ip) {
		RemoteServer server = depend_server.get(server_name);
		return server.getAvailable_nodes();
	}

	public void disabled_node(RemoteNode node) {
		RemoteServer server = depend_server.get(server_name);
		server.disabled(node);
	}

	// /////////////////////////////////////////////////HTTPFunction/////////////////////////////////////////////
	public RemoteHTTPFunction get_http_function(String method) {
		return new RemoteHTTPFunction(this, method);
	}

	public RemoteHTTPFunction get_http_function(String method, RemoteHTTPFunction.NetType net_type) {
		return new RemoteHTTPFunction(this, method, net_type);
	}

	////////////////////////////////////////// GET
	public Return get(String function, Map<String, String> name_value_pair) {
		return new RemoteHTTPFunction(this, function).get(name_value_pair);
	}

	public HTTP_Callback<Return> get_async(String function, Map<String, String> name_value_pair) {
		return new RemoteHTTPFunction(this, function).get_async(name_value_pair);
	}

	public void get_async(String function, Map<String, String> name_value_pair, Callback callback) throws Service_Notfound_Exception, Node_Unavailable_Exception {
		new RemoteHTTPFunction(this, function).get_async(name_value_pair, callback);
	}

	//////////////////////////////////////////// POST
	public Return post(String function, Map<String, String> name_value_pair) {
		return new RemoteHTTPFunction(this, function).post(name_value_pair);

	}

	public HTTP_Callback<Return> post_async(String function, Map<String, String> name_value_pair) {
		return new RemoteHTTPFunction(this, function).post_async(name_value_pair);
	}

	public void post_async(String function, Map<String, String> name_value_pair, Callback callback) throws Service_Notfound_Exception, Node_Unavailable_Exception {
		new RemoteHTTPFunction(this, function).post_async(name_value_pair, callback);
	}

	//////////////////////////////////////////// POST JSON
	public Return post_json(String function, Map<String, String> name_value_pair) {
		return new RemoteHTTPFunction(this, function).post_json(name_value_pair);
	}

	public HTTP_Callback<Return> post_json_async(String function, Map<String, String> name_value_pair) {
		return new RemoteHTTPFunction(this, function).post_json_async(name_value_pair);
	}

	public void post_json_async(String function, Map<String, String> name_value_pair, Callback callback) throws Service_Notfound_Exception, Node_Unavailable_Exception {
		new RemoteHTTPFunction(this, function).post_json_async(name_value_pair, callback);
	}

	//////////////////////////////////////////// POST FORM DATA
	public void post_form_data_async(String function, IdentityHashMap<String, String> name_value_pair, Callback callback) throws Service_Notfound_Exception, Node_Unavailable_Exception {
		new RemoteHTTPFunction(this, function).post_form_data_async(name_value_pair, callback);
	}

	public void post_form_data_async(String function, Map<String, List<String>> name_value_pair, Callback callback) throws Service_Notfound_Exception, Node_Unavailable_Exception {
		new RemoteHTTPFunction(this, function).post_form_data_async(name_value_pair, callback);
	}

	////////////////////////////////////////////////// RPC///////////////////////////////////////////////////////////
	public RemoteRPCChannel get_rpc_channel() {
		return new RemoteRPCChannel(this);
	}

}
