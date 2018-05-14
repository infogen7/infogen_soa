package com.infogen.sql;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.infogen.core.json.Jackson;

/**
 * @author larry
 * @version 创建时间 2017年9月26日 上午10:33:28
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, setterVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE)
public class Json extends Operator {
	private static final long serialVersionUID = -7495371176994686065L;
	private static final Logger LOGGER = LogManager.getLogger(JsonArray.class.getName());

	public Json(String key, Set<? extends Object> items) {
		super();
		this.key = key;
		for (Object string : items) {
			this.items.put(string.toString(), 1);
		}
	}

	private Map<String, Integer> items = new HashMap<>();
	public String key = "";

	public void add(String item) {
		this.items.put(item, 1);
	}

	public String to_filter() {
		// consumer_preference @> '{"健身":1}'
		// consumer_preference ? '健身'
		// abroad ?| array['德国', '英国']
		// jsonb_exists_any(abroad,array['巴西'])
		if (key == null || key.trim().isEmpty() || items.isEmpty()) {
			return " 1 = 1 ";
		}
		StringBuilder string_builder = new StringBuilder();
		try {
			string_builder.append(" jsonb_exists_any(").append(key).append(",array").append(Jackson.toJson(items.keySet()).replaceAll("\"", "'")).append(") ");
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
	public Map<String, Integer> getItems() {
		return items;
	}

	/**
	 * @param items
	 *            the items to set
	 */
	public void setItems(Map<String, Integer> items) {
		this.items = items;
	}

}
