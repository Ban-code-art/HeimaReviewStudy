package com.itheima.springbootmq.service.imp.rabbitmq.topic;

import com.itheima.springbootmq.service.MessageService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceRabbitmqImplTopic implements MessageService {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void sendMessage(String id) {
        System.out.println("使用rabbitmq_topic发送消息: " + id);
        rabbitTemplate.convertAndSend("topic_exchange", "topic_topic_routing_key", id);
    }

    @Override
    public String doMessage() {
        return "";
    }
}
