package com.infogen.core.structure;

import java.io.Serializable;

/**
 * 自定义的string类型的KV键值对
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年6月10日 下午6:20:03
 * @since 1.0
 * @version 1.0
 */
public class BasicNameValuePair implements Serializable {
	private static final long serialVersionUID = -5729160568013465624L;
	private final String name;
	private final String value;

	/**
	 * Default Constructor taking a name and a value. The value may be null.
	 *
	 * @param name
	 *            The name.
	 * @param value
	 *            The value.
	 */
	public BasicNameValuePair(final String name, final String value) {
		super();
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return this.name;
	}

	public String getValue() {
		return this.value;
	}

	@Override
	public String toString() {
		// don't call complex default formatting for a simple toString

		if (this.value == null) {
			return name;
		}
		final int len = this.name.length() + 1 + this.value.length();
		final StringBuilder buffer = new StringBuilder(len);
		buffer.append(this.name);
		buffer.append("=");
		buffer.append(this.value);
		return buffer.toString();
	}

}
