package com.hmdp.controller;


import cn.hutool.core.util.ObjectUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.service.IShopTypeService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@RestController
@RequestMapping("/shop-type")
public class ShopTypeController {
    @Resource
    private IShopTypeService typeService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
//给店铺的类型添加缓存  这里返回的是多个对象，所以使用HashRedis
    @GetMapping("list")
    public Result queryTypeList() {

        List<ShopType> typeList = typeService
                .query().orderByAsc("sort").list();
//        return Result.ok(typeList);
        return typeService.queryList();
    }
}
