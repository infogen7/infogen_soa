package com.infogen.sql;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * @author larry
 * @version 创建时间 2017年9月26日 上午11:18:48
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, setterVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE)
public class GT extends Operator {
	private static final long serialVersionUID = -6016383539821071462L;

	public GT(String key, Number value) {
		super();
		this.key = key;
		this.value = value.toString();
	}

	public GT(String key, String value) {
		super();
		this.key = key;
		this.value = value;
	}

	public String key = "";
	private String value = null;

	public String sql() {
		if (key == null || key.trim().isEmpty() || value == null) {
			return " 1 = 1 ";
		}

		StringBuilder string_builder = new StringBuilder();
		string_builder.append(" ");
		string_builder.append(key);
		string_builder.append(" > ");
		string_builder.append(value);
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
