package com.infogen.authc.exception;

import java.io.IOException;

/**
 * 认证失败的异常
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年5月11日 上午11:43:15
 * @since 1.0
 * @version 1.0
 */
public abstract class InfoGen_Auth_Exception extends IOException {
	private static final long serialVersionUID = -8959567347914450591L;

	public enum Auth_Exception_CODE {
		authentication_fail(5001, "认证失败"), //
		roles_fail(5002, "授权失败"), //
		session_expiration(5005, "Session 过期"), //
		session_lose(5006, "Session 丢失"), //
		end(9999, "");
		public String note;
		public Integer code;

		private Auth_Exception_CODE(Integer code, String note) {
			this.note = note;
			this.code = code;
		}
	}

	public abstract Integer code();

	public abstract String name();

	public abstract String note();
}
