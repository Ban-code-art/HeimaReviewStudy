package com.itheima;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching//开启缓存的功能
public class Springboot2ThirdApplication {
    public static void main(String[] args) {
        SpringApplication.run(Springboot2ThirdApplication.class, args);
    }
}
