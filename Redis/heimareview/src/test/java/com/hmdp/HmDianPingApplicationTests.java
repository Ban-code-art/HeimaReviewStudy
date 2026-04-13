package com.hmdp;

import com.hmdp.entity.Shop;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IShopService;
import com.hmdp.service.impl.ShopServiceImpl;
import com.hmdp.utils.CacheClient;
import com.hmdp.utils.RedisIdworker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.Collectors;

import static com.hmdp.utils.RedisConstants.CACHE_SHOP_KEY;

@SpringBootTest
class HmDianPingApplicationTests {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ShopServiceImpl shopServiceImpl;
    @Autowired
    private CacheClient cacheClient;
    @Autowired
    private RedisIdworker redisIdworker;

    private ExecutorService es = Executors.newFixedThreadPool(500);
    /*
    * 测试保存商铺到Redis
    * */
    @Test
    void testSaveToRedis(){
        shopServiceImpl.saveShopToRedis(1L,1000L);

    }
    /*
    * 测试工具类中的逻辑过期方法
    * */
    @Test
    void testLogical(){
        Shop shop = shopServiceImpl.getById(1L);
        cacheClient.setLogicalExpire(CACHE_SHOP_KEY + 1L,shop,10L,TimeUnit.SECONDS);
    }
    /*
    * 测试全局ID生成器
    * */
    @Test
    void testNextId() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(300);
        Runnable task = () -> {
            for (int i = 0; i < 100; i++) {
                long id = redisIdworker.nextId("order");
                System.out.println(id);
            }
            latch.countDown();
        };
        long begin = System.currentTimeMillis();
        for (int i = 0; i < 300; i++) {
            es.submit(task);

        }
        latch.await();
        long end = System.currentTimeMillis();
        System.out.println("耗时：" + (end - begin) + "ms");


    }




}