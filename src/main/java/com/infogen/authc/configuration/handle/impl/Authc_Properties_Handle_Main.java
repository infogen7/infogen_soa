package com.infogen.authc.configuration.handle.impl;

import com.infogen.aop.AOP;
import com.infogen.authc.configuration.handle.Authc_Properties_Handle;
import com.infogen.core.tools.Tool_Core;

/**
 * 解析安全框架ini配置中[main] 基本配置的部分
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年6月19日 下午12:47:45
 * @since 1.0
 * @version 1.0
 */
public class Authc_Properties_Handle_Main extends Authc_Properties_Handle {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.infogen.authc.configuration.handle.properties_handle#handle(java.lang.String)
	 */
	@Override
	public void handle(String line) {
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
