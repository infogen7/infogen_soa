package com.infogen.core.json;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.infogen.core.CODE;
import com.infogen.core.tools.Tool_Jackson;

/**
 * HTTP协议返回值封装
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年5月19日 下午2:50:30
 * @since 1.0
 * @version 1.0
 */
public class Return extends JSONObject {
	private static final long serialVersionUID = 2203513787220720192L;

	//////////////////////////////// create//////////////////////////////////
	public static Return create() {
		return new Return();
	}

	public static Return create(Integer code, String note) {
		Return jo = new Return();
		jo.put(Return_Fields.code.name(), code);
		jo.put(Return_Fields.note.name(), note);
		return jo;
	}

	public static Return create(CODE code) {
		return create(code.code, code.note);
	}

	public static Return create(String key, Object value) {
		return new Return().put(key, value);
	}

	//////////////////////////////////// GETTER SETTER///////////////////////////

	public Integer get_code() {
		return (Integer) this.getOrDefault(Return_Fields.code.name(), CODE.error.code);
	}

	public String get_note() {
		return (String) this.getOrDefault(Return_Fields.note.name(), "");
	}

	//////////////////////// @Override/////////////////////////////////////
	@Override
	public Return put(String key, Object value) {
		super.put(key, value);
		return this;
	}

	////////////////////////////////////////////////////////////////////////
	public static Return toObject(String json) throws JsonParseException, JsonMappingException, IOException {
		Return jo = new Return();
		Map<String, Object> fromJson = Tool_Jackson.toObject(json, new TypeReference<HashMap<String, Object>>() {
		});
		for (Entry<String, Object> entry : fromJson.entrySet()) {
			jo.put(entry.getKey(), entry.getValue());
		}
		return jo;
	}
}
