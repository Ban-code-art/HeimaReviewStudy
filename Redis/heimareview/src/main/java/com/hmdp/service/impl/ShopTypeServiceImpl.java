package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {
@Autowired
private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result queryList() {
        String key = "cache:shop:ShopType_list";
//        首先先从redis中查询然后进行判断
        String json = stringRedisTemplate.opsForValue().get(key);
//        如果有数据的话那么就返回
        if (!StrUtil.isEmpty(json)) {
            List<ShopType> list = JSONUtil.toList(json, ShopType.class);
            return Result.ok(list);
        }
//        如果没有数据的话就从数据库中查询
        List<ShopType> list = query().orderByAsc("sort").list();

//        判断是否存在，不存在的话返回401
        if (list.isEmpty()) {
            return Result.fail("店铺类型不存在");
        }
//        存在将查询到的数据保存到redis中
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(list),30, TimeUnit.MINUTES);
        return Result.ok(list);
    }
}
