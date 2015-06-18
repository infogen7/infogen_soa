package com.infogen.rpc.exception;

/**
 * 认证失败的异常
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年5月11日 上午11:43:15
 * @since 1.0
 * @version 1.0
 */
public abstract class InfoGen_RPC_Exception extends Exception {
	private static final long serialVersionUID = -6462362814822951685L;

	public abstract Integer code();

	public abstract String name();

	public abstract String note();
}
