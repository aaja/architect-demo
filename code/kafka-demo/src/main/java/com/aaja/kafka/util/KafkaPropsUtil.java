package com.aaja.kafka.util;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * <pre>
 * kafka配置文件工具类
 * Created by aaja on 2019/7/19.
 * </pre>
 */
public class KafkaPropsUtil {
	
	private static Logger logger = Logger.getLogger(KafkaPropsUtil.class);
	private static final String KAFKA_BOOTSTRAP_SERVERS    					   = "bootstrap.servers";
	// producer
	private static final String PRODUCER_ACKS 								   = "acks";
	private static final String PRODUCER_RETRIES 							   = "retries";
	private static final String PRODUCER_BATCH_SIZE				 			   = "batch.size";
	private static final String PRODUCER_LINGER_MS 				 			   = "linger.ms";
	private static final String PRODUCER_BUFFER_MEMORY 			 			   = "buffer.memory";
	private static final String PRODUCER_KEY_SERIALIZER 			 		   = "key.serializer";
	private static final String PRODUCER_VALUE_SERIALIZER					   = "value.serializer";
	private static final String PRODUCER_MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION = "max.in.flight.requets.per.connection";
	private static final String PRODUCER_RETRY_BACKOFF_MS          			   = "retry.backoff.ms";
	private static final String PRODUCER_MAX_BLOCK_MS              			   = "max.block.ms";
	private static final String PRODUCER_COMPRESSION_TYPE          			   = "compression.type";
	private static final String PRODUCER_MAX_REQUEST_SIZE         			   = "max.request.size";
	private static final String PRODUCER_REQUEST_TIMEOUT_MS        			   = "request.timeout.ms";
	private static final String PRODUCER_PARTITIONER_CLASS         			   = "partitioner.class";
	private static final String PRODUCER_INTERCEPTOR_CLASSES       			   = "interceptor.classes";
	// consumer
	private static final String CONSUMER_PROPS_GROUP_ID 					   = "group.id";
	private static final String CONSUMER_ENABLE_AUTOCOMMIT					   = "enable.auto.commit";
	private static final String CONSUMER_AUTOCOMMIN_INTERVAL_MS 			   = "auto.commit.interval.ms";
	private static final String CONSUMER_KEY_DESERIALIZER 					   = "key.deserializer";
	private static final String CONSUMER_VALUE_DESERIALIZER		 			   = "value.deserializer";
	private static final String CONSUMER_MAX_POLL_RECORDS					   = "max.poll.records";
	private static final String CONSUMER_AUTO_OFFSET_RESET       			   = "auto.offset.reset";
	private static final String CONSUMER_SESSION_TIMEOUT_MS      			   = "session.timeout.ms";
	private static final String CONSUMER_MAX_POLL_INTERVAL_MS   			   = "max.poll.interval.ms";
	private static final String CONSUMER_FETCH_MAX_BYTES        			   = "fetch.max.bytes";
	private static final String CONSUMER_HEARTBEAT_INTERVAL_MS  			   = "heartbeat.interval.ms";
	private static final String CONSUMER_CONNECTION_MAX_IDLE_MS  			   = "connections.max.idle.ms";
	
	private static volatile Properties $mp = new Properties();
	private static volatile boolean isLoad = false;
	static synchronized void loadp() {
		if(isLoad) return;
		InputStream is = KafkaPropsUtil.class.getResourceAsStream("/kafka.properties");
		try {
			BufferedReader bf = new BufferedReader(new InputStreamReader(is,"UTF-8"));
			$mp.load(bf);
		} catch(Exception e){
			logger.error("加载配置文件:kafka.properties失败，错误信息为：",e);
		} finally {
			if(is!=null) {
				try {
					is.close();
				} catch (IOException e) {
					logger.error("关闭kafka.properties文件流失败，错误信息为：",e);
				}
			}
		}
		isLoad = true;
		logger.info("加载监控配置文件:kafka.properties成功！");
	}
	
	private static String getKafkaProperty(String key) {
		if(!isLoad) {
			loadp();
		}
		return $mp.getProperty(key);
	}

	public static String getBrokerList(){
		return getKafkaProperty(KAFKA_BOOTSTRAP_SERVERS);
	}

	public static String getGroupId(){
		return getKafkaProperty(CONSUMER_PROPS_GROUP_ID);
	}
	
	public static String getAcks() {
		return getKafkaProperty(PRODUCER_ACKS);
	}
	
	public static String getRetries() {
		return getKafkaProperty(PRODUCER_RETRIES);
	}
	
	public static String getBatchSize() {
		return getKafkaProperty(PRODUCER_BATCH_SIZE);
	}
	
	public static String getLingerMs() {
		return getKafkaProperty(PRODUCER_LINGER_MS);
	}
	
	public static String getBufferMemory() {
		return getKafkaProperty(PRODUCER_BUFFER_MEMORY);
	}
	
	public static String getKeySerializer() {
		return getKafkaProperty(PRODUCER_KEY_SERIALIZER);
	}
	
	public static String getValueSerializer() {
		return getKafkaProperty(PRODUCER_VALUE_SERIALIZER);
	}
	
	public static String getEnableAutoCommit() {
		return getKafkaProperty(CONSUMER_ENABLE_AUTOCOMMIT);
	}
	
	public static String getAutoCommitIntervalMs() {
		return getKafkaProperty(CONSUMER_AUTOCOMMIN_INTERVAL_MS);
	}
	
	public static String getKeyDeserializer() {
		return getKafkaProperty(CONSUMER_KEY_DESERIALIZER);
	}
	
	public static String getValueDeserializer() {
		return getKafkaProperty(CONSUMER_VALUE_DESERIALIZER);
	}
	
	public static String getMaxPollRecords() {
		return getKafkaProperty(CONSUMER_MAX_POLL_RECORDS);
	}
	
	public static String getMaxInFlightRequestPerConnection() {
		return getKafkaProperty(PRODUCER_MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION);
	}
	
	public static String getRetryBackoffMs() {
		return getKafkaProperty(PRODUCER_RETRY_BACKOFF_MS);
	}
	
	public static String getMaxBlockMs() {
		return getKafkaProperty(PRODUCER_MAX_BLOCK_MS);
	}
	
	public static String getCompressionType() {
		return getKafkaProperty(PRODUCER_COMPRESSION_TYPE);
	}
	
	public static String getMaxRequestSize() {
		return getKafkaProperty(PRODUCER_MAX_REQUEST_SIZE);
	}
	
	public static String getRequestTimeoutMs() {
		return getKafkaProperty(PRODUCER_REQUEST_TIMEOUT_MS);
	}
	
	public static String getPartitionerClazz() {
		return getKafkaProperty(PRODUCER_PARTITIONER_CLASS);
	}
	
	public static String getInterceptorClazz() {
		return getKafkaProperty(PRODUCER_INTERCEPTOR_CLASSES);
	}
	
	public static String getAutoOffsetReset() {
		return getKafkaProperty(CONSUMER_AUTO_OFFSET_RESET);
	}
	
	public static String getSessionTimeoutMs() {
		return getKafkaProperty(CONSUMER_SESSION_TIMEOUT_MS);
	}
	
	public static String getMaxPollIntervalMs() {
		return getKafkaProperty(CONSUMER_MAX_POLL_INTERVAL_MS);
	}
	
	public static String getFetchMaxBytes() {
		return getKafkaProperty(CONSUMER_FETCH_MAX_BYTES);
	}
	
	public static String getHeartbeatIntervalMs() {
		return getKafkaProperty(CONSUMER_HEARTBEAT_INTERVAL_MS);
	}
	
	public static String getConnectionMaxIdleMs() {
		return getKafkaProperty(CONSUMER_CONNECTION_MAX_IDLE_MS);
	}

	public static String getTopic() {
		return "test";
	}
}
