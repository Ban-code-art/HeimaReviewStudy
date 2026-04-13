package com.hmdp.service.impl;

import com.hmdp.dto.Result;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.IVoucherOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisIdworker;
//import com.hmdp.utils.UserHolder;
import com.hmdp.utils.UserHolder;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public  class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {
@Autowired
private ISeckillVoucherService seckillVoucherService;
@Autowired
private RedisIdworker redisIdworker;
    @Override
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
        synchronized (id.toString().intern()) {//当前用户的值一样，所以可以使用id.toString().intern()来同步
            //获取代理对象（事务）| 拿到当前对象的代理对象
            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
            return proxy.createVoucherOrder(voucherId);
        }
    }

//    @Override
//    public Result createVoucherOrder(Long voucherId) {
//        return null;
//    }

//    @Override
//    public Result createVoucherOrder(Long voucherId) {
//
//        return Result.ok();
//    }

    @Transactional
    public  Result createVoucherOrder(Long voucherId) {
            Long id = UserHolder.getUser().getId();
            /*
             * 一人一单业务
             * */
//        业务一：查询订单
            Long count = query().eq("user_id", id)
                    .eq("voucher_id", voucherId)
                    .count();//记录拿到的数量
//        业务二：判断是否存在
            if (count > 0) {
                return Result.fail("已购买");
            }
//        5.扣减库存
//        voucher.setStock(voucher.getStock() - 1);
            boolean success = seckillVoucherService.update()
                    .setSql("stock = stock - 1")
                    .eq("voucher_id", voucherId)
                    .gt("stock", 0).update();
            // 把第54行那段替换成下面这几行
    /*    boolean success = seckillVoucherService.lambdaUpdate()
                .eq(SeckillVoucher::getVoucherId, voucherId)   // MP 自动转成 voucher_id
                .gt(SeckillVoucher::getStock, 0)               // 关键：防止超卖
                .setSql("stock = stock - 1")
                .update();*/
            if (!success) {
                return Result.fail("扣减库存失败");
            }


//        6。创建订单
//        6.1 订单id
            VoucherOrder voucherOrder = new VoucherOrder();
            long orderId = redisIdworker.nextId("order:");
            voucherOrder.setId(orderId);
//        6.2 用户id

            voucherOrder.setUserId(id);
//        6.3 代金券id
            voucherOrder.setVoucherId(voucherId);
            save(voucherOrder);
            return Result.ok(orderId);
        }
    }

