package com.itheima.springbootmq.service.imp.rabbitmq.direct;

import com.itheima.springbootmq.service.MessageService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

//@Service
public class MessageServiceRabbitmqImplDirect implements MessageService {
//    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void sendMessage(String id) {
        System.out.println("使用rabbitmq_direct发送消息: " + id);
        rabbitTemplate.convertAndSend("direct_exchange", "direct_routing_key", id);
    }

    @Override
    public String doMessage() {
        return "";
    }
}
