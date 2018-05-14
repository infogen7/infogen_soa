package com.infogen.sql;

import java.io.Serializable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.infogen.core.json.Jackson;

/**
 * @author larry
 * @version 创建时间 2017年9月26日 上午9:54:26
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, setterVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE)
public class Where implements Serializable {
	private static final long serialVersionUID = 956926869188029141L;
	private static final Logger LOGGER = LogManager.getLogger(Where.class.getName());

	public Where(Operator item) {
		super();
		this.item = item;
	}

	private Operator item = null;

	public String to_filter() {
		if (item == null) {
			return "";
		}
		String to_filter = item.to_filter();
		to_filter = " where " + to_filter + " ";
		return to_filter;
	}

	public String toJson(String _default) {
		try {
			return Jackson.toJson(item);
		} catch (Exception e) {
			LOGGER.error("json 解析失败:", e);
			return _default;
		}
	}
}
