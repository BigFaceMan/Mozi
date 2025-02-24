package com.games.games;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
public class RedisTests {
    @Autowired
    private RedisTemplate redisTemplate;
    @Test
    void testString() {
        redisTemplate.opsForValue().set("age", 19);
        Object age = redisTemplate.opsForValue().get("age");
        System.out.println("age : " + age);
//        System.out.println("hello world");
    }
}
