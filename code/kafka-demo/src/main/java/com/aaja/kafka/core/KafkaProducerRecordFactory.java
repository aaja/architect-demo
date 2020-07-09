package com.aaja.kafka.core;

import org.apache.kafka.clients.producer.ProducerRecord;

/**
 * 创建kafka的消息对象
 */
public class KafkaProducerRecordFactory {

	/**
	 * 默认不传key采用Round-Robin负载均衡算法进行kafka分区的定位
	 */
	public static ProducerRecord<String, String> createProducerRecord(
			String topic, String value) {
    	return new ProducerRecord<>(topic, value);
    }
}
