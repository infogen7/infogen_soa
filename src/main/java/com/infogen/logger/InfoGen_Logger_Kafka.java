/**
 * 
 */
package com.infogen.logger;

import java.util.Properties;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import org.apache.log4j.Logger;

import com.infogen.InfoGen_Jetty;
import com.infogen.configuration.InfoGen_Configuration;
import com.infogen.tools.Tool_Core;
import com.infogen.util.Logger_Once;

/**
 * 启动kafka生产者
 * 
 * @author larry
 * @email larrylv@outlook.com
 * @version 创建时间 2015年1月12日 上午10:00:46
 */
public class InfoGen_Logger_Kafka extends InfoGen_Logger {
	public final Logger logger = Logger.getLogger(InfoGen_Jetty.class.getName());
	private final String IP = Tool_Core.getLocalIP();

	private static class InnerInstance {
		public static InfoGen_Logger_Kafka instance = new InfoGen_Logger_Kafka();
	}

	public static InfoGen_Logger_Kafka getInstance() {
		return InnerInstance.instance;
	}

	private InfoGen_Logger_Kafka() {
	}

	private Producer<String, String> producer;

	/**
	 * 启动kafka生产者
	 * 
	 * @param infogen_configuration
	 * @return
	 */
	public InfoGen_Logger_Kafka start(InfoGen_Configuration infogen_configuration) {
		if (producer == null) {
			// 设置配置属性
			Properties props = new Properties();
			props.put("metadata.broker.list", infogen_configuration.kafka);
			props.put("serializer.class", "kafka.serializer.StringEncoder");
			// 触发acknowledgement机制，否则是fire and forget，可能会引起数据丢失
			// 值为0,1,-1,可以参考
			// http://kafka.apache.org/08/configuration.html
			props.put("request.required.acks", "1");
			// key.serializer.class默认为serializer.class
			// 如果topic不存在，则会自动创建，默认replication-factor为1，partitions为0
			ProducerConfig config = new ProducerConfig(props);
			// 创建producer
			producer = new Producer<String, String>(config);
		}
		return this;
	}

	/**
	 * 关闭一个kafka生产者
	 */
	public void close() {
		if (producer != null) {
			producer.close();
		}
	}

	/**
	 * 发送消息 如果生产者没有初始化只写一次日志
	 * 
	 * @param message
	 */
	public void send(String topic, String key, String message) {
		if (producer != null) {
			try {
				producer.send(new KeyedMessage<String, String>(topic, key, IP.concat(",").concat(message)));
			} catch (Exception e) {
				logger.warn("kafka 发送失败");
			}
		} else {
			Logger_Once.warn("kafka未初始化");
		}
	}
}
