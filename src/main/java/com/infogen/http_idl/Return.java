package com.infogen.http_idl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	private static final Logger LOGGER = LogManager.getLogger(Return.class.getName());
	private static final long serialVersionUID = 2203513787220720192L;

	private enum Return_Fields {
		code, message
	}

	//////////////////////////////// create//////////////////////////////////
	public static Return create(Integer code, String message) {
		Return jo = new Return();
		jo.put(Return_Fields.code.name(), code);
		jo.put(Return_Fields.message.name(), message);
		return jo;
	}

	//////////////////////////////////// GETTER SETTER///////////////////////////

	public Integer getCode() {
		return this.getAsInteger(Return_Fields.code.name(), -1);
	}

	public String getMessage() {
		return this.getAsString(Return_Fields.message.name(), "");
	}

	@Override
	public Return put(String key, Object value) {
		if (key.equals(Return_Fields.code.name()) && this.get(Return_Fields.code.name()) != null) {
			LOGGER.warn("#！！！Return 中已存在属性 code ！！！");
		}
		if (key.equals(Return_Fields.message.name()) && this.get(Return_Fields.message.name()) != null) {
			LOGGER.warn("#！！！Return 中已存在属性 message ！！！");
		}
		super.put(key, value);
		return this;
	}

	public Return put(String json) throws IOException {
		Map<String, Object> fromJson = Jackson.toObject(json, new TypeReference<HashMap<String, Object>>() {
		});
		for (Entry<String, Object> entry : fromJson.entrySet()) {
			this.put(entry.getKey(), entry.getValue());
		}
		return this;
	}
}
