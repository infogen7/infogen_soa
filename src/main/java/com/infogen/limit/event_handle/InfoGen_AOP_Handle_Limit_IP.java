package com.infogen.limit.event_handle;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.infogen.aop.advice.event_handle.AOP_Handle;
import com.infogen.aop.agent.Agent_Advice_Method;
import com.infogen.limit.annotation.Limit_IP;

/**
 * 统计方法执行时间和调用链记录的处理器
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年2月27日 下午6:11:09
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_AOP_Handle_Limit_IP extends AOP_Handle {

	@Override
	public Agent_Advice_Method attach_method(String class_name, Method method, Annotation annotation) {
		Agent_Advice_Method advice_method = new Agent_Advice_Method();
		advice_method.setMethod_name(method.getName());

		int frequency = ((Limit_IP) annotation).frequency();

		StringBuilder insert_before = new StringBuilder();
		insert_before.append("com.infogen.limit.event_handle.InfoGen_AOP_Handle_Limit_IP.insert_before_call_back(");
		insert_before.append("\"").append(frequency).append("\");");
		advice_method.setInsert_before(insert_before.toString());
		return advice_method;
	}

	public InfoGen_AOP_Handle_Limit_IP(Limit_IP_Handle handle) {
		super();
		InfoGen_AOP_Handle_Limit_IP.limit_ip_handle = handle;
	}

	private static Limit_IP_Handle limit_ip_handle = null;

	public static void insert_before_call_back(Integer frequency) {
		if (limit_ip_handle != null) {
			limit_ip_handle.insert_before_call_back(frequency);
		}
	}
}
