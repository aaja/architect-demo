package com.aaja.kafka.core;

import java.io.IOException;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import org.apache.log4j.Logger;

public class KafkaConsumerFactory {

	private static Logger log = Logger.getLogger(KafkaConsumerFactory.class);
	
	private static String MAX_POLL_RECORDS_CONFIG = "1000";
	
	public static KafkaConsumer<String,String> createConsumer() throws IOException {
		return createConsumer(MAX_POLL_RECORDS_CONFIG);
	}
	
	public static KafkaConsumer<String,String> createConsumer(String maxPoll) throws IOException {
		Properties consumerProp = loadProperties(maxPoll);
		log.info("kafka consumer 配置文件加载完毕!");
		return new KafkaConsumer<String,String>(consumerProp);
	}
	
	/**
	* @Title: createConsumerSJTD
	* @Description: 创建数据投递kafka消费者
	* @param maxPoll
	* @throws IOException    参数
	* @return KafkaConsumer<String,String>    返回类型
	*/
	public static KafkaConsumer<String,String> createConsumerSJTD(String maxPoll,String sfnw) 
			throws IOException {
		Properties consumerProp = loadProperties(maxPoll);
		consumerProp.setProperty(ConsumerConfig.GROUP_ID_CONFIG,"jkpt-transfer-group-test");
		log.info("kafka consumer 配置文件加载完毕!");
		return new KafkaConsumer<String,String>(consumerProp);
	}
	
	public static KafkaConsumer<String,String> createConsumerJKDP(String maxPoll)
			throws IOException {
		Properties consumerProp = loadProperties(maxPoll);
		consumerProp.setProperty(ConsumerConfig.GROUP_ID_CONFIG,"jkpt-transfer-group-jkdp");
		log.info("kafka consumer 配置文件加载完毕!");
		return new KafkaConsumer<String,String>(consumerProp);
	}
	
	private static Properties loadProperties(String maxPoll)
			throws IOException {
		Properties props = new Properties();
		props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9999");
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG,"jkpt-transfer-group");
        props.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,"true");
        props.setProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG,"1000");
        props.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPoll);
        props.setProperty(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "600000");
        props.setProperty(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "120000");
        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringDeserializer");
        return props;
	}
	
}
