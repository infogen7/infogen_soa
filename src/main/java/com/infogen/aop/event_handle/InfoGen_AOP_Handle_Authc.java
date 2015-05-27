package com.infogen.aop.event_handle;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.infogen.aop.annotation.Authc;
import com.infogen.authc.InfoGen_Auth;
import com.infogen.authc.exception.InfoGen_Auth_Exception;
import com.infogen.authc.exception.impl.Session_Lose_Exception;
import com.infogen.authc.subject.Subject;
import com.larrylgq.aop.advice.event_handle.AOP_Handle;
import com.larrylgq.aop.agent.Agent_Advice_Method;

/**
 * 统计方法执行时间的处理器
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年2月27日 下午6:11:09
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_AOP_Handle_Authc extends AOP_Handle {
	@Override
	public Agent_Advice_Method attach_method(String class_name, Method method, Annotation annotation) {
		String method_name = method.getName();
		Agent_Advice_Method advice_method = new Agent_Advice_Method();
		advice_method.setMethod_name(method_name);

		StringBuilder stringbuilder = new StringBuilder("com.infogen.aop.event_handle.InfoGen_AOP_Handle_Authc.check(\"");
		stringbuilder.append(((Authc) annotation).roles().replaceAll(" ", ""));
		stringbuilder.append("\");");
		advice_method.setInsert_before(stringbuilder.toString());

		advice_method.setSet_exception_types("com.infogen.authc.exception.InfoGen_Auth_Exception");

		return advice_method;
	}

	public static void check(String roles) throws InfoGen_Auth_Exception {
		Subject subject = InfoGen_Auth.getSubject();
		if (subject == null) {
			throw new Session_Lose_Exception();
		}

		subject.checkExpiration();

		if (!roles.isEmpty()) {
			subject.hasRole(roles.split(","));
		}

	}


}
