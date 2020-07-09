package com.aaja.kafka.core;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 传输通道-入口
 */
public class TransferChannelExit {

	/**
	 * 传输层通道出口
	 * @param consumer
	 * @param Millis
	 * @return
	 * @throws IOException
	 */
	public static ConsumerRecords<String, String> subscribe(Duration Millis, String topic,KafkaConsumer<String,String> consumer) throws IOException {
		consumer.subscribe(Arrays.asList(topic)); // 订阅主题
		return subscribe(Millis, consumer);
	}
	
	public static ConsumerRecords<String, String> subscribe(Duration Millis, KafkaConsumer<String,String> consumer) throws IOException {
		Map<String, List<PartitionInfo>> listTopics = consumer.listTopics();
		if(listTopics != null && listTopics.size() > 0) {
			return consumer.poll(Millis);
		}
		return null;
	}
}
