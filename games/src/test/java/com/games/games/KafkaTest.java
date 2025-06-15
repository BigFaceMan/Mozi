package com.games.games;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.TimeUnit;


@SpringBootTest
public class KafkaTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    public void testKafkaSendAndReceive() throws Exception {
        String topic = "springKafka";
        String message = "hello-kafka-test";

        kafkaTemplate.send(topic, "123", message);

        // 最多等待 5 秒接收消息
        String received = KafkaListenerService.messages.poll(5, TimeUnit.SECONDS);

        System.out.println("测试接收到消息: " + received);
//        assertEquals(message, received);
    }
}
