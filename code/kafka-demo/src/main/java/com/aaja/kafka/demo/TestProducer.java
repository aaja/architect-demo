package com.aaja.kafka.demo;

import com.aaja.kafka.core.TransferChannelEntrance;

import java.io.IOException;
import java.util.HashMap;

/**
 * <p>Title: TestProducer</p>
 * <p>Description: ${}</p>
 *
 * @author aaja
 * @date 2020/7/8 23:33
 */
public class TestProducer {

    public static void main(String[] args) {
        new HashMap<String, String>();
        final String topic = "test_topic";
        String value = "test data ...";
        try {
            while(true){
                TransferChannelEntrance.publish(topic, value);
                Thread.sleep(1000); //1秒钟发送一次消息
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
