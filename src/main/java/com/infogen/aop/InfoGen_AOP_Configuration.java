package com.infogen.aop;

import org.apache.log4j.Logger;

/**
 * AOP的工具类,可以获取存放在ThreadLocal中的对象
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年5月4日 下午2:11:06
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_AOP_Configuration {
	public final Logger logger = Logger.getLogger(InfoGen_AOP_Configuration.class.getName());
	public final static String infogen_logger_topic_execution_time = "infogen_logger_topic_execution_time";
	public final static String infogen_logger_topic_execution_exception = "infogen_logger_topic_execution_exception";
	public final static String infogen_logger_topic_invoke_time = "infogen_logger_topic_invoke_time";
	public final static String infogen_logger_topic_invoke_exception = "infogen_logger_topic_invoke_exception";
}
