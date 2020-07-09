package com.aaja.kafka.core;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.errors.RetriableException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.time.Duration;

/**
 * 传输通道 - 出口
 */
public class TransferChannelEntrance {

	private static Logger logger = Logger.getLogger(TransferChannelEntrance.class);

	/**
	 * 传输层流式数据入口方法
	 * @param topic		topic名称
	 * @param value		消息体(json字符串格式)
	 * @throws IOException
	 */
    public static void publish(String topic, String value) throws IOException {
    	Producer<String,String> producer = KafkaProducerFactory.createProducer();
    	ProducerRecord<String,String> record = KafkaProducerRecordFactory.createProducerRecord(topic, value);
    	producer.send(record, new Callback() {
    		@Override
            public void onCompletion(RecordMetadata metadata, Exception exception) {
                if (exception == null) {
//                    logger.debug("Sent ok: " + record + ", metadata: " + metadata);
                } else {
                	producer.close(Duration.ZERO); // 防止消息乱序、不把失败的消息发送出去
                	if(exception instanceof RetriableException) {
                		logger.debug("消息发送失败，处理可重试瞬时异常， " + record, exception);
                	} else {
                        logger.debug("消息发送失败，处理不可重试异常，" + record, exception);
                	}
                }
            }
        });
    	producer.close(); // 释放资源
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static void publish(Producer producer, String topic, String value,Callback callback) throws IOException {
    	ProducerRecord record = KafkaProducerRecordFactory.createProducerRecord(topic, value);
    	producer.send(record, callback);
    }

	public static void publish(Producer producer,String topic, String value) throws IOException {
		ProducerRecord<String,String> record = KafkaProducerRecordFactory.createProducerRecord(topic, value);
		producer.send(record, new Callback() {
			@Override
			public void onCompletion(RecordMetadata metadata, Exception exception) {
				if (exception == null) {
//                    logger.debug("Sent ok: " + record + ", metadata: " + metadata);
				} else {
					producer.close(Duration.ZERO); // 防止消息乱序、不把失败的消息发送出去
					if(exception instanceof RetriableException) {
						logger.debug("消息发送失败，处理可重试瞬时异常， " + record, exception);
					} else {
						logger.debug("消息发送失败，处理不可重试异常，" + record, exception);
					}
				}
			}
		});
	}
}
