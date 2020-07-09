package com.aaja.kafka.demo;

import com.aaja.kafka.core.TransferChannelExit;

import java.io.IOException;
import java.time.Duration;

/**
 * <p>Title: TestConsumer</p>
 * <p>Description: ${}</p>
 *
 * @author aaja
 * @date 2020/7/8 23:33
 */
public class TestConsumer {

    public static void main(String[] args) {
        final String topic = "test_topic";

        try {
            TransferChannelExit.subscribe(Duration.ofMillis(1000), topic, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
