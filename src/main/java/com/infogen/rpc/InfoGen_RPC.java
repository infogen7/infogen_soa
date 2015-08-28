package com.infogen.rpc;

import org.apache.log4j.Logger;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年8月28日 下午7:33:48
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_RPC {
	private static final Logger LOGGER = Logger.getLogger(InfoGen_RPC.class.getName());

	private static class InnerInstance {
		public static final InfoGen_RPC instance = new InfoGen_RPC();
	}

	public static InfoGen_RPC getInstance() {
		return InnerInstance.instance;
	}

	private InfoGen_RPC() {
	}

}
