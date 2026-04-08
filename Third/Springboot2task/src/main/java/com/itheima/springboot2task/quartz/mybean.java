package com.itheima.springboot2task.quartz;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class mybean {
    @Scheduled(cron = "0/1 * * * * ?")//每1秒钟执行一次
    void print(){
        System.out.println("this is a bean to print and test @EnableScheduling");
    }
}
