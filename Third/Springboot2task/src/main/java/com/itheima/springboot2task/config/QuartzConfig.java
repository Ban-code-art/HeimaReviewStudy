package com.itheima.springboot2task.config;

import com.itheima.springboot2task.quartz.myQuartz;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {
  @Bean//工作明细
    public JobDetail printjobdetail(){
//      绑定具体的工作
      return JobBuilder.newJob(myQuartz.class).storeDurably().build();
  }
  @Bean//触发器
    public Trigger printjobtrigger(){
//      绑定对应的工作明细
      return TriggerBuilder.newTrigger().forJob(printjobdetail()).build();
    }
}
