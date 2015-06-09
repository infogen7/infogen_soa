package com.infogen.aop.event_handle;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.infogen.aop.annotation.Execution;
import com.infogen.logger.kafka.InfoGen_Logger_Kafka_Producer;
import com.infogen.tracking.CallChain;
import com.infogen.tracking.ThreadLocal_Tracking;
import com.larrylgq.aop.advice.event_handle.AOP_Handle;
import com.larrylgq.aop.agent.Agent_Advice_Method;

/**
 * 统计方法执行时间的处理器
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年2月27日 下午6:11:09
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_AOP_Handle_Execution extends AOP_Handle {

	@Override
	public Agent_Advice_Method attach_method(String class_name, Method method, Annotation annotation) {
		String method_name = method.getName();
		Agent_Advice_Method advice_method = new Agent_Advice_Method();
		advice_method.setMethod_name(method_name);
		advice_method.setLong_local_variable("infogen_logger_attach_start_millis");
		advice_method.setInsert_before("infogen_logger_attach_start_millis =System.currentTimeMillis();");

		StringBuilder sbd = new StringBuilder();
		sbd.append("com.infogen.aop.event_handle.InfoGen_AOP_Handle_Execution.insert_after_call_back(");
		sbd.append("\"").append(class_name).append("\"").append(",");
		sbd.append("\"").append(method_name).append("\"").append(",");
		sbd.append("\"").append(((Execution) annotation).value()).append("\"").append(",");
		sbd.append("infogen_logger_attach_start_millis, System.currentTimeMillis());");
		advice_method.setInsert_after(sbd.toString());

		sbd.setLength(0);
		sbd.append("com.infogen.aop.event_handle.InfoGen_AOP_Handle_Execution.add_catch_call_back(");
		sbd.append("\"").append(class_name).append("\"").append(",");
		sbd.append("\"").append(method_name).append("\"").append(",");
		sbd.append("\"").append(((Execution) annotation).value()).append("\"").append(",");
		sbd.append("$e);throw $e;");
		advice_method.setAdd_catch(sbd.toString());

		return advice_method;
	}

	public static InfoGen_Logger_Kafka_Producer producer = InfoGen_Logger_Kafka_Producer.getInstance();

	public static void insert_after_call_back(String class_name, String method_name, String user_definition, long start_millis, long end_millis) {
		CallChain callChain = ThreadLocal_Tracking.getCallchain().get();

		StringBuilder sbd = new StringBuilder();
		sbd.append(callChain.getIdentify()).append(",");

		// sbd.append(request_ip.get()).append(",").append(module.get()).append(",").append(class_name).append(",").append(method_name).append(",").append(end_millis - start_millis);
		// producer.send(InfoGen_AOP_Configuration.infogen_logger_topic_execution_time, class_name, sbd.toString());
		// Logger logger = Logger.getLogger(class_name);
		// logger.info(sbd.toString());
	}

	public static void add_catch_call_back(String class_name, String method_name, String user_definition, Throwable e) {
		// StringBuilder sbd = new StringBuilder();
		// sbd.append(class_name).append(",").append(method_name).append(",").append(e.getMessage()).append(",").append(Tool_Core.stacktrace(e));
		// producer.send(InfoGen_AOP_Configuration.infogen_logger_topic_execution_exception, class_name, sbd.toString());
		// Logger logger = Logger.getLogger(class_name);
		// logger.error(sbd.toString());
	}
}
