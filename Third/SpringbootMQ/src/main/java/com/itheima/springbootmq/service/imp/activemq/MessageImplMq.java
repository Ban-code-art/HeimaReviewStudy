package com.itheima.springbootmq.service.imp.activemq;

import com.itheima.springbootmq.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Service;

//@Service
public class MessageImplMq implements MessageService {
    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;
    @Override
    public void sendMessage(String id) {
        System.out.println("发送消息" + id);
        jmsMessagingTemplate.convertAndSend("test.id",id);
    }

    @Override
    public String doMessage() {
        String id = jmsMessagingTemplate.receiveAndConvert("test.id",String.class);
        System.out.println("处理消息" + id);
        return id;
    }
}
