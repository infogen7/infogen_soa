package com.infogen.aop.event_handle;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.infogen.aop.InfoGen_AOP_Configuration;
import com.infogen.aop.annotation.Execution;
import com.infogen.logger.kafka.InfoGen_Logger_Kafka_Producer;
import com.infogen.tracking.CallChain;
import com.infogen.tracking.ThreadLocal_Tracking;
import com.infogen.util.Return;
import com.larrylgq.aop.advice.event_handle.AOP_Handle;
import com.larrylgq.aop.agent.Agent_Advice_Method;
import com.larrylgq.aop.tools.Tool_Jackson;

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
		sbd.append("infogen_logger_attach_start_millis, System.currentTimeMillis(),$_);");
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

	public static final InfoGen_Logger_Kafka_Producer producer = InfoGen_Logger_Kafka_Producer.getInstance();

	public static void insert_before_call_back(String class_name, String method_name, String user_definition, long start_millis) {
	}

	private static StringBuilder get_callchain(CallChain callChain) {
		StringBuilder sbd = new StringBuilder();
		sbd.append(callChain.getTrackid()).append(",");
		sbd.append(callChain.getSequence()).append(",");
		String referer = callChain.getReferer();
		sbd.append(referer == null ? "" : referer).append(",");
		sbd.append(callChain.getReferer_ip()).append(",");
		sbd.append(callChain.getTarget()).append(",");
		sbd.append(callChain.getTarget_ip()).append(",");
		sbd.append(callChain.getTarget_server()).append(",");
		return sbd;
	}

	// traceid,sequence,来源地址 ,来源ip,当前地址,当前ip,当前服务 ,当前类,当前方法,调用时间 ,调用时长,调用状态(成功/失败) ,返回数据大小,cookie等用户标识,sessionid(token),客户端类型
	// tr00000,0 ,home.html ,xx ,send ,xx ,中控 ,2015050X ,300ms ,ok/error/auth,1.3k ,t0000,测试/京东/聚信立, a00000...
	public static void insert_after_call_back(String class_name, String method_name, String user_definition, long start_millis, long end_millis, Object return0) {
		CallChain callChain = ThreadLocal_Tracking.getCallchain().get();

		StringBuilder sbd = get_callchain(callChain);
		sbd.append(class_name).append(",");
		sbd.append(method_name).append(",");
		sbd.append(start_millis).append(",");
		sbd.append(end_millis - start_millis).append(",");
		sbd.append(1).append(",");
		//
		if (return0 instanceof String) {
			sbd.append(return0.toString().getBytes().length);
		} else if (return0 instanceof Return) {
			sbd.append(Tool_Jackson.toJson(return0).getBytes().length);
		}
		sbd.append(",");
		sbd.append(callChain.getIdentify()).append(",");
		// 客户端类型
		sbd.append(",");
		// session
		producer.send(InfoGen_AOP_Configuration.infogen_topic_tracking, callChain.getTrackid(), sbd.toString());
	}

	public static void add_catch_call_back(String class_name, String method_name, String user_definition, Throwable e) {
		CallChain callChain = ThreadLocal_Tracking.getCallchain().get();

		StringBuilder sbd = get_callchain(callChain);
		sbd.append(class_name).append(",");
		sbd.append(method_name).append(",");
		sbd.append(System.currentTimeMillis()).append(",");
		sbd.append(0).append(",");
		sbd.append(0).append(",");
		sbd.append(0).append(",");

		sbd.append(callChain.getIdentify()).append(",");
		// 客户端类型
		sbd.append(",");
		// session
		producer.send(InfoGen_AOP_Configuration.infogen_topic_tracking, callChain.getTrackid(), sbd.toString());
	}
}
