package com.itheima.springbootmq.service.imp.rabbitmq.direct.config;

//import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


//@Configuration
public class RabbitmqDirectConfig {
    @Bean
    public Queue direct_queue(){
        return new Queue("direct_queue");
    }
    @Bean
    public DirectExchange direct_exchange(){
        return new DirectExchange("direct_exchange");
    }
    @Bean
    public Binding direct_queue_binding(){
        return BindingBuilder.bind(direct_queue()).to(direct_exchange()).with("direct_routing_key");
    }
}
