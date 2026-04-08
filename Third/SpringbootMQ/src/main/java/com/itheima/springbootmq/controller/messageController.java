package com.itheima.springbootmq.controller;

import com.itheima.springbootmq.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class messageController {
    @Autowired
    private MessageService messageService;
    @PostMapping("/send/{id}")
    public void sendMessage(@PathVariable String id) {
        messageService.sendMessage(id);

    }
    @GetMapping("/do")
    public String doMessage(){
        return messageService.doMessage();
    }
   }
