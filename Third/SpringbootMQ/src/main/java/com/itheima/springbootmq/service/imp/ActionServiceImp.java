package com.itheima.springbootmq.service.imp;

import com.itheima.springbootmq.service.ActionService;
import com.itheima.springbootmq.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ActionServiceImp implements ActionService {
    @Autowired
    private MessageService messageService;
    @Override
    public void order(String id) {
//        实现业务层方法的具体逻辑
        System.out.println("订单处理开始");
        messageService.sendMessage(id);
        System.out.println("订单处理结束");
    }
}
