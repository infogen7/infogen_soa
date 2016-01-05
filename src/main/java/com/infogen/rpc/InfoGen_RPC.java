package com.infogen.rpc;

import com.google.protobuf.BlockingService;
import com.infogen.rpc.server.Server;
import com.infogen.rpc.tracking.InfoGen_Server_Filter;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年8月28日 下午7:33:48
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_RPC {

	private static class InnerInstance {
		public static final InfoGen_RPC instance = new InfoGen_RPC();
	}

	public static InfoGen_RPC getInstance() {
		return InnerInstance.instance;
	}

	private InfoGen_RPC() {
	}

	private Server server;

	public InfoGen_RPC start(Integer rpc_port) throws InterruptedException {
		server = new Server(rpc_port);
		InfoGen_Server_Filter filter = new InfoGen_Server_Filter();
		server.add_filter(filter);
		server.serve();
		return this;
	}

	public InfoGen_RPC shutdown() throws InterruptedException {
		server.shutdown();
		return this;
	}

	public InfoGen_RPC registerService(final BlockingService service) {
		server.registerService(service);
		return this;
	}

}
