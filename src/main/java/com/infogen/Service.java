package com.infogen;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

import com.infogen.rpc.RemoteRPCChannel;
import com.infogen.server.cache.InfoGen_Cache_Server;
import com.infogen.server.model.RemoteNode;
import com.infogen.server.model.RemoteServer;

/**
 * 
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年7月29日 下午5:34:17
 * @since 1.0
 * @version 1.0
 */
public abstract class Service {

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private static final InfoGen instance = InfoGen.getInstance();
	private static ConcurrentMap<String, RemoteServer> depend_server = InfoGen_Cache_Server.getInstance().depend_server;
	private String server_name;

	public static Service create(String server_name) {
		return new Service(server_name){};
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

	////////////////////////////////////////////////// RPC///////////////////////////////////////////////////////////
	public RemoteRPCChannel get_rpc_channel() {
		return new RemoteRPCChannel(this);
	}
}
