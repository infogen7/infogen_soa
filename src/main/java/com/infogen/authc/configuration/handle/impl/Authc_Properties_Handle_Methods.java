package com.infogen.authc.configuration.handle.impl;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.infogen.authc.configuration.handle.Authc_Properties_Handle;
import com.infogen.core.tools.Tool_Core;

/**
 * 解析安全框架ini配置中[authc]方法权限配置的部分
 * @author larry/larrylv@outlook.com/创建时间 2015年6月19日 下午12:48:48
 * @since 1.0
 * @version 1.0
 */
public class Authc_Properties_Handle_Methods extends Authc_Properties_Handle {
	private static final Logger LOGGER = Logger.getLogger(Authc_Properties_Handle_Methods.class.getName());
	public static final Map<String, String[]> urls_equal = new HashMap<>();
	public static final Map<String, String[]> urls_rule = new LinkedHashMap<>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.infogen.authc.configuration.handle.properties_handle#handle(java.lang.String)
	 */
	@Override
	public void handle(String line) {
		String[] split = line.split("=");
		if (split.length <= 1) {
			LOGGER.error("格式错误 ".concat(line));
			return;
		}

		Map<String, String[]> map_current = urls_equal;
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

		if (value.split(",")[0].trim().equals("authc")) {
			// 删除已有的被当前规则包含的规则
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

			String roles = value.substring(value.indexOf("roles["));
			roles = roles.replace("roles[", "");
			roles = roles.replace("]", "");
			roles = Tool_Core.trim(roles);

			map_current.put(key, roles.split(","));
		}
	}

}
