package com.infogen.tracking.kafka;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.infogen.configuration.InfoGen_Configuration;

import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.serializer.StringDecoder;
import kafka.utils.VerifiableProperties;

/**
 * 基于kafka的日志消息接收处理器
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年8月3日 上午11:16:14
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_Logger_Kafka_Consumer {
	private static final Logger LOGGER = LogManager.getLogger(InfoGen_Logger_Kafka_Consumer.class.getName());
	public static ExecutorService executors = Executors.newCachedThreadPool((r) -> {
		Thread thread = Executors.defaultThreadFactory().newThread(r);
		thread.setDaemon(true);
		return thread;
	});

	/**
	 * 创建kafka消费者线程
	 * 
	 * @param infogen_configuration
	 *            zookeeper配置
	 * @param group
	 *            group
	 * @param topic
	 *            topic
	 * @param concurrent
	 *            并发数
	 * @param handle
	 *            处理逻辑
	 */
	public static void consume(InfoGen_Configuration infogen_configuration, String group, String topic, Integer concurrent, Class<? extends InfoGen_Consume_Handle> clazz) {
		Thread thread = new Thread(() -> {
			Properties props = new Properties();
			// zookeeper 配置
			props.put("zookeeper.connect", infogen_configuration.zookeeper);
			// group 代表一个消费组
			props.put("group.id", group);
			// zk连接超时
			props.put("zookeeper.session.timeout.ms", "10000");
			props.put("zookeeper.sync.time.ms", "2000"); // 从200修改成2000 太短有rebalance错误
			props.put("auto.commit.interval.ms", "1000");
			props.put("auto.offset.reset", "smallest");// 2个合法的值"largest"/"smallest",默认为"largest",此配置参数表示当此groupId下的消费者,在ZK中没有offset值时(比如新的groupId,或者是zk数据被清空),consumer应该从哪个offset开始消费.largest表示接受接收最大的offset(即最新消息),smallest表示最小offset,即从topic的开始位置消费所有消息.
			// 序列化类
			props.put("serializer.class", "kafka.serializer.StringEncoder");
			ConsumerConfig config = new ConsumerConfig(props);
			ConsumerConnector consumer = kafka.consumer.Consumer.createJavaConsumerConnector(config);

			Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
			topicCountMap.put(topic, new Integer(concurrent));

			StringDecoder keyDecoder = new StringDecoder(new VerifiableProperties());
			StringDecoder valueDecoder = new StringDecoder(new VerifiableProperties());

			Map<String, List<KafkaStream<String, String>>> consumerMap = consumer.createMessageStreams(topicCountMap, keyDecoder, valueDecoder);
			for (KafkaStream<String, String> kafkaStream : consumerMap.get(topic)) {
				executors.submit(() -> {
					InfoGen_Consume_Handle newInstance = null;
					try {
						newInstance = clazz.newInstance();
					} catch (InstantiationException | IllegalAccessException e) {
						LOGGER.error("#实例化InfoGen_Consume_Handle失败:", e);
					}
					newInstance.handle_event(kafkaStream.iterator());
				});
			}
		});
		thread.setDaemon(true);
		thread.start();

	}
}
