package com.itheima.springboot2redis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@SpringBootTest
class Springboot2RedisApplicationTests {
    @Autowired
    private RedisTemplate redisTemplate;
    @Test
    void setRedisTemplate() {
        ValueOperations ops = redisTemplate.opsForValue();
        ops.set("key", "value");

    }

    @Test
    void getRedisTemplate() {
        ValueOperations ops = redisTemplate.opsForValue();
        System.out.println(ops.get("key"));
    }
    @Test
    void hsetRedisTemplate() {
        HashOperations ops = redisTemplate.opsForHash();
        ops.put("keycode", "valuecode","valuecodes");

    }
    @Test
    void hgetRedisTemplate() {
        HashOperations ops = redisTemplate.opsForHash();
        System.out.println(ops.get("keycode", "valuecode"));

    }

}
