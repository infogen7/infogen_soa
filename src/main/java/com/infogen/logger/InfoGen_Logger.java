/**
 * @author larry/larrylv@outlook.com
 * @date 创建时间 2015年4月29日 下午3:07:47
 * @version 1.0
 */
package com.infogen.logger;


/**
 * @author larry/larrylv@outlook.com/创建时间 2015年4月29日 下午3:07:47
 * @since 1.0
 * @version 1.0
 */
public abstract class InfoGen_Logger {
	public static InfoGen_Logger_Kafka producer = InfoGen_Logger_Kafka.getInstance();
	public abstract void send(String topic, String key, String message);
}
