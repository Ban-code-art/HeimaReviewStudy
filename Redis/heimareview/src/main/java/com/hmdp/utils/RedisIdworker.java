package com.hmdp.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.quartz.LocalDataSourceJobStore;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class RedisIdworker {
    /*
    * 序列号位数
    * */
    private static final int COUNT_BITS = 32;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    public long nextId(String keyPrefix) {


//        1.生成时间戳
        LocalDateTime now = LocalDateTime.now();//获取当前时间
        long timestamp = now.toEpochSecond(ZoneOffset.UTC);//将时间转换为时间戳，utc时区
//        2.生成序列号

//        2.1获取当前日期，精确到天
        String data = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
//        2.2自增长
        Long count = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + data);
        return timestamp << COUNT_BITS | count;//将时间戳和序列号合并，返回
    }
}
