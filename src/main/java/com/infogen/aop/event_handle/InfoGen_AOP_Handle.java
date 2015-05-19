package com.infogen.aop.event_handle;

import com.infogen.aop.agent.InfoGen_Agent_Advice_Method;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年4月29日 下午3:35:56
 * @since 1.0
 * @version 1.0
 */
public abstract class InfoGen_AOP_Handle {
	public abstract InfoGen_Agent_Advice_Method attach(String class_name, String method_name, String user_defined);
}
