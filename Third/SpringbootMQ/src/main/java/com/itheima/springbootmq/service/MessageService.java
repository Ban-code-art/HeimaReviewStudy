package com.itheima.springbootmq.service;

public interface MessageService {
    void sendMessage(String id);
    String doMessage();
}
