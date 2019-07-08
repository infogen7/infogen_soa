package com.infogen;

import java.util.concurrent.ConcurrentMap;

import com.infogen.http_client.HTTP_LoadBalancing;
import com.infogen.rpc_client.LoadBalancingRPCChannel;
import com.infogen.server.InfoGen_Server_Management;
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
	private static ConcurrentMap<String, RemoteServer> depend_server = InfoGen_Server_Management.getInstance().depend_server;
	private String server_name;

	public Service(String server_name) {
		this.server_name = server_name;
		InfoGen.init_server(server_name, (native_server) -> {
		});
	}

	///////////////////////////////////////////////// Server///////////////////////////////////////////////////////
	public RemoteServer get_server() {
		return depend_server.get(server_name);
	}

	// /////////////////////////////////////////////////HTTPFunction/////////////////////////////////////////////

	public HTTP_LoadBalancing get_loadbalancing_http_channel() {
		return new HTTP_LoadBalancing(this);
	}

	////////////////////////////////////////////////// RPC///////////////////////////////////////////////////////////
	public LoadBalancingRPCChannel get_loadbalancing_rpc_channel() {
		return new LoadBalancingRPCChannel(this);
	}

}
