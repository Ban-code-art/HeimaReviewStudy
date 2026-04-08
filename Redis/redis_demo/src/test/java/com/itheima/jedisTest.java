package com.itheima;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;

public class jedisTest {
    private Jedis jedis;
    //这个注解表示在每个测试方法执行前执行setup方法
    @BeforeEach
    void setup(){
        //连接redis
//        jedis = new Jedis("localhost",6379);
        jedis = JedisConnectionFactory.getJedis();
        //选择数据库为0号数据库
        jedis.select(0);


    }
//    测试stirng类型的redis
    @Test
    public void testJedis(){

        System.out.println(jedis);
        jedis.set("name1","itcastima");
        System.out.println(jedis.get("name1"));
    }

//    测试list类型的redis
    @Test
    public void testList(){
        System.out.println(jedis);
        jedis.lpush("ListTest","itcastima");
        System.out.println(jedis.lrange("list1",0,-1));
    }
//    测试hash类型redis
    @Test
    public void testHash(){
        jedis.hset("HashTest","name","itcastima");
        jedis.hset("HashTest","age","18");
        System.out.println(jedis.hgetAll("HashTest"));
    }
    //这个注解表示在每个测试方法执行后执行tearDown方法
    @AfterEach
    void tearDown(){
        if(jedis != null){
            jedis.close();
        }
    }
}
