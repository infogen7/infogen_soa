package com.infogen.sql;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * @author larry
 * @version 创建时间 2017年9月26日 上午10:33:28
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, setterVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE)
public class Empty extends Operator {
	private static final long serialVersionUID = 5733165376281048683L;

	public Empty() {
		super();
	}

	public String to_filter() {
		return " 1 = 1 ";
	}

}
