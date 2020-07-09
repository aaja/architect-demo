package com.aaja.kafka.core;

import java.util.List;
import java.util.Set;

import com.css.sword.jkpt.common.Public_Const;
import com.css.sword.jkpt.common.transfer.pojo.Topic;
import com.css.sword.kernel.base.exception.SwordBaseCheckedException;
import com.css.sword.kernel.utils.SwordServiceUtils;

/**
 * kafka客户端topic管理API
 * @author sujiujun
 * @dete   2019/7/19
 */
public class KafkaTopicManager {

	/**
	 * 查询topic是否存在
	 * @param topic	
	 * @return
	 */
	public static boolean queryTopicByName(Topic topic) {
		boolean createFlag = false;
		try {
			createFlag = (boolean)SwordServiceUtils
					.callService(Public_Const.SERVICE_CSYQ_HEAD 
							+ "TOPIC_QUERY", new Object[] {topic});
		} catch (SwordBaseCheckedException e) {
			e.printStackTrace();
		}
		return createFlag;
	}
	
	/**
	 * 创建topic
	 * @param ip		broker节点的IP
	 * @param port		broker节点的端口
	 * @param topics	用list保存topic、可以同时创建多个topic	
	 * @return
	 */
	public static boolean createTopic(List<Topic> topics) {
		boolean createFlag = false;
		try {
			createFlag = (boolean)SwordServiceUtils
					.callService(Public_Const.SERVICE_CSYQ_HEAD 
							+ "TOPIC_CREATE", new Object[] {topics});
		} catch (SwordBaseCheckedException e) {
			e.printStackTrace();
		}
		return createFlag;
	}

	/**
	 * 删除topic
	 * @param ip		broker节点的IP
	 * @param port		broker节点的port
	 * @param topics	要删除的topic名字列表
	 */
	public static void deleteTopic(Set<String> topics) {
		try {
			SwordServiceUtils.callService(Public_Const.SERVICE_CSYQ_HEAD 
					+ "TOPIC_DELETE" , new Object[] {topics});
		} catch (SwordBaseCheckedException e) {
			e.printStackTrace();
		}
	}
}
