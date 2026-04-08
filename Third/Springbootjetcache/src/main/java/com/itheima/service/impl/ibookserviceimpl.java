package com.itheima.service.impl;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.*;
import com.itheima.mapper.bookmapper;
import com.itheima.pro.book;
import com.itheima.service.ibookservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ibookserviceimpl implements ibookservice {
    @Autowired
    private bookmapper bookmapper;
//    @CreateCache(name = "jetCache",expire = 3600,timeUnit = TimeUnit.SECONDS)
    @CreateCache(name = "jetCache",expire = 3600,timeUnit = TimeUnit.SECONDS,cacheType = CacheType.REMOTE)
    private Cache<Integer,book> jetCache;
    @Override
    @Cached(name = "book_cache",key = "#id")

    public book getById(Integer id) {
        book book = bookmapper.selectById(id);
//        jetCache.put(id,book);//放入缓存
//        book reget = getcache(id);//从缓存中获取
        return book;
    }

    private book getcache(Integer id) {
        book book = jetCache.get(id);
        return book;
    }
    @Override
    @CacheUpdate(name = "book_cache",key = "#book.id",value = "#book")//更新属性，使用value替换缓存中的值
    public boolean update(book book) {
        return false;
    }
    @Override

    public List<book> getbook() {
        return bookmapper.selectList(null);
    }



    @Override
    public boolean save(book book) {
        return false;
    }



    @Override
    @CacheInvalidate(name = "book_cache",key = "#id")
    public boolean delete(Integer id) {
        return false;
    }

    @Override
    public List<book> getAll() {
        return List.of();
    }
}
