package com.itheima.service.impl;

import com.itheima.mapper.bookmapper;
import com.itheima.pro.book;
import com.itheima.service.ibookservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class ibookserviceimpl implements ibookservice {
    @Autowired
    private bookmapper bookmapper;
/*    //定义一个hashmap当做缓存
    private HashMap<Integer,book> cacahe = new HashMap<>();

    @Override
    public book getById(Integer id) {
//        如果当前缓存中没有本次要查询的数据，则进行查询，否则直接从缓存中获取数据返回
        book book = cacahe.get(id);
        if(book == null){
            book querybook = bookmapper.selectById(id);
            cacahe.put(id,querybook);//将数据放回到缓存
            return  querybook;
        }
        return cacahe.get(id);
    }*/
    @Override
    @Cacheable(value = "book",key = "#id")
//    @CachePut(value = "book",key = "#id")
    public book getById(Integer id) {
    return bookmapper.selectById(id);
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
    public boolean update(book book) {
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
