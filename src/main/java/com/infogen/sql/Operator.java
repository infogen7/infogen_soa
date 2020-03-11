package com.infogen.sql;

import java.io.Serializable;

/**
 * @author larry
 * @version 创建时间 2017年9月26日 上午11:23:03
 */
public abstract class Operator implements Serializable {
	private static final long serialVersionUID = -7735467654296659101L;

	public abstract String sql();
}
