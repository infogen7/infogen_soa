/**
 * 
 */
package com.infogen.tracking.kafka;

import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.infogen.configuration.InfoGen_Configuration;
import com.infogen.logger.Logger_Once;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

/**
 * 启动kafka生产者
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年8月3日 上午11:17:34
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_Logger_Kafka_Producer {
	private static final Logger LOGGER = LogManager.getLogger(InfoGen_Logger_Kafka_Producer.class.getName());

	private static class InnerInstance {
		public static final InfoGen_Logger_Kafka_Producer instance = new InfoGen_Logger_Kafka_Producer();
	}

	public static InfoGen_Logger_Kafka_Producer getInstance() {
		return InnerInstance.instance;
	}

	private InfoGen_Logger_Kafka_Producer() {
	}

	private Producer<String, String> producer;

	/**
	 * 启动kafka生产者
	 * 
	 * @param infogen_configuration
	 * @return
	 */
	public InfoGen_Logger_Kafka_Producer start(InfoGen_Configuration infogen_configuration) {
		if (infogen_configuration.kafka == null || infogen_configuration.kafka.trim().isEmpty()) {
			LOGGER.error("没有配置 kafka");
			return this;
		}
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
				producer.send(new KeyedMessage<String, String>(topic, key, message));
			} catch (Exception e) {
				LOGGER.warn("kafka 发送失败", e);
			}
		} else {
			Logger_Once.warn("kafka未初始化");
		}
	}
}
