package com.infogen.tracking.event_handle;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.infogen.aop.advice.event_handle.AOP_Handle;
import com.infogen.aop.agent.Agent_Advice_Method;
import com.infogen.core.json.Return;
import com.infogen.core.tools.Tool_Jackson;
import com.infogen.tracking.CallChain;
import com.infogen.tracking.ThreadLocal_Tracking;
import com.infogen.tracking.annotation.Execution;
import com.infogen.tracking.enum0.Function_Type;
import com.infogen.tracking.kafka.InfoGen_Logger_Kafka_Producer;

/**
 * 统计方法执行时间和调用链记录的处理器
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年2月27日 下午6:11:09
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_AOP_Handle_Execution extends AOP_Handle {
	private static final Logger LOGGER = LogManager.getLogger(InfoGen_AOP_Handle_Execution.class.getName());

	@Override
	public Agent_Advice_Method attach_method(String class_name, Method method, Annotation annotation) {
		String method_name = method.getName();
		Agent_Advice_Method advice_method = new Agent_Advice_Method();
		advice_method.setMethod_name(method_name);
		advice_method.setLong_local_variable("infogen_logger_attach_start_millis");

		Class<?>[] parameterTypes = method.getParameterTypes();
		StringBuilder stringbuilder = new StringBuilder();
		for (Class<?> type : parameterTypes) {
			stringbuilder.append(type.getName()).append(" ");
		}
		String full_method_name = stringbuilder.toString();
		map.put(full_method_name, new AtomicInteger(1));
		advice_method.setInsert_before("infogen_logger_attach_start_millis =System.currentTimeMillis();com.infogen.tracking.event_handle.InfoGen_AOP_Handle_Execution.insert_before_call_back(\"" + full_method_name + "\");");

		String user_definition = ((Execution) annotation).user_definition();
		if (user_definition.contains(",")) {
			user_definition.replaceAll(",", "|");
			LOGGER.warn("注解Execution中user_definition字段不能出现 ',' 将被替换成 '|' ".concat(class_name).concat(".").concat(method_name));
		}
		Function_Type type = ((Execution) annotation).type();
		StringBuilder sbd = new StringBuilder();
		sbd.append("com.infogen.tracking.event_handle.InfoGen_AOP_Handle_Execution.insert_after_call_back(");
		sbd.append("\"").append(class_name).append("\"").append(",");
		sbd.append("\"").append(method_name).append("\"").append(",");
		sbd.append("\"").append(user_definition).append("\"").append(",");
		sbd.append(type.ordinal()).append(",");
		sbd.append("\"").append(full_method_name).append("\"").append(",");
		sbd.append("infogen_logger_attach_start_millis, System.currentTimeMillis(),$_);");
		advice_method.setInsert_after(sbd.toString());

		sbd.setLength(0);
		sbd.append("com.infogen.tracking.event_handle.InfoGen_AOP_Handle_Execution.add_catch_call_back(");
		sbd.append("\"").append(class_name).append("\"").append(",");
		sbd.append("\"").append(method_name).append("\"").append(",");
		sbd.append("\"").append(user_definition).append("\"").append(",");
		sbd.append(type.ordinal()).append(",");
		sbd.append("\"").append(full_method_name).append("\"").append(",");
		sbd.append("$e);throw $e;");
		advice_method.setAdd_catch(sbd.toString());

		return advice_method;
	}

	private static final Map<String, AtomicInteger> map = new HashMap<>();

	public static void insert_before_call_back(String full_method_name) {
		map.get(full_method_name).incrementAndGet();
	}

	public static final InfoGen_Logger_Kafka_Producer producer = InfoGen_Logger_Kafka_Producer.getInstance();
	public static final String infogen_topic_tracking = "infogen_topic_tracking";
	public static Boolean open_return_size = false;

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

	// traceid,sequence,来源地址 ,来源ip,当前地址,当前ip,当前服务 ,当前类,当前方法,调用时间 ,调用时长,调用状态(成功/失败) ,返回数据大小,cookie等用户标识,sessionid(token),方法类型(mysql/redis/interface),当前并发数
	// tr00000,0 ,home.html ,xx ,send ,xx ,中控 ,2015050X ,300ms ,ok/error/auth,1.3k ,t0000,测试/京东/聚信立, a00000...
	public static void insert_after_call_back(String class_name, String method_name, String user_definition, int type, String full_method_name, long start_millis, long end_millis, Object return0) {
		CallChain callChain = ThreadLocal_Tracking.getCallchain().get();
		if (callChain == null) {
			return;
		}

		StringBuilder sbd = get_callchain(callChain);
		sbd.append(class_name).append(",");
		sbd.append(method_name).append(",");
		sbd.append(start_millis).append(",");
		sbd.append(end_millis - start_millis).append(",");
		sbd.append(1).append(",");
		if (return0 instanceof String) {
			sbd.append(return0.toString().length());
		} else if (open_return_size && return0 instanceof Return) {
			sbd.append(Tool_Jackson.toJson(return0).getBytes().length);
		} else {
			sbd.append(-1);
		}
		sbd.append(",");
		// 用户标识
		sbd.append(callChain.getIdentify()).append(",");
		// 会话ID
		String sessionid = callChain.getSessionid();
		sbd.append(sessionid == null ? "" : sessionid).append(",");
		// 客户端类型
		sbd.append(type).append(",");
		// 当前并发数
		sbd.append(map.get(full_method_name).decrementAndGet());
		producer.send(infogen_topic_tracking, callChain.getTrackid(), sbd.toString());
	}

	public static void add_catch_call_back(String class_name, String method_name, String user_definition, int type, String full_method_name, Throwable e) {
		CallChain callChain = ThreadLocal_Tracking.getCallchain().get();

		StringBuilder sbd = get_callchain(callChain);
		sbd.append(class_name).append(",");
		sbd.append(method_name).append(",");
		sbd.append(System.currentTimeMillis()).append(",");
		sbd.append(0).append(",");
		sbd.append(0).append(",");
		sbd.append(0).append(",");
		// 用户标识
		sbd.append(callChain.getIdentify()).append(",");
		// 会话ID
		String sessionid = callChain.getSessionid();
		sbd.append(sessionid == null ? "" : sessionid).append(",");
		// 客户端类型
		sbd.append(type).append(",");
		// 当前并发数
		sbd.append(map.get(full_method_name).decrementAndGet());
		producer.send(infogen_topic_tracking, callChain.getTrackid(), sbd.toString());
	}
}
