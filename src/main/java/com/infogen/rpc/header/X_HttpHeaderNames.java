package com.infogen.rpc.header;

import io.netty.util.AsciiString;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年8月31日 上午10:33:59
 * @since 1.0
 * @version 1.0
 */
public enum X_HttpHeaderNames {
	x_sequence("x-infogen-seq", "sequence"); //
	public AsciiString key;
	public String note;

	private X_HttpHeaderNames(String key, String note) {
		this.key = new AsciiString(key);
		this.note = note;
	}
}
