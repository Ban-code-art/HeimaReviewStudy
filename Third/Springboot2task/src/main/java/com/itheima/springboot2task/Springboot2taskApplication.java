package com.itheima.springboot2task;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling//开启定时任务的开关
public class Springboot2taskApplication {

    public static void main(String[] args) {
        SpringApplication.run(Springboot2taskApplication.class, args);
    }

}
