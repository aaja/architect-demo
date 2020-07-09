package com.aaja.kafka.core;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Set;

import com.aaja.es.util.ElasticSearchClient;
import com.aaja.es.util.EsConfigUtils;
import org.apache.log4j.Logger;

/**
 * 采集引擎获取各自的topic的公共方法
 */
public class KafkaTopicFactory {

	private static Logger log = Logger.getLogger(KafkaConsumerFactory.class);
	
	private static ElasticSearchClient client            =  null;
	private static final String INDEX_TOPIC              =  "topic";
	private static final String INDEXID_TOPIC_RZJK       =  "cjyq_rzjk";
	private static final String INDEXID_TOPIC_ZJJK       =  "cjyq_zjjk";
	private static final String INDEXID_TOPIC_YYJK       =  "cjyq_yyjk";
	private static final String INDEXID_TOPIC_YYJK_PRO   =  "cjyq_zjjk_prometheus";
	private static final String INDEXID_TOPIC_STORAGE_ZJJK_DRUID =  "druidStorage";
	private static final String INDEXID_TOPIC_INDEX =  "jsyq_index";
	private static final String INDEXID_TOPIC_JSYQ_TRACE = "jsyq_trace";
	
	private static final String INDEXID_TOPIC_STORAGE_RZJK_ES = "logStorage";
	
	private static final String INDEXID_TOPIC_STORAGE_YYJK_ES = "traceStorage";
	
	private static ElasticSearchClient getClient() throws IOException {
		if(client==null) {
			client = new ElasticSearchClient(EsConfigUtils.getClusterNodes(),
					EsConfigUtils.getNamespace(),
					EsConfigUtils.getUser(),
					EsConfigUtils.getPassword(),
					EsConfigUtils.INDEX_TYPE);
			client.connect();
			log.debug("KafkaTopicFactory创建client 成功~");
		}
		return client;
	}
	
	public static String getYyjkStorageTopic() throws IOException {
		return getByEs(getClient(),INDEXID_TOPIC_STORAGE_YYJK_ES);
	}
	
	private static String getByEs(ElasticSearchClient client, String id) throws IOException {
//		GetResponse response = client.get("tyjkpt_topic_manager", id);
        String topic = null;
//		if (response.isExists()) {
//        	Set<Entry<String, Object>> entrySet = response.getSource().entrySet();
//        	for(Entry<String, Object> entry : entrySet) {
//        		if(INDEX_TOPIC.equals(entry.getKey())) {
//        			topic = entry.getValue().toString();
//        		}
//        	}
//        }
		return topic;
    }
}
