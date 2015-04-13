/**
 * 
 */
package com.infogen.tools;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * json转换
 * 
 * @author larry
 * @email larrylv@outlook.com
 * @version 创建时间 2015年2月15日 上午11:57:38
 */
public class Tool_Jackson {
	public static Logger logger = Logger.getLogger(Tool_Jackson.class.getName());
	private final static ObjectMapper objectMapper = new ObjectMapper();

	static {
		// TODO 是否需要时间转换 默认时间戳
		objectMapper.setDateFormat(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
		// 允许单引号
		objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		// 允许反斜杆等字符
		objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
		// 允许出现对象中没有的字段
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	private Tool_Jackson() {
	}

	public static String toJson(Object object) {
		try {
			return objectMapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			logger.error("对象转json失败", e);
		}
		return "";
	}

	public static <T> T toObject(String json, Class<T> valueType) throws JsonParseException, JsonMappingException, IOException {
		return objectMapper.readValue(json, valueType);
	}

	@SuppressWarnings("unchecked")
	public static <T> T toObject(String json, TypeReference<T> typeReference) throws JsonParseException, JsonMappingException, IOException {
		return (T) objectMapper.readValue(json, typeReference);
	}
}
