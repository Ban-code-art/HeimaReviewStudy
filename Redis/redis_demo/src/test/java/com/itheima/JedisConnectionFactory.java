package com.itheima;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

public class JedisConnectionFactory {
    private static final JedisPool jedisPool;
    static {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(100);
        config.setMaxIdle(10);
        config.setMinIdle(5);
        config.setMaxWait(Duration.ofDays(10000));
        jedisPool = new JedisPool(config,"localhost",6379);
           }
           public static Jedis getJedis(){
        return jedisPool.getResource();
           }
}
