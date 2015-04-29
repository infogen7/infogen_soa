/**
 * 
 */
package com.infogen.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.infogen.tools.Tool_Jackson;

/**
 * 接口返回值封装
 * 
 * @author larry
 * @email larry.lv.word@gmail.com
 * @version 创建时间 2013-4-11 下午7:57:11
 */
public class Return extends HashMap<String, Object> {
	private static final long serialVersionUID = 2203513787220720192L;
	public static final Logger logger = Logger.getLogger(Return.class.getName());

	public enum Return_Fields {
		success, code, note
	}

	public static Return create(String json) {
		Return jo = new Return();
		/*
		HashMap<String, Object> fromJson;
		try {
			fromJson = Tool_Jackson.toObject(json, new TypeReference<HashMap<String, Object>>() {
			});
			fromJson.forEach((k, v) -> {
				jo.put(k, v);
			});
		} catch (IOException e) {
			logger.error("Return.create 解析 JSON 失败", e);
		}
		*/
		HashMap<String, Object> fromJsons;
		try {
			fromJsons = Tool_Jackson.toObject(json, new TypeReference<HashMap<String, Object>>() {
			});
			Iterator<Entry<String, Object>> iter = fromJsons.entrySet().iterator(); 

			while(iter.hasNext()){ 
				HashMap.Entry entry = (HashMap.Entry)iter.next();
				Object key = entry.getKey(); 
				Object val = entry.getValue(); 
				jo.put((String) key, val);
			} 
		
		} catch (IOException e) {
			logger.error("Return.create 解析 JSON 失败", e);
		}
			
		return jo;
	}

	public static Return SUCCESS(Integer code, String note) {
		Return jo = new Return();
		jo.put(Return_Fields.success.name(), true);
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

	public Return put(String key, Object value) {
		super.put(key, value);
		return this;
	}

	public Boolean get_success() {
		return (Boolean) this.getOrDefault(Return_Fields.success.name(), false);
	}

	public Integer get_code() {
		return (Integer) this.getOrDefault(Return_Fields.code.name(), CODE._500.code);
	}

	public String get_note() {
		return (String) this.getOrDefault(Return_Fields.note.name(), "");
	}

	public String toJson() {
		try {
			return Tool_Jackson.toJson(this);
		} catch (Exception e) {
			logger.error("json 解析失败:", e);
			return Tool_Jackson.toJson(Return.FAIL(CODE._510.code, CODE._510.name()));
		}
	}

}
