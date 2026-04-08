package com.itheima.service.impl;


import com.itheima.mapper.bookmapper;
import com.itheima.pro.book;
import com.itheima.service.ibookservice;
import net.oschina.j2cache.CacheChannel;
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
    @Autowired
    private CacheChannel cacheChannel;
    @Override
    public book getById(Integer id) {
        book book1 = bookmapper.selectById(id);
        cacheChannel.set("book", String.valueOf(id),book1,10);
        return book1;
    }

    @Override
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
    public boolean delete(Integer id) {
        return false;
    }

    @Override
    public List<book> getAll() {
        return List.of();
    }
}
