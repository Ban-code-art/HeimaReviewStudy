package com.hmdp.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.CacheClient;
import com.hmdp.utils.RedisData;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.smartcardio.CardChannel;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {
@Resource
private StringRedisTemplate stringRedisTemplate;
@Resource
private CacheClient cacheClient;
    @Override
    public Result queryById(Long id) {
/*        String key = CACHE_SHOP_KEY + id;
//        1.从Redis中查询商铺缓存，这里使用的是string的redis
//        因为店铺是一个对象，所以存储进去的是一个json字符串，key是店铺的id
        String shopJson = stringRedisTemplate.opsForValue().get(key);
//        2.判断是否存在缓存
        if (StrUtil.isNotBlank(shopJson)) {
//            3.如果缓存存在，直接返回缓存中的数据
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);//将json字符串转换为shop对象
            return Result.ok(shop);
        }
//        判断命中的是否是空值  如果上面的if语句跳过了，后面就是null 或者是“”
        if (shopJson != null) {
//            执行到此步，shopJson若不为null，则是空字符串；为应付缓存穿透，只有商铺不存在的情况下才存储空字符串
            return Result.fail("店铺不存在");
               }
//        4.如果缓存不存在，从数据库中查询店铺信息
        Shop shop = getById(id);
        if (shop == null) {
//            5.如果店铺不存在，返回失败结果/
//            为了避免缓存穿透，将空值写入redis
            stringRedisTemplate.opsForValue().set(key,"",CACHE_NULL_TTL, TimeUnit.MINUTES);
            return Result.fail("店铺不存在");
        }
//            6.如果店铺存在，将店铺信息缓存到Redis中
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop),CACHE_SHOP_TTL, TimeUnit.MINUTES);//将shop对象转换为json字符串并缓存
//            7.返回店铺信息
        return Result.ok(shop);*/
        //判断缓存穿透
//        Shop shop = queryWithPassThrough(id);
//        if (shop == null) {
//            return Result.fail("店铺不存在");
//        }

//        使用互斥锁解决缓存击穿
//        Shop shop = queryWithMutex(id);

//        使用逻辑过期来解决缓存击穿
//        Shop shop = queryWithLogicalExpire(id);
        Shop shop = cacheClient.queryWithLogicalExpire(CACHE_SHOP_KEY, id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.MINUTES);

        if (shop == null) {
            return Result.fail("店铺不存在或者过期");
        }
//        返回
        return Result.ok(shop);
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateShop(Shop shop) {
        Long id = shop.getId();
        if (id==null) {
            return Result.fail("店铺不存在");
        }
//        更新数据库
        updateById(shop);

//        删除缓存
        stringRedisTemplate.delete(CACHE_SHOP_KEY + id);
        return Result.ok();
    }

    //⭐定义一个线程池 10个线程
    private final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    //⭐缓存穿透的方法代码 写入“”
    public Shop queryWithPassThrough(Long id ){
        String key = CACHE_SHOP_KEY + id;
//        1.从Redis中查询商铺缓存，这里使用的是string的redis
//        因为店铺是一个对象，所以存储进去的是一个json字符串，key是店铺的id
        String shopJson = stringRedisTemplate.opsForValue().get(key);
//        2.判断是否存在缓存  有具体的内容才会执行if
        if (StrUtil.isNotBlank(shopJson)) {
//            3.如果缓存存在，直接返回缓存中的数据
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);//将json字符串转换为shop对象
            return shop;
        }
//        判断命中的是否是空值  如果上面的if语句跳过了，后面就是null 或者是“”
        if (shopJson != null) {
//            执行到此步，shopJson若不为null，则是空字符串；为应付缓存穿透，只有商铺不存在的情况下才存储空字符串
            return null;
        }
//        4.如果缓存不存在，从数据库中查询店铺信息
        Shop shop = getById(id);
        if (shop == null) {
//            5.如果店铺不存在，返回失败结果/
//            为了避免缓存穿透，将空值写入redis
            stringRedisTemplate.opsForValue().set(key,"",CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }
//            6.如果店铺存在，将店铺信息缓存到Redis中
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop),CACHE_SHOP_TTL, TimeUnit.MINUTES);//将shop对象转换为json字符串并缓存
//            7.返回店铺信息
        return shop;

    }


//    ⭐这个是互斥锁的方法代码
    public Shop queryWithMutex(Long id ){
        String key = CACHE_SHOP_KEY + id;
//        1.从Redis中查询商铺缓存，这里使用的是string的redis
//        因为店铺是一个对象，所以存储进去的是一个json字符串，key是店铺的id
        String shopJson = stringRedisTemplate.opsForValue().get(key);
//        2.判断是否存在缓存
        if (StrUtil.isNotBlank(shopJson)) {
//            3.如果缓存存在，直接返回缓存中的数据
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);//将json字符串转换为shop对象
            return shop;
        }
//        判断命中的是否是空值  如果上面的if语句跳过了，后面就是null 或者是“”
        if (shopJson != null) {
//            执行到此步，shopJson若不为null，则是空字符串；为应付缓存穿透，只有商铺不存在的情况下才存储空字符串
            return null;
        }
//        4.如果缓存不存在，从数据库中查询店铺信息 |
//        缓存未命中，实现互斥锁功能，实现缓存重建


//        4.1获取互斥锁
        String lockKey = "lock:shop:" + id;
        Shop shop = null;
        try {
            boolean isLock = tryLock(lockKey);
//         4.2判断是否获取成功
            if (!isLock) {
    //            4.3失败则休眠并重试
                Thread.sleep(50);
                queryWithMutex(id);
            }
//
            shop = getById(id);
//            模拟重建延时 这里是为了更好的模拟而设置
//            Thread.sleep(200);
            if (shop == null) {
    //            5.如果店铺不存在，返回失败结果/
    //            为了避免缓存穿透，将空值写入redis
                stringRedisTemplate.opsForValue().set(key,"",CACHE_NULL_TTL, TimeUnit.MINUTES);
                return null;
            }
//            6.如果店铺存在，将店铺信息缓存到Redis中
            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop),CACHE_SHOP_TTL, TimeUnit.MINUTES);//将shop对象转换为json字符串并缓存
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            // 7.释放互斥锁
            unlock(lockKey);
        }


//            8.返回店铺信息
        return shop;

    }

//    ⭐这个方法是用来处理逻辑过期方式的代码，和互斥锁方法同一级别 疑问：1.为什么使用了逻辑过期就不需要考虑缓存穿透问题了，直接删掉缓存穿透的代码
    public Shop queryWithLogicalExpire(Long id ){
        String key = CACHE_SHOP_KEY + id;

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
        Shop shop = JSONUtil.toBean((JSONObject) redisData.getData(), Shop.class);
        LocalDateTime expireTime = redisData.getExpireTime();//拿到自定义的逻辑过期时间
//        5.判断是否过期
        if (expireTime.isAfter(LocalDateTime.now())) {
            //5.1未过期，直接返回店铺信息
            return shop;
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
                    this.saveShopToRedis(id,30L);
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
        return shop;

    }


//    ⭐这个方法用于在redis中添加有experetime逻辑过期时间的店铺信息（逻辑过期相关代码）
    public void saveShopToRedis(Long id,Long expireSeconds){
//        查询店铺数据
    Shop shop = getById(id);
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
//    封装逻辑过期时间
    RedisData redisData = new RedisData();
    redisData.setData(shop);
    redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
//    写入redis
    stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY+id, JSONUtil.toJsonStr(redisData), expireSeconds, TimeUnit.SECONDS);
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









}
