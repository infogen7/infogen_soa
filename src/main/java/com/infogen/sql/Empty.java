package com.infogen.sql;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * @author larry
 * @version 创建时间 2017年9月26日 上午10:33:28
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, setterVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE)
public class Empty extends Operator {
	private static final long serialVersionUID = 5733165376281048683L;

	private Boolean pass = true;

	public Empty(Boolean pass) {
		super();
		this.pass = pass;
	}

	public String sql() {
		if (pass) {
			return " 1 = 1 ";
		} else {
			return " 1 != 1 ";
		}
	}

}
