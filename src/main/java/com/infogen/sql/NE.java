package com.infogen.sql;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * @author larry
 * @version 创建时间 2017年9月26日 上午10:33:28
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, setterVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE)
public class NE extends Operator {
	private static final long serialVersionUID = 2020502685563180302L;

	public NE(String key, String value) {
		super();
		this.key = key;
		this.value = value;
	}

	public String key = "";
	private String value = "";

	public String to_filter() {
		if (key == null || key.trim().isEmpty() || value == null || value.trim().isEmpty()) {
			return " 1 = 1 ";
		}
		StringBuilder string_builder = new StringBuilder();
		string_builder.append(" ").append(key).append(" != ");
		string_builder.append("'");
		string_builder.append(value);
		string_builder.append("'");
		string_builder.append(" ");
		return string_builder.toString();
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
}
