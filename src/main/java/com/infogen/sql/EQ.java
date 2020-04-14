package com.infogen.sql;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * @author larry
 * @version 创建时间 2017年9月26日 上午10:33:28
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, setterVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE)
public class EQ extends Operator {
	private static final long serialVersionUID = 757844916520032852L;

	public EQ(String key, String value, Boolean is_string_value) {
		super();
		this.key = key;
		this.value = value;
		this.is_string_value = is_string_value;
	}

	public EQ(String key, Number value) {
		super();
		this.key = key;
		this.value = value.toString();
	}

	public EQ(String key, Boolean value) {
		super();
		this.key = key;
		this.value = value.toString();
	}

	public String key = "";
	private String value = "";
	private Boolean is_string_value = false;

	public String sql() {
		if (key == null || key.trim().isEmpty() || value == null || value.trim().isEmpty()) {
			return " 1 = 1 ";
		}

		StringBuilder string_builder = new StringBuilder();
		string_builder.append(" ");
		string_builder.append(key);
		string_builder.append(" = ");
		if (is_string_value) {
			string_builder.append("'");
			string_builder.append(value);
			string_builder.append("'");
		} else {
			string_builder.append(value);
		}
		string_builder.append(" ");
		return string_builder.toString();
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key
	 *            the key to set
	 */
	public void setKey(String key) {
		this.key = key;
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
