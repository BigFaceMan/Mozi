package com.games.games;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class KafkaListenerService {

    // 用于测试中验证是否接收到消息
    public static final BlockingQueue<String> messages = new LinkedBlockingQueue<>();

    @KafkaListener(topics = "springKafka", groupId = "test-group")
    public void listen(ConsumerRecord<String, String> record) {
        System.out.println("接收到消息: " + record.value());
        messages.add(record.value());
    }
}
