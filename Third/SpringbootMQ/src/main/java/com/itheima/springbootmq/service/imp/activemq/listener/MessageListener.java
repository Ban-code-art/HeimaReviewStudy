package com.itheima.springbootmq.service.imp.activemq.listener;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

//@Component
public class MessageListener {
    @JmsListener(destination = "test.id")
    @SendTo("sendto.test.id")//将处理结果发送到sendto.test.id队列
    public String resive(String id){
        System.out.println("监听收到消息" + id);
        return id;
    }
}
