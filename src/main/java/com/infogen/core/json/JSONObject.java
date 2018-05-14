package com.infogen.core.json;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * HTTP协议调用端json处理类
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年7月31日 上午9:44:55
 * @since 1.0
 * @version 1.0
 */
public class JSONObject extends HashMap<String, Object> {
	private static final long serialVersionUID = 684659227542251158L;
	private static final Logger LOGGER = LogManager.getLogger(JSONObject.class.getName());

	public static JSONObject create() {
		return new JSONObject();
	}

	public static JSONObject create(String key, String value) {
		return new JSONObject().put(key, value);
	}

	public static JSONObject create(Map<String, Object> map) {
		JSONObject jo = new JSONObject();
		for (String key : map.keySet()) {
			jo.put(key, map.get(key));
		}
		return jo;
	}

	@Override
	public JSONObject put(String key, Object value) {
		super.put(key, value);
		return this;
	}
	///////////////////////////////////////////////////////////// json工具//////////////////////////////////////////

	public String getAsString(String key, String _default) {
		Object object = this.get(key);
		return object != null ? object.toString() : _default;
	}

	public Boolean getAsBoolean(String key, Boolean _default) {
		Object object = this.get(key);
		return object != null ? Boolean.valueOf(object.toString()) : _default;
	}

	public Long getAsLong(String key, Long _default) {
		Object object = this.get(key);
		return object != null ? Long.valueOf(object.toString()) : _default;
	}

	public Integer getAsInteger(String key, Integer _default) {
		Object object = this.get(key);
		return object != null ? Integer.valueOf(object.toString()) : _default;
	}

	public Double getAsDouble(String key, Double _default) {
		Object object = this.get(key);
		return object != null ? Double.valueOf(object.toString()) : _default;
	}

	public Float getAsFloat(String key, Float _default) {
		Object object = this.get(key);
		return object != null ? Float.valueOf(object.toString()) : _default;
	}

	public JSONObject getAsJSONObject(String key, JSONObject _default) {
		return getAsMapOrList(key, new TypeReference<JSONObject>() {
		}, _default);
	}

	public JSONArray getAsJSONArray(String key, JSONArray _default) {
		return getAsMapOrList(key, new TypeReference<JSONArray>() {
		}, _default);
	}

	///////////////////////////////////////////////////////////////////
	public <T> T getAsMapOrList(String key, TypeReference<T> typereference, T _default) {
		Object object = this.get(key);
		if (object == null) {
			return _default;
		}
		try {
			return (T) Jackson.toObject(Jackson.toJson(object), typereference);
		} catch (IOException e) {
			LOGGER.error("json 转换对象失败:", e);
			return _default;
		}
	}

	public <T> T getAsClass(String key, Class<T> clazz, T _default) {
		Object object = this.get(key);
		if (object == null) {
			return _default;
		}
		try {
			return (T) Jackson.toObject(Jackson.toJson(object), clazz);
		} catch (IOException e) {
			LOGGER.error("json 转换对象失败:", e);
			return _default;
		}
	}

	///////////////////////////////////////////////////////////////////
	public static JSONObject toObject(String json) throws JsonParseException, JsonMappingException, IOException {
		return Jackson.toObject(json, new TypeReference<JSONObject>() {
		});
	}

	public String toJson(String _default) {
		try {
			return Jackson.toJson(this);
		} catch (Exception e) {
			LOGGER.error("json 解析失败:", e);
			return _default;
		}
	}
}
