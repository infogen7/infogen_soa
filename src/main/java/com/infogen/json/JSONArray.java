package com.infogen.json;

import java.io.IOException;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * HTTP协议调用端json处理类
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年7月31日 上午9:52:56
 * @since 1.0
 * @version 1.0
 */
public class JSONArray extends ArrayList<Object> {
	private static final long serialVersionUID = -1975539711595702789L;

	public static JSONArray create() {
		return new JSONArray();
	}

	public static JSONArray create(String json_array) throws IOException {
		JSONArray ja = new JSONArray();
		ArrayList<Object> fromJson = Jackson.toObject(json_array, new TypeReference<ArrayList<Object>>() {
		});
		for (Object object : fromJson) {
			ja.add(object);
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

	public JSONObject getAsJSONObject(Integer index) throws JsonProcessingException, IOException {
		Object object = this.get(index);
		if (object == null) {
			return null;
		}
		return Jackson.toObject(Jackson.toJson(object), new TypeReference<JSONObject>() {
		});
	}

	public JSONArray getAsJSONArray(Integer index) throws JsonProcessingException, IOException {
		Object object = this.get(index);
		if (object == null) {
			return null;
		}
		return Jackson.toObject(Jackson.toJson(object), new TypeReference<JSONArray>() {
		});
	}

	public <T> T getAsMapOrList(Integer index, TypeReference<T> typereference) throws JsonProcessingException, IOException {
		Object object = this.get(index);
		if (object == null) {
			return null;
		}
		return (T) Jackson.toObject(Jackson.toJson(object), typereference);
	}

	public <T> T getAsClass(Integer index, Class<T> clazz) throws JsonProcessingException, IOException {
		Object object = this.get(index);
		if (object == null) {
			return null;
		}
		return (T) Jackson.toObject(Jackson.toJson(object), clazz);
	}
	///////////////////////////////////////////////////////////////////

	public String toJson() throws JsonProcessingException {
		return Jackson.toJson(this);
	}
}
