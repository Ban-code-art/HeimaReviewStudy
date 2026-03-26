package com.itheima.controller;

import com.itheima.pro.Result;
import com.itheima.pro.book;
import com.itheima.service.ibookservice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class bookcontroller {
    @Autowired
    private ibookservice bookservice;
    @GetMapping("/book/{id}")
    public Result getById(@PathVariable Integer id){
        log.info("根据id查询图书信息，id={}",id);
        book byId = bookservice.getById(id);

        return Result.success(byId);
    }
}
