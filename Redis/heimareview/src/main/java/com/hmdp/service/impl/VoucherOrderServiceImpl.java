package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Voucher;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.IVoucherOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisIdworker;
//import com.hmdp.utils.UserHolder;
import com.hmdp.utils.UserHolder;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public  class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {
    @Autowired
    private ISeckillVoucherService seckillVoucherService;
    @Autowired
    private RedisIdworker redisIdworker;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedissonClient redissonClient;
    /*
     * 使用lua脚本优化秒杀业务
     * */
    /*
     * 执行lua脚本相关代码
     * */
    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();//初始化
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));//classpathresource是resource的实现类，用于加载classpath下的资源文件
        SECKILL_SCRIPT.setResultType(Long.class);//设置返回值的类型
    }

    private static final ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();//线程池
    private IVoucherOrderService proxy;

    @PostConstruct//
    private void init() {
//        将runnable任务提交给线程池执行
        SECKILL_ORDER_EXECUTOR.submit(new VoucherOrderHandler());
    }




    /*
* 使用异步下单优化秒杀业务|redis消息队列
* */
    private class VoucherOrderHandler implements Runnable {
        String queueName = "stream.orders";//消息队列的名字
        @Override
        public void run() {
            while (true) {
                try {
//                1.从消息队列中获取信息 xreadgroup group g1 c1 count 1 block 2000 streams streams.order
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                            StreamOffset.create(queueName, ReadOffset.lastConsumed())
                    );
//                    2.判断消息是否获取成功
                    if (list == null || list.isEmpty()) {
//                        如果获取失败，说明没有消息，继续下一次循环
                        continue;
                    }
//                    3.解析消息中的订单信息
                    MapRecord<String, Object, Object> record = list.get(0);
                    Map<Object, Object> values = record.getValue();
                    VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(values, new VoucherOrder(), true);
                    //4.创建订单 通过创建一个方法来创建订单
                    handleVoucherOrder(voucherOrder);
//                    5.ack确认sack stream.orders g1 id
                    stringRedisTemplate.opsForStream().acknowledge(queueName,"g1",record.getId());
                } catch (Exception e) {

                    log.error("获取队列中的订单信息失败", e);
//                    处理放入到pendinglist中的异常消息
                    handlePendingList();
                }
            }
        }

    private void handlePendingList() {
        while (true) {
            try {
//                1.从pending-list中获取信息 xreadgroup group g1 c1 count 1  2000 streams streams.order 0
                List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                        Consumer.from("g1", "c1"),
                        StreamReadOptions.empty().count(1),
                        StreamOffset.create(queueName, ReadOffset.from("0"))
                );
//                    2.判断消息是否获取成功
                if (list == null || list.isEmpty()) {
//                   如果获取失败，说明pending-list没有消息，结束循环
                    break;
                }
//                    3.解析消息中的订单信息
                MapRecord<String, Object, Object> record = list.get(0);
                Map<Object, Object> values = record.getValue();
                VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(values, new VoucherOrder(), true);
                //4.创建订单 通过创建一个方法来创建订单
                handleVoucherOrder(voucherOrder);
//                    5.ack确认sack stream.orders g1 id
                stringRedisTemplate.opsForStream().acknowledge(queueName,"g1",record.getId());
            } catch (Exception e) {

                log.error("获取pending-list中的订单信息失败", e);
            }
        }
    }

        @PostConstruct
        public void initStream() {
            try {
                // 创建消费者组（如果不存在）
                stringRedisTemplate.opsForStream().createGroup(queueName, ReadOffset.latest(), "g1");
            } catch (RedisSystemException e) {
                // 消费者组已存在，忽略错误
                log.info("消费者组g1已存在");
            }
        }
}


        private void handleVoucherOrder(VoucherOrder voucherOrder) {
            //因为在lua脚本中已经判断了库存和是否重复下单，所以这里直接创建订单就行了
            Long userId = voucherOrder.getUserId();
            //创建锁对象
            RLock lock = redissonClient.getLock("lock:order:" + userId);
            //尝试获取锁
            boolean islock = lock.tryLock();
            //判断锁是否获取成功
            if (!islock) {
                log.error("不允许重复下单");
                return;
            }
            try {
                //获取代理对象（事务）| 拿到当前对象的代理对象 在这里是拿不到代理对象的，因为是子线程

                proxy.createVoucherOrder(voucherOrder);
            } finally {
                //释放锁
                lock.unlock();
            }
        }
    /*
     * 秒杀业务优化代码 | 使用Redis消息队列
     * */
    @Override
    public Result seckillVoucher(Long voucherId) {
//        获取用户id
        Long userId = UserHolder.getUser().getId();
//        获取订单id
        long orderId = redisIdworker.nextId("order:");//获取订单id
//        1.执行lua脚本
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,//lua脚本对象
                Collections.emptyList(),//key
                voucherId.toString(), //ARGV[1]
                userId.toString(),//ARGV[2]
                String.valueOf(orderId)//ARGV[3]
        );
//        2.判断结果是否是零
        int r = result.intValue();
        if (r != 0) {
//            2.1不为零，代表没有购买资格
            return Result.fail(r == 1 ? "库存不足" : "不能重复下单");
        }
        proxy = (IVoucherOrderService) AopContext.currentProxy();
//        返回订单id
        return Result.ok(orderId);
    }

        @Transactional
        public void createVoucherOrder(VoucherOrder voucherOrder) {
            Long userId = voucherOrder.getUserId();
            /*
             * 一人一单业务
             * */
//        业务一：查询订单
            Long count = query().eq("user_id", userId)
                    .eq("voucher_id", voucherOrder)
                    .count();//记录拿到的数量
//        业务二：判断是否存在
            if (count > 0) {
                log.error("已购买");
                return;
            }
//        5.扣减库存
//        voucher.setStock(voucher.getStock() - 1); 防止超卖
            boolean success = seckillVoucherService.update()
                    .setSql("stock = stock - 1")
                    .eq("voucher_id", voucherOrder.getVoucherId())
                    .gt("stock", 0).update();

            if (!success) {
                log.error("扣减库存失败");
                return;
            }
            save(voucherOrder);

        }


    }

/*
 * 创建完了阻塞队列之后实现异步下单
 * */
/*
    private BlockingQueue<VoucherOrder> orderTasks = new ArrayBlockingQueue<>(1024 * 1024);//阻塞队列
    private class VoucherOrderHandler implements Runnable{

        @Override
        public void run() {
            while (true) {
                try {
//                获取队列中的订单信息 线程池不断从阻塞队列中取出订单
                    VoucherOrder voucherOrder = orderTasks.take();//从阻塞队列中提取
                    //创建订单 通过创建一个方法来创建订单
                    handleVoucherOrder(voucherOrder);
                } catch (InterruptedException e) {
                    log.error("获取队列中的订单信息失败", e);
                }
            }
        }
    }
*/

        /*
         * 秒杀业务优化代码
         * *//*

    @Override
    public Result seckillVoucher(Long voucherId) {
//        获取用户id
        Long userId = UserHolder.getUser().getId();
//        1.执行lua脚本
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,//lua脚本对象
                Collections.emptyList(),//key
                voucherId.toString(), //ARGV[1]
                userId.toString()//ARGV[2]
        );
//        2.判断结果是否是零
        int r = result.intValue();
        if (r != 0) {
//            2.1不为零，代表没有购买资格
            return Result.fail(r ==1 ? "库存不足" : "不能重复下单");
        }
        // 2.2为零，代表购买成功，将下单的信息保存到阻塞队列
        long orderId = redisIdworker.nextId("order:");//获取订单id
//        TODO 保存阻塞队列
//        6。创建订单
        VoucherOrder voucherOrder = new VoucherOrder();
        voucherOrder.setId(orderId);
//        6.2 用户id
        voucherOrder.setUserId(userId);
//        6.3 代金券id
        voucherOrder.setVoucherId(voucherId);
//        放入到阻塞队列
        orderTasks.add(voucherOrder);
//        获取代理对象初始化  放到了成员变量
         proxy = (IVoucherOrderService) AopContext.currentProxy();
//        返回订单id
         return Result.ok(orderId);
    }
*/





        /*
         * 原来的秒杀业务，未使用lua脚本优化
         * */

    /*@Override
    public Result seckillVoucher(Long voucherId) {
//        1.查询优惠券
        SeckillVoucher voucher = seckillVoucherService.getById(voucherId);
//        2.判断秒杀是否开始
        System.out.println(voucher);
        if(voucher.getBeginTime().isAfter(LocalDateTime.now())){
            return Result.fail("秒杀未开始");
        }
//        3.判断秒杀是否已经结束
        if(voucher.getEndTime().isBefore(LocalDateTime.now())){
            return Result.fail("秒杀已结束");
        }
//        4。判断库存是否充足
        if(voucher.getStock() <= 0){
            return Result.fail("库存不足");
        }

//        7。返回订单id
        Long id = UserHolder.getUser().getId();
        *//*
         * 使用分布式锁实现一人一单业务
         * *//*
        //业务一：创建锁对象
//        SimpleRedisLock lock = new SimpleRedisLock("order:" + id, stringRedisTemplate);
        RLock lock = redissonClient.getLock("order:" + id);

        //业务二：尝试获取锁
        boolean tryLock = lock.tryLock();
        //判断锁是否获取成功
        if(!tryLock){
            return Result.fail("只允许一人一单");
        }

        try {
            //获取代理对象（事务）| 拿到当前对象的代理对象
            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
            return proxy.createVoucherOrder(voucherId);
        } finally {
            //业务三：释放锁
            lock.unlock();
        }

    }*/

//    @Override
//    public Result createVoucherOrder(Long voucherId) {
//        return null;
//    }

//    @Override
//    public Result createVoucherOrder(Long voucherId) {
//
//        return Result.ok();
//    }





