package com.infogen.authc.configuration.handle.impl;

import com.infogen.authc.configuration.handle.Properties_Handle;
import com.larrylgq.aop.AOP;
import com.larrylgq.aop.tools.Tool_Core;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年6月19日 下午12:47:45
 * @since 1.0
 * @version 1.0
 */
public class Properties_Main_Handle extends Properties_Handle {

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
		String[] split = line.split("=");
		if (split.length > 1) {
			String key = Tool_Core.trim(split[0]);
			String value = Tool_Core.trim(split[1]);
			if (key.equals("com.infogen.authc.InfoGen_Authc_Handle::subject_dao")) {
				AOP.getInstance().add_autowired_field("com.infogen.authc.InfoGen_Authc_Handle", "subject_dao", value);
			}
		}
	}
}
