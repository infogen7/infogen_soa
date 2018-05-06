package com.infogen.core.json;

import java.io.IOException;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * HTTP协议返回值封装
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年5月19日 下午2:50:30
 * @since 1.0
 * @version 1.0
 */
public class Return extends IdentityHashMap<String, Object> {
	private static final long serialVersionUID = 2203513787220720192L;

	//////////////////////////////// create//////////////////////////////////

	public static Return create(Integer code, String message) {
		Return jo = new Return();
		jo.put(Return_Fields.code.name(), code);
		jo.put(Return_Fields.message.name(), message);
		return jo;
	}

	public static Return create(String json) throws JsonParseException, JsonMappingException, IOException {
		Return jo = new Return();
		Map<String, Object> fromJson = Jackson.toObject(json, new TypeReference<HashMap<String, Object>>() {
		});
		for (Entry<String, Object> entry : fromJson.entrySet()) {
			jo.put(entry.getKey(), entry.getValue());
		}
		return jo;
	}
	//////////////////////////////////// GETTER SETTER///////////////////////////

	public Integer get_code() {
		return (Integer) this.getOrDefault(Return_Fields.code.name(), -1);
	}

	public String get_message() {
		return (String) this.getOrDefault(Return_Fields.message.name(), "");
	}

	//////////////////////// @Override/////////////////////////////////////
	@Override
	public Return put(String key, Object value) {
		super.put(key, value);
		return this;
	}

	public Return put(Map<String, ? extends Object> map) {
		for (String key : map.keySet()) {
			super.put(key, map.get(key));
		}
		return this;
	}

	public String toJson(String _default) {
		try {
			return Jackson.toJson(this);
		} catch (JsonProcessingException e) {
			return _default;
		}
	}
}
