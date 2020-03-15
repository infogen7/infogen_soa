package com.infogen.http_idl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.infogen.json.JSONObject;
import com.infogen.json.Jackson;

/**
 * HTTP协议返回值封装
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年5月19日 下午2:50:30
 * @since 1.0
 * @version 1.0
 */
public class Return extends JSONObject {
	private static final long serialVersionUID = 2203513787220720192L;

	private enum Return_Fields {
		code, message
	}

	//////////////////////////////// create//////////////////////////////////
	public static Return create() {
		return new Return();
	}

	public static Return create(Integer code, String message) {
		Return jo = new Return();
		jo.put(Return_Fields.code.name(), code);
		jo.put(Return_Fields.message.name(), message);
		return jo;
	}

	public static Return create(String json) throws IOException {
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

	@Override
	public Return put(String key, Object value) {
		super.put(key, value);
		return this;
	}
}
