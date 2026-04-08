package com.itheima;

import com.itheima.pro.book;
import com.itheima.service.ibookservice;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class Springboot2SourceApplicationTests {
    @Autowired
    private ibookservice ibookservice;
    @Test
    void contextLoads() {
        List<book> getbook = ibookservice.getbook();
        System.out.println(getbook);
    }

}
