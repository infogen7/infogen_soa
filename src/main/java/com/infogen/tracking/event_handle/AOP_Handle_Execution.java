package com.infogen.tracking.event_handle;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.infogen.aop.advice.event_handle.AOP_Handle;
import com.infogen.aop.agent.Agent_Advice_Method;
import com.infogen.tracking.annotation.Execution;

/**
 * 统计方法执行时间和调用链记录的处理器
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年2月27日 下午6:11:09
 * @since 1.0
 * @version 1.0
 */
public class AOP_Handle_Execution extends AOP_Handle {
	private static final Logger LOGGER = LogManager.getLogger(AOP_Handle_Execution.class.getName());

	@Override
	public Agent_Advice_Method attach_method(String class_name, Method method, Annotation annotation) {
		String method_name = method.getName();

		Class<?>[] parameterTypes = method.getParameterTypes();
		StringBuilder stringbuilder = new StringBuilder();
		for (Class<?> type : parameterTypes) {
			stringbuilder.append(type.getName()).append(" ");
		}
		String full_method_name = stringbuilder.toString();

		String user_definition = ((Execution) annotation).user_definition();
		if (user_definition.contains(",")) {
			user_definition.replaceAll(",", "|");
			LOGGER.warn("注解Execution中user_definition字段不能出现 ',' 将被替换成 '|' ".concat(class_name).concat(".").concat(method_name));
		}

		Agent_Advice_Method advice_method = new Agent_Advice_Method();
		advice_method.setMethod_name(method_name);
		advice_method.setLong_local_variable("infogen_logger_attach_start_millis");

		advice_method.setInsert_before("infogen_logger_attach_start_millis =System.currentTimeMillis();com.infogen.tracking.event_handle.InfoGen_AOP_Handle_Execution.insert_before_call_back(\"" + full_method_name + "\");");

		StringBuilder sbd = new StringBuilder();
		sbd.append("com.infogen.tracking.event_handle.InfoGen_AOP_Handle_Execution.insert_after_call_back(");
		sbd.append("\"").append(class_name).append("\"").append(",");
		sbd.append("\"").append(method_name).append("\"").append(",");
		sbd.append("\"").append(user_definition).append("\"").append(",");
		sbd.append("\"").append(full_method_name).append("\"").append(",");
		sbd.append("infogen_logger_attach_start_millis, System.currentTimeMillis(),$_);");
		advice_method.setInsert_after(sbd.toString());

		sbd.setLength(0);
		sbd.append("com.infogen.tracking.event_handle.InfoGen_AOP_Handle_Execution.add_catch_call_back(");
		sbd.append("\"").append(class_name).append("\"").append(",");
		sbd.append("\"").append(method_name).append("\"").append(",");
		sbd.append("\"").append(user_definition).append("\"").append(",");
		sbd.append("\"").append(full_method_name).append("\"").append(",");
		sbd.append("$e);throw $e;");
		advice_method.setAdd_catch(sbd.toString());

		init(full_method_name);
		return advice_method;
	}

	public AOP_Handle_Execution(Tracking_Handle handle) {
		super();
		AOP_Handle_Execution.tracking_handle = handle;
	}

	private static Tracking_Handle tracking_handle = null;

	public static void init(String full_method_name) {
		if (tracking_handle != null) {
			tracking_handle.init(full_method_name);
		}
	}

	public static void insert_before_call_back(String full_method_name) {
		if (tracking_handle != null) {
			tracking_handle.insert_before_call_back(full_method_name);
		}
	}

	public static void insert_after_call_back(String class_name, String method_name, String user_definition, String full_method_name, long start_millis, long end_millis, Object return0) {
		if (tracking_handle != null) {
			tracking_handle.insert_after_call_back(class_name, method_name, user_definition, full_method_name, start_millis, end_millis, return0);
		}
	}

	public static void add_catch_call_back(String class_name, String method_name, String user_definition, String full_method_name, Throwable e) {
		if (tracking_handle != null) {
			tracking_handle.add_catch_call_back(class_name, method_name, user_definition, full_method_name, e);
		}
	}
}
