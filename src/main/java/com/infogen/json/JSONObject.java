package com.infogen.json;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * HTTP协议调用端json处理类
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年7月31日 上午9:44:55
 * @since 1.0
 * @version 1.0
 */
public class JSONObject extends HashMap<String, Object> {
	private static final long serialVersionUID = 684659227542251158L;

	public static JSONObject create() {
		return new JSONObject();
	}

	public static JSONObject create(String json) throws IOException {
		JSONObject jo = new JSONObject();
		Map<String, Object> fromJson = Jackson.toObject(json, new TypeReference<HashMap<String, Object>>() {
		});
		for (Entry<String, Object> entry : fromJson.entrySet()) {
			jo.put(entry.getKey(), entry.getValue());
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
		return object != null && object.toString().trim().isEmpty() == false ? Boolean.valueOf(object.toString()) : _default;
	}

	public Long getAsLong(String key, Long _default) {
		Object object = this.get(key);
		return object != null && object.toString().trim().isEmpty() == false ? Long.valueOf(object.toString()) : _default;
	}

	public Integer getAsInteger(String key, Integer _default) {
		Object object = this.get(key);
		return object != null && object.toString().trim().isEmpty() == false ? Integer.valueOf(object.toString()) : _default;
	}

	public Double getAsDouble(String key, Double _default) {
		Object object = this.get(key);
		return object != null && object.toString().trim().isEmpty() == false ? Double.valueOf(object.toString()) : _default;
	}

	public Float getAsFloat(String key, Float _default) {
		Object object = this.get(key);
		return object != null && object.toString().trim().isEmpty() == false ? Float.valueOf(object.toString()) : _default;
	}

	public JSONObject getAsJSONObject(String key) throws JsonProcessingException, IOException {
		Object object = this.get(key);
		if (object == null) {
			return null;
		}
		return Jackson.toObject(Jackson.toJson(object), new TypeReference<JSONObject>() {
		});
	}

	public JSONArray getAsJSONArray(String key) throws JsonProcessingException, IOException {
		Object object = this.get(key);
		if (object == null) {
			return null;
		}
		return Jackson.toObject(Jackson.toJson(object), new TypeReference<JSONArray>() {
		});
	}

	public <T> T getAsMapOrList(String key, TypeReference<T> typereference) throws JsonProcessingException, IOException {
		Object object = this.get(key);
		if (object == null) {
			return null;
		}
		return (T) Jackson.toObject(Jackson.toJson(object), typereference);
	}

	public <T> T getAsClass(String key, Class<T> clazz) throws JsonProcessingException, IOException {
		Object object = this.get(key);
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
