package com.infogen.authc.exception;

import java.io.IOException;

/**
 * 认证相关的异常
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年5月11日 上午11:43:15
 * @since 1.0
 * @version 1.0
 */
public abstract class InfoGen_Auth_Exception extends IOException {
	private static final long serialVersionUID = -8959567347914450591L;

	public abstract Integer code();

	public abstract String name();

	public abstract String note();
}
