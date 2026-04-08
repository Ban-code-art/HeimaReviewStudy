package com.itheima.springbootmq.service.imp.rabbitmq.topic.config;

//import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitmqTopicConfig {
    @Bean
    public Queue topic_queue(){
        return new Queue("topic_queue");
    }
    @Bean
    public TopicExchange topic_exchange(){
        return new TopicExchange("topic_exchange");
    }
    @Bean
    public Binding topic_queue_binding(){
        return BindingBuilder.bind(topic_queue()).to(topic_exchange()).with("topic_routing_key");
    }
}
