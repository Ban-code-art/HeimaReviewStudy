package com.itheima.springbootmq.service.imp.rabbitmq.topic.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class MessageListener {
    @RabbitListener(queues = "topic_queue")
    public void receive(String message) {
        System.out.println("监听收到消息" + message);
    }
}
