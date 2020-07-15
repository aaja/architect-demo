package com.aaja.es.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class EsConfigUtils {

	private static String configFileName = "elasticsearch.properties";
	
	private static String clusterNodes = "es.cluster.nodes";
	private static String namespace = "es.index.namespace";
	private static String namespaceSkywalking = "skywalking.index.namespace";
	private static String user = "es.username";
	private static String password = "es.password";
	private static String indexShardsNumber = "es.index.shards.number";
	private static String indexReplicasNumber = "es.index.replicas.number";
	
	private static String bulkActions = "es.bulk.actions";
	private static String bulkSize = "es.bulk.size";
	private static String bulkFlushInterval = "es.bulk.flush.interval";
	private static String bulkConcurrentRequests = "es.bulk.concurrent.requests";
	
	public  static final String INDEX_TYPE = "_doc";
	public  static final String INDEX_TYPE_SKYWALKING = "type";
	
	public  static final int metadataQueryMaxSize = 5000;

	/** 计算规则ES指标类索引   */
  	public static String ES_INDEX_INDEX = "tyjkpt_compute_rule_index";
    
  	/** 计算规则ES日志类索引   */
  	public static String ES_INDEX_LOG = "tyjkpt_compute_rule_log";
    
  	/** 计算规则ES应用类索引   */
  	public static String ES_INDEX_TRACE = "tyjkpt_compute_rule_trace";
	
  	public static String KEY_ES_SOURCE = "_source";
	/** 告警规则ES索引 */
	public static String ES_INDEX_GJGZ = "tyjkpt_gjgz_index";
	/** 告警配置 */
	public static String ES_INDEX_GJPZ = "tyjkpt_zbgl_gjpz";
	private static volatile Properties $mp = new Properties();
	private static volatile boolean isLoad = false;
	private static synchronized void loadp() {
		if(isLoad) return;
		InputStream is = EsConfigUtils.class.getResourceAsStream("/"+configFileName);
		try {
			BufferedReader bf = new BufferedReader(new InputStreamReader(is,"UTF-8"));
//			$mp.load(bf);
		} catch(Exception e){
		} finally {
			if(is!=null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
		isLoad = true;
	}
	
	private static String getKafkaProperty(String key) {
		if(!isLoad) {
			loadp();
		}
		return $mp.getProperty(key);
	}

	public static String getClusterNodes() {
		return getKafkaProperty(clusterNodes);
	}

	public static String getNamespace() {
		return getKafkaProperty(namespace);
	}

	public static String getNamespaceSkywalking() {
		return getKafkaProperty(namespaceSkywalking);
	}
	
	public static String getUser() {
		return getKafkaProperty(user);
	}

	public static String getPassword() {
		return getKafkaProperty(password);
	}

	public static String getIndexShardsNumber() {
		return getKafkaProperty(indexShardsNumber);
	}

	public static String getIndexReplicasNumber() {
		return getKafkaProperty(indexReplicasNumber);
	}
	
	public static int getBulkActions() {
		return Integer.parseInt(getKafkaProperty(bulkActions));
	}
	
	public static int getBulkSize() {
		return Integer.parseInt(getKafkaProperty(bulkSize));
	}
	
	public static int getBulkFlushInterval() {
		return Integer.parseInt(getKafkaProperty(bulkFlushInterval));
	}
	
	public static int getBulkConcurrentRequests() {
		return Integer.parseInt(getKafkaProperty(bulkConcurrentRequests));
	}

	public static void main(String[] args) {
		System.out.println(getClusterNodes());
		System.out.println(getNamespace());
		System.out.println(getNamespaceSkywalking());
		System.out.println(getUser());
		System.out.println(getPassword());
		System.out.println(getIndexShardsNumber());
		System.out.println(getIndexReplicasNumber());
	}
	
	
}
