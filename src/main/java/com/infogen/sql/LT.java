package com.infogen.sql;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * @author larry
 * @version 创建时间 2017年9月26日 上午11:18:48
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, setterVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE)
public class LT extends Operator {

	private static final long serialVersionUID = 1412101728540937357L;

	public LT(String key, Number value) {
		super();
		this.key = key;
		this.value = value;
	}

	public String key = "";
	private Number value = null;

	public String to_filter() {
		if (key == null || key.trim().isEmpty() || value == null) {
			return " 1 = 1 ";
		}
		return new StringBuilder().append(" ").append(key).append(" < ").append(value).append(" ").toString();
	}

	/**
	 * @return the value
	 */
	public Number getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(Number value) {
		this.value = value;
	}

}
