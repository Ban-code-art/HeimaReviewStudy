package com.itheima;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itheima.Redis.pojo.student;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
class RedisSpringbootTest1ApplicationTests {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedisTemplate redisTemplate;
    private static final ObjectMapper mapper = new ObjectMapper();
    @Test
    void testString() throws JsonProcessingException {
//        redisTemplate.opsForValue().set("StringTest", "itcast");
//        定义一个对象
        student stu = new student("itcastheimalast", 19);
        student stu1 = new student("itcastheimalast", 19);
//        手动将这个对象进行序列化操作
        String stuJson = mapper.writeValueAsString(stu);
//        添加
        stringRedisTemplate.opsForValue().set("StringTest", stuJson);
        redisTemplate.opsForValue().set("StringTest1", stu1);
//        从Redis中获取数据
        stringRedisTemplate.opsForValue().get("StringTest");
//        手动将这个字符串进行反序列化操作
        student stuget = mapper.readValue(stringRedisTemplate.opsForValue().get("StringTest"), student.class);
        Object o = redisTemplate.opsForValue().get("StringTest1");
        student stuget1 = (student) o;
        System.out.println(stuget);
        System.out.println(stuget1);
    }

    @Test
    void testList() {
        stringRedisTemplate.opsForList().leftPush("ListTest", "itcastima");
        System.out.println(stringRedisTemplate.opsForList().range("ListTest", 0, 1));
    }

    @Test
    void testSet() {
        stringRedisTemplate.opsForSet().add("SetTest", "itcastima");
        System.out.println(stringRedisTemplate.opsForSet().members("SetTest"));
    }

    @Test
    void testHash() {
        stringRedisTemplate.opsForHash().put("HashTest", "name", "itcastima");
        System.out.println(stringRedisTemplate.opsForHash().get("HashTest", "name"));
    }

}
