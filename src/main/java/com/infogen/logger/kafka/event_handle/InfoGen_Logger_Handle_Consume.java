package com.infogen.logger.kafka.event_handle;

/**
 * 用于自定义日志消息处理的接口
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年8月3日 上午11:15:28
 * @since 1.0
 * @version 1.0
 */
public interface InfoGen_Logger_Handle_Consume {
	public abstract void handle_event(String message);
}
