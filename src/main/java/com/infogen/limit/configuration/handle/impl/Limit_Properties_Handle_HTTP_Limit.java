package com.infogen.limit.configuration.handle.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.infogen.authc.configuration.handle.Authc_Properties_Handle;
import com.infogen.core.tools.Tool_Core;

/**
 * 并发数限流的ini配置文件中[limit-http]部分的解析器
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年6月19日 下午12:48:48
 * @since 1.0
 * @version 1.0
 */
public class Limit_Properties_Handle_HTTP_Limit extends Authc_Properties_Handle {
	private static final Logger LOGGER = Logger.getLogger(Limit_Properties_Handle_HTTP_Limit.class.getName());
	public static final Map<String, Integer> map = new HashMap<>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.infogen.authc.configuration.handle.properties_handle#handle(java.lang.String)
	 */
	@Override
	public void handle(String line) {
		String[] split = line.split("=");
		if (split.length <= 1) {
			LOGGER.warn("格式错误 ".concat(line));
			return;
		}
		String key = Tool_Core.trim(split[0]);
		String value = Tool_Core.trim(split[1]);

		Integer limit = Integer.valueOf(value);

		map.put(key, limit);
	}
}
