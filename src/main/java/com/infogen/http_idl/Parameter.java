package com.infogen.http_idl;

import java.io.IOException;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.infogen.json.Jackson;

/**
 * HTTP协议调用端输入参数类
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年6月12日 下午3:56:17
 * @since 1.0
 * @version 1.0
 */
public class Parameter extends IdentityHashMap<String, Object> {
	private static final long serialVersionUID = -5436768657673377874L;
	private static final Logger LOGGER = LogManager.getLogger(Parameter.class.getName());

	public static Parameter create() {
		return new Parameter();
	}

	public static Parameter create(String key, Object value) {
		return new Parameter().put(key, value);
	}

	public static Parameter create(Map<String, List<String>> name_value_pair) {
		Parameter parameter = new Parameter();
		name_value_pair.forEach((key, values) -> {
			values.forEach(value -> {
				parameter.put(new String(key), value);
			});
		});
		return parameter;
	}

	public static Parameter create(String json) throws JsonParseException, JsonMappingException, IOException {
		Parameter parameter = new Parameter();
		Map<String, Object> fromJson = Jackson.toObject(json, new TypeReference<IdentityHashMap<String, Object>>() {
		});
		for (Entry<String, Object> entry : fromJson.entrySet()) {
			parameter.put(entry.getKey(), entry.getValue());
		}
		return parameter;
	}

	//////////////////////// @Override/////////////////////////////////////
	@Override
	public Parameter put(String key, Object value) {
		super.put(key, value);
		return this;
	}

	///////////////////////////////////////////////////////////
	public String toJson(String _default) {
		try {
			return Jackson.toJson(this);
		} catch (Exception e) {
			LOGGER.error("json 解析失败:", e);
			return _default;
		}
	}
}
