package com.infogen.aop.event_handle;

import org.apache.log4j.Logger;

import com.infogen.configuration.InfoGen_Configuration;
import com.infogen.infogen_aop.InfoGen_Agent_Advice_Method;
import com.infogen.tools.Tool_Core;

/**
 * 统计方法执行时间的处理器
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年2月27日 下午6:11:09
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_AOP_Handle_Execution extends InfoGen_AOP_Handle {
	@Override
	public InfoGen_Agent_Advice_Method attach(String class_name, String method_name, String user_defined) {
		InfoGen_Agent_Advice_Method method = new InfoGen_Agent_Advice_Method();
		method.setMethod_name(method_name);
		method.setLong_local_variable("infogen_logger_attach_start_millis");
		method.setInsert_before("infogen_logger_attach_start_millis =System.currentTimeMillis();");

		StringBuilder sbd = new StringBuilder();
		sbd.append("com.infogen.aop.event_handle.InfoGen_AOP_Handle_Execution.insert_after_call_back(");
		sbd.append("\"").append(class_name).append("\"").append(",");
		sbd.append("\"").append(method_name).append("\"").append(",");
		sbd.append("\"").append(user_defined).append("\"").append(",");
		sbd.append("infogen_logger_attach_start_millis, System.currentTimeMillis());");
		method.setInsert_after(sbd.toString());

		sbd.setLength(0);
		sbd.append("com.infogen.aop.event_handle.InfoGen_AOP_Handle_Execution.add_catch_call_back(");
		sbd.append("\"").append(class_name).append("\"").append(",");
		sbd.append("\"").append(method_name).append("\"").append(",");
		sbd.append("\"").append(user_defined).append("\"").append(",");
		sbd.append("$e);throw $e;");
		method.setAdd_catch(sbd.toString());

		return method;
	}

	public static void insert_after_call_back(String class_name, String method_name, String user_defined, long start_millis, long end_millis) {

		StringBuilder sbd = new StringBuilder();
		sbd.append(class_name).append(",").append(method_name).append(",").append(end_millis - start_millis);
		producer.send(InfoGen_Configuration.infogen_logger_topic_execution_time, class_name, sbd.toString());
		Logger logger = Logger.getLogger(class_name);
		logger.info(sbd.toString());
	}

	public static void add_catch_call_back(String class_name, String method_name, String user_defined, Throwable e) {
		StringBuilder sbd = new StringBuilder();
		sbd.append(class_name).append(",").append(method_name).append(",").append(e.getMessage()).append(",").append(Tool_Core.stacktrace(e));
		producer.send(InfoGen_Configuration.infogen_logger_topic_execution_exception, class_name, sbd.toString());
		Logger logger = Logger.getLogger(class_name);
		logger.error(sbd.toString());
	}
}
