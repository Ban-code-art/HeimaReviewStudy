package com.hmdp.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hmdp.entity.Shop;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.events.Event;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.baomidou.mybatisplus.extension.toolkit.Db.getById;
import static com.hmdp.utils.RedisConstants.*;

@Slf4j
@Component
public class CacheClient {
    /*这是一个工具类，用于封装四种不同的方法*/
    @Autowired
    private  StringRedisTemplate stringRedisTemplate;
//    将任意java对象序列化为json并存储在string类型的key中，并且可以设置ttl过期时间
    public void set(String key, Object value, Long time, TimeUnit timeUnit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value),time,timeUnit);
    }
//     将任意Java对象序列化为json并存储在string类型的key中，并且可以设置逻辑过期时间，用于处理缓存击穿问题
    public  void setLogicalExpire(String key, Object value, Long time, TimeUnit timeUnit) {
        RedisData redisData = new RedisData();
        redisData.setData(value);                              //使用timeUnit中的方法将单位转化为秒
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(timeUnit.toSeconds(time)));
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }
//    根据指定的key查询缓存，反序列化为指定的类型，利用缓存空值的方式解决缓存穿透的问题
public <R,ID> R queryWithPassThrough(String keyPrefix, ID id , Class<R> type, Function<ID,R> dbFallback,Long time, TimeUnit timeUnit){//这里的三个形参是key的前缀，id，数据对象的类型
    String key = keyPrefix + id;                                                                        //就是定义的时候参数传了个接口, 用到时传入接口的实现类
//        1.从Redis中查询商铺缓存，这里使用的是string的redis
//        因为店铺是一个对象，所以存储进去的是一个json字符串，key是店铺的id
    String shopJson = stringRedisTemplate.opsForValue().get(key);
//        2.判断是否存在缓存  有具体的内容才会执行if
    if (StrUtil.isNotBlank(shopJson)) {
//            3.如果缓存存在，直接返回缓存中的数据
        R r = JSONUtil.toBean(shopJson, type);//将json字符串转换为shop对象
        return r;
    }
//        判断命中的是否是空值  如果上面的if语句跳过了，后面就是null 或者是“”
    if (shopJson != null) {
//            执行到此步，shopJson若不为null，则是空字符串；为应付缓存穿透，只有商铺不存在的情况下才存储空字符串
        return null;
    }
//        4.如果缓存不存在，从数据库中查询店铺信息
//    R r = getById(id);
    R r = dbFallback.apply(id);
    if (r == null) {
//            5.如果店铺不存在，返回失败结果/
//            为了避免缓存穿透，将空值写入redis
        stringRedisTemplate.opsForValue().set(key,"",time, timeUnit);
        return null;
    }
//            6.如果店铺存在，将店铺信息缓存到Redis中
    stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(r),time,timeUnit);//将shop对象转换为json字符串并缓存
//            7.返回店铺信息
    return r;

}

//      根据指定的key查询缓存，并反序列化为指定类型，需要利用逻辑过期解决缓存击穿问题
public <R,ID> R queryWithLogicalExpire(String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit timeUnit){
    String key = keyPrefix + id;

//        1.从Redis中查询商铺缓存，这里使用的是string的redis
//        因为店铺是一个对象，所以存储进去的是一个json字符串，key是店铺的id
    String shopJson = stringRedisTemplate.opsForValue().get(key);
//        2.判断是否存在缓存
    if (StrUtil.isBlank(shopJson)) {
//            3.如果缓存不存在直接返回空
        return null;
    }

//        4.命中，需要先把json反序列化为对象
    RedisData redisData = JSONUtil.toBean(shopJson, RedisData.class);
//        Object data = redisData.getData();//这三步是什么意思，为什么拿到的数据先转为JSONObject然后又转为shop类型对象
//        JSONObject jsonObject = (JSONObject) data;
//        Shop shop = JSONUtil.toBean(jsonObject, Shop.class);
    R r = JSONUtil.toBean((JSONObject) redisData.getData(), type);
    LocalDateTime expireTime = redisData.getExpireTime();//拿到自定义的逻辑过期时间
//        5.判断是否过期
    if (expireTime.isAfter(LocalDateTime.now())) {
        //5.1未过期，直接返回店铺信息
        return r;
    }

//        5.2已过期，需要缓存重建
//        6.缓存重建
//        6.1获取互斥锁
    String lockKey = LOCK_SHOP_KEY + id;
    boolean isLock = tryLock(lockKey);
//        6.2判断获取互斥锁是否成功
    if (isLock) {
        CACHE_REBUILD_EXECUTOR.submit(()->{
            try {
//                从数据库中查询数据
                R apply = dbFallback.apply(id);
//                写入redis
                setLogicalExpire(key, apply, time, timeUnit);

            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                unlock(lockKey);//释放锁，为什么要释放锁
            }
        });
    }
//        6.3成功，开启独立线程，实现缓存重建
//        6.4返回过期店铺信息
//            6.如果店铺存在，将店铺信息缓存到Redis中
    //       stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop),CACHE_SHOP_TTL, TimeUnit.MINUTES);//将shop对象转换为json字符串并缓存
//            7.返回店铺信息
    return r;

}
    //⭐定义一个方法获取锁setnx（互斥锁相关代码）
    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.MINUTES);
        //网络问题或键不存在但 Redis 未响应，setIfAbsent 可能会返回 null，所以要使用工具类转化成基本数据类型boolean
        return BooleanUtil.isTrue(flag);
    }
    //⭐定义一个方法释放锁（互斥锁相关代码）
    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

}
