package com.infogen.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.infogen.json.Jackson;

/**
 * @author larry
 * @version 创建时间 2017年9月26日 上午10:33:28
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, setterVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE)
public class JsonArray extends Operator {
	private static final long serialVersionUID = -7495371176994686065L;
	private static final Logger LOGGER = LogManager.getLogger(JsonArray.class.getName());

	public JsonArray(String key, Set<? extends Object> items) {
		super();
		this.key = key;
		for (Object string : items) {
			this.items.add(string.toString());
		}
	}

	private List<String> items = new ArrayList<>();
	public String key = "";

	public void add(String item) {
		this.items.add(item);
	}

	public String to_filter() {
		if (key == null || key.trim().isEmpty() || items.isEmpty()) {
			return " 1 = 1 ";
		}
		// abroad ?| array['德国', '英国']::jsonb
		// jsonb_exists_any(abroad,array['巴西'])
		StringBuilder string_builder = new StringBuilder();
		try {
			string_builder.append(" jsonb_exists_any(").append(key).append(",array").append(Jackson.toJson(items).replaceAll("\"", "'")).append(") ");
		} catch (JsonProcessingException e) {
			LOGGER.error("生成 jsonb_exists_any 错误 - ", e);
		}
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
	 * @return the items
	 */
	public List<String> getItems() {
		return items;
	}

	/**
	 * @param items
	 *            the items to set
	 */
	public void setItems(List<String> items) {
		this.items = items;
	}

}
