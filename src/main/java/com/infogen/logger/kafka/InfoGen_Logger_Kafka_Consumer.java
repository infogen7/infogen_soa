/**
 * 
 */
package com.infogen.logger.kafka;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.serializer.StringDecoder;
import kafka.utils.VerifiableProperties;

import com.infogen.configuration.InfoGen_Configuration;
import com.infogen.logger.kafka.event_handle.InfoGen_Logger_Handle_Consume;

/**
 * kafka消费者的简单封装
 * 
 * @author larry
 * @email larrylv@outlook.com
 * @version 创建时间 2015年1月12日 下午6:38:29
 */
public class InfoGen_Logger_Kafka_Consumer {

	public static void consume(InfoGen_Configuration infogen_configuration, String group, String topic, InfoGen_Logger_Handle_Consume handle) {
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
				props.put("auto.offset.reset", "smallest");
				// 序列化类
				props.put("serializer.class", "kafka.serializer.StringEncoder");
				ConsumerConfig config = new ConsumerConfig(props);
				ConsumerConnector consumer = kafka.consumer.Consumer.createJavaConsumerConnector(config);

				Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
				topicCountMap.put(topic, new Integer(1));

				StringDecoder keyDecoder = new StringDecoder(new VerifiableProperties());
				StringDecoder valueDecoder = new StringDecoder(new VerifiableProperties());

				Map<String, List<KafkaStream<String, String>>> consumerMap = consumer.createMessageStreams(topicCountMap, keyDecoder, valueDecoder);
				KafkaStream<String, String> stream = consumerMap.get(topic).get(0);
				ConsumerIterator<String, String> it = stream.iterator();
				while (it.hasNext()) {
					handle.handle_event(it.next().message());
				}
			});
		thread.setDaemon(true);
		thread.start();

	}
}
