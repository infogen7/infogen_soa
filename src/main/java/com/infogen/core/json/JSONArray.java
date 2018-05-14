package com.infogen.core.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * HTTP协议调用端json处理类
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年7月31日 上午9:52:56
 * @since 1.0
 * @version 1.0
 */
public class JSONArray extends ArrayList<Object> {
	private static final long serialVersionUID = -1975539711595702789L;
	private static final Logger LOGGER = LogManager.getLogger(JSONArray.class.getName());

	public static JSONArray create() {
		return new JSONArray();
	}

	public static JSONArray create(String value) {
		JSONArray ja = new JSONArray();
		ja.add(value);
		return ja;
	}

	public static JSONArray create(List<String> list) {
		JSONArray ja = new JSONArray();
		for (String item : list) {
			ja.add(item);
		}
		return ja;
	}

	@Override
	public boolean add(Object value) {
		return super.add(value);
	}
	///////////////////////////////////////////////////////////// json工具//////////////////////////////////////////

	public String getAsString(Integer index, String _default) {
		Object object = this.get(index);
		return object != null ? object.toString() : _default;
	}

	public Boolean getAsBoolean(Integer index, Boolean _default) {
		Object object = this.get(index);
		return object != null ? Boolean.valueOf(object.toString()) : _default;
	}

	public Long getAsLong(Integer index, Long _default) {
		Object object = this.get(index);
		return object != null ? Long.valueOf(object.toString()) : _default;
	}

	public Integer getAsInteger(Integer index, Integer _default) {
		Object object = this.get(index);
		return object != null ? Integer.valueOf(object.toString()) : _default;
	}

	public Double getAsDouble(Integer index, Double _default) {
		Object object = this.get(index);
		return object != null ? Double.valueOf(object.toString()) : _default;
	}

	public Float getAsFloat(Integer index, Float _default) {
		Object object = this.get(index);
		return object != null ? Float.valueOf(object.toString()) : _default;
	}

	public JSONObject getAsJSONObject(Integer index, JSONObject _default) {
		return getAsMapOrList(index, new TypeReference<JSONObject>() {
		}, _default);
	}

	public JSONArray getAsJSONArray(Integer index, JSONArray _default) {
		return getAsMapOrList(index, new TypeReference<JSONArray>() {
		}, _default);
	}

	/////////////////////////////////////////////////////////////////////
	public <T> T getAsMapOrList(Integer index, TypeReference<T> typereference, T _default) {
		Object object = this.get(index);
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

	public <T> T getAsClass(Integer index, Class<T> clazz, T _default) {
		Object object = this.get(index);
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
	public static JSONArray toArray(String json_array) throws JsonParseException, JsonMappingException, IOException {
		return Jackson.toObject(json_array, new TypeReference<JSONArray>() {
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
