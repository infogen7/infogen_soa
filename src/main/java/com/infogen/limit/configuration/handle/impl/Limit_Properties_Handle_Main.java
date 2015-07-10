package com.infogen.limit.configuration.handle.impl;

import com.infogen.authc.configuration.handle.Authc_Properties_Handle;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年6月19日 下午12:47:45
 * @since 1.0
 * @version 1.0
 */
public class Limit_Properties_Handle_Main extends Authc_Properties_Handle {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.infogen.authc.configuration.handle.properties_handle#handle(java.lang.String)
	 */
	@Override
	public void handle(String line) {
		String[] split = line.split("=");
		if (split.length > 1) {
			// String key = Tool_Core.trim(split[0]);
			// String value = Tool_Core.trim(split[1]);
		}
	}
}
