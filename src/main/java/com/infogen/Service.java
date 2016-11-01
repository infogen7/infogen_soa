package com.infogen;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

import com.infogen.rpc_client.LoadBalancingRPCChannel;
import com.infogen.server.management.InfoGen_Server_Management;
import com.infogen.server.model.RemoteNode;
import com.infogen.server.model.RemoteServer;

/**
 * 对远端服务的抽象，有对节点和远端方法的操作
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年7月29日 下午5:34:17
 * @since 1.0
 * @version 1.0
 */
public class Service {

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private static final InfoGen instance = InfoGen.getInstance();
	private static ConcurrentMap<String, RemoteServer> depend_server = InfoGen_Server_Management.getInstance().depend_server;
	private String server_name;

	public static Service create(String server_name) {
		return new Service(server_name) {
		};
	}

	public Service(String server_name) {
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
	public RemoteHTTPFunction get_http_function(String function) {
		return new RemoteHTTPFunction(this, function);
	}

	////////////////////////////////////////////////// RPC///////////////////////////////////////////////////////////
	public LoadBalancingRPCChannel get_loadbalancing_rpc_channel() {
		return new LoadBalancingRPCChannel(this);
	}

}
