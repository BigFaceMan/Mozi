package com.games.games;

import com.games.games.consumer.ResourceInfo;
import com.games.games.consumer.SignUpdateGame;
import com.games.games.service.impl.trainServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.Map;

@SpringBootTest
class GamesApplicationTests {
    @Autowired
    private RedisTemplate redisTemplate;
    @Test
    void testString() {
//        redisTemplate.opsForValue().set("age", 18);
        Object age = redisTemplate.opsForValue().get("name");
        System.out.println("age : " + age);
//        System.out.println("hello world");
    }
}
