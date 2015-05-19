/**
 * 
 */
package com.infogen.kafka.event_handle;

/**
 * 日志消息接收触发的处理器
 * @author larry
 * @email larrylv@outlook.com
 * @version 创建时间 2014年12月30日 下午4:42:29
 */
public interface InfoGen_Logger_Handle_Consume {
	public abstract void handle_event(String message);
}
