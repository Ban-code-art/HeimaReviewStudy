package com.itheima.springboot2redis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
public class StringRedisTemplateTest {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void settest() {
        stringRedisTemplate.opsForValue().set("key", "value");
    }

    @Test
    void gettest() {
        System.out.println(stringRedisTemplate.opsForValue().get("key"));
        System.out.println(stringRedisTemplate.opsForValue().get("key").toString());
    }
}
