package com.infogen.authc.configuration.handle.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.infogen.authc.configuration.handle.Properties_Handle;
import com.larrylgq.aop.tools.Tool_Core;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年6月19日 下午12:48:48
 * @since 1.0
 * @version 1.0
 */
public class Properties_Methods_Handle extends Properties_Handle {
	private static final Logger LOGGER = Logger.getLogger(Properties_Methods_Handle.class.getName());
	public static final Map<String, String[]> urls_equal = new HashMap<>();
	public static final Map<String, String[]> urls_rule = new HashMap<>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.infogen.authc.configuration.handle.properties_handle#handle(java.lang.String)
	 */
	@Override
	public void handle(String line) {
		if (line == null || line.isEmpty()) {
			return;
		}
		Map<String, String[]> map_current = urls_equal;
		String[] split = line.split("=");
		if (split.length > 1) {
			String key = Tool_Core.trim(split[0]);
			String value = Tool_Core.trim(split[1]);
			if (key.endsWith("*")) {
				key = key.substring(0, key.length() - 1);
				map_current = urls_rule;
			}
			if (key.contains("*")) {
				LOGGER.error("url格式错误 eg:/a/b  或 /a/*:".concat(line));
				return;
			}
			for (String key_equal : urls_equal.keySet()) {
				if (key.startsWith(key_equal)) {
					urls_equal.remove(key_equal);
				}
			}
			for (String key_rule : urls_rule.keySet()) {
				if (key_rule.startsWith(key)) {
					urls_rule.remove(key_rule);
				}
			}
			value = value.replace("roles[", "");
			value = value.replace("]", "");
			value = Tool_Core.trim(value);
			map_current.put(key, value.split(","));
		}
	}

}
