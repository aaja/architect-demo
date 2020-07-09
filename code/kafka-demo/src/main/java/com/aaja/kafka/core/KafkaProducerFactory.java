package com.aaja.kafka.core;

import com.aaja.kafka.util.KafkaPropsUtil;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

/*
 * @describe 从elasticsearch加载kafka初始化配置、并构建KafkaProducerFactory
 */
public class KafkaProducerFactory {

	public static final String PROPERTIES_INDEX_ID = "1"; // _id
	private static Logger logger = Logger.getLogger(KafkaProducerFactory.class);
	
	public static Producer<String,String> createProducer() throws IOException {
		Properties props = loadProperties();
		logger.info("配置文件加载完毕！");
        return new KafkaProducer<String,String>(props);
    }
	
	private static Properties loadProperties() throws IOException {
		Properties props = new Properties();
        props.setProperty(ProducerConfig.ACKS_CONFIG,"all");
        props.setProperty(ProducerConfig.RETRIES_CONFIG,"1");
        props.setProperty(ProducerConfig.BATCH_SIZE_CONFIG,"16384");
        props.setProperty(ProducerConfig.LINGER_MS_CONFIG, "0");
        props.setProperty(ProducerConfig.BUFFER_MEMORY_CONFIG,"33554432");
        props.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4"); // 压缩方式、消费端自动解压缩
        props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer");
        props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer");
        return props;
	}

}
