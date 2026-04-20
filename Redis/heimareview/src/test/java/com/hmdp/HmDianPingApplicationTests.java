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
import org.springframework.data.geo.Point;
//import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

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
    /*
    * 附近商铺测试
    * */
    @Test
    void loadShopData(){
//        1.查询店铺信息
        List<Shop> list = shopServiceImpl.list();
//        2.将店铺分组，按照typeId分组，typeId一致的放到一个集合(类型id,店铺集合)
        Map<Long, List<Shop>> map = list.stream().collect(Collectors.groupingBy(Shop::getTypeId));
//        3.分批完成写入redis的geo
        for (Map.Entry<Long, List<Shop>> entry : map.entrySet()) {
//        3.1.获取类型id
            Long typeId = entry.getKey();
            String key = "shop:geo:" + typeId;
//        3.2.获取同类型的店铺的集合
            List<Shop> value = entry.getValue();
            List<RedisGeoCommands.GeoLocation<String>> locations = new ArrayList<>(value.size());
//        3.3.写入redis geoadd key 经度 纬度 member
            for (Shop shop : value) {
//                stringRedisTemplate.opsForGeo().add(key,new Point(shop.getX(),shop.getY()),shop.getId().toString());
                locations.add(new RedisGeoCommands.GeoLocation<>(shop.getId().toString(),new Point(shop.getX(),shop.getY())));
            }
            stringRedisTemplate.opsForGeo().add(key,locations);
        }
    }

    @Test
    void testHyperLogLog(){
        String[] values = new String[1000];
        int j = 0;
        for (int i = 0; i < 1000000; i++) {
            j = i % 1000;
            values[j] = "user_" + i;
            if (j == 999) {
//                发送到redis
                stringRedisTemplate.opsForHyperLogLog().add("hl2",values);
            }
        }
//        统计数量
        long count = stringRedisTemplate.opsForHyperLogLog().size("hl2");
        System.out.println(count);
    }



}