package com.infogen.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.larrylgq.aop.tools.Tool_Core;
import com.larrylgq.aop.tools.Tool_Jackson;

/**
 * 接口返回值封装
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年5月19日 下午2:50:30
 * @since 1.0
 * @version 1.0
 */
public class Return extends HashMap<String, Object> {
	private static final long serialVersionUID = 2203513787220720192L;
	private static final Logger LOGGER = Logger.getLogger(Return.class.getName());

	public enum Return_Fields {
		success, code, note
	}

	public static Return create(String json) {
		Return jo = new Return();
		try {
			Map<String, Object> fromJson = Tool_Jackson.toObject(json, new TypeReference<HashMap<String, Object>>() {
			});
			fromJson.forEach((k, v) -> {
				jo.put(k, v);
			});
		} catch (IOException e) {
			LOGGER.error("Return.create 解析 JSON 失败", e);
			return Return.FAIL(CODE.generate_return_error);
		}
		return jo;
	}

	public static Return SUCCESS(CODE code) {
		return SUCCESS(code.code, code.note);
	}

	public static Return SUCCESS(CODE code, Exception e) {
		return SUCCESS(code.code, Tool_Core.stacktrace(e));
	}

	public static Return SUCCESS(String code, String note) {
		Return jo = new Return();
		jo.put(Return_Fields.success.name(), true);
		jo.put(Return_Fields.note.name(), note);
		jo.put(Return_Fields.code.name(), code);
		return jo;
	}

	public static Return SUCCESS(Integer code, String note) {
		Return jo = new Return();
		jo.put(Return_Fields.success.name(), true);
		jo.put(Return_Fields.note.name(), note);
		jo.put(Return_Fields.code.name(), code);
		return jo;
	}

	public static Return FAIL(CODE code) {
		return FAIL(code.code, code.note);
	}

	public static Return FAIL(CODE code, Exception e) {
		return FAIL(code.code, Tool_Core.stacktrace(e));
	}

	public static Return FAIL(String code, String note) {
		Return jo = new Return();
		jo.put(Return_Fields.success.name(), false);
		jo.put(Return_Fields.note.name(), note);
		jo.put(Return_Fields.code.name(), code);
		return jo;
	}

	public static Return FAIL(Integer code, String note) {
		Return jo = new Return();
		jo.put(Return_Fields.success.name(), false);
		jo.put(Return_Fields.note.name(), note);
		jo.put(Return_Fields.code.name(), code);
		return jo;
	}

	@Override
	public Return put(String key, Object value) {
		super.put(key, value);
		return this;
	}

	public Return add(String key, Object value) {
		super.put(key, value);
		return this;
	}

	public Boolean is_success() {
		return (Boolean) this.getOrDefault(Return_Fields.success.name(), false);
	}

	public Boolean get_success() {
		return (Boolean) this.getOrDefault(Return_Fields.success.name(), false);
	}

	public Integer get_code() {
		return (Integer) this.getOrDefault(Return_Fields.code.name(), CODE.error.code);
	}

	public String get_note() {
		return (String) this.getOrDefault(Return_Fields.note.name(), "");
	}

	public String toJson() {
		try {
			return Tool_Jackson.toJson(this);
		} catch (Exception e) {
			LOGGER.error("json 解析失败:", e);
			return Tool_Jackson.toJson(Return.FAIL(CODE.generate_return_error));
		}
	}

}
