package com.itheima.springbootmq.service.imp.base;

import com.itheima.springbootmq.service.MessageService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;


@Service
public class MessageServiceImp  {
    private ArrayList<String> messageList = new ArrayList<>();
    public void sendMessage(String id) {
        System.out.println("发送消息" + id);
        messageList.add(id);
    }

    public String doMessage() {
        String id = messageList.get(0);
        System.out.println("处理消息" + id);
        messageList.remove(0);
        return id;
    }
}
