package com.infogen.core.json;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.infogen.core.tools.Tool_Jackson;
import com.infogen.core.util.CODE;

/**
 * HTTP协议返回值封装
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年5月19日 下午2:50:30
 * @since 1.0
 * @version 1.0
 */
public class Return extends JSONObject {
	private static final long serialVersionUID = 2203513787220720192L;
	private static final Logger LOGGER = LogManager.getLogger(Return.class.getName());

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
		return new Return().add(key, value);
	}

	public static Return create(String json) {
		Return jo = new Return();
		try {
			Map<String, Object> fromJson = Tool_Jackson.toObject(json, new TypeReference<HashMap<String, Object>>() {
			});
			for (Entry<String, Object> entry : fromJson.entrySet()) {
				jo.put(entry.getKey(), entry.getValue());
			}
		} catch (IOException e) {
			LOGGER.error("Return.create 解析 JSON 失败", e);
			return Return.create(CODE.generate_return_error);
		}
		return jo;
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

	public Return add(String key, Object value) {
		super.put(key, value);
		return this;
	}
}
