package com.xs.nzwbh.common.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String PEST_QUEUE = "canal.pest.queue";
    public static final String CANAL_EXCHANGE = "canal.exchange";
    public static final String PEST_ROUTING_KEY = "canal.pest.routing.key";

    public static final String CROP_QUEUE = "canal.crop.queue";
    public static final String CROP_ROUTING_KEY = "canal.crop.routing.key";


    @Bean
    public Queue canalQueue() {
        return QueueBuilder.durable(PEST_QUEUE).build();
    }

    @Bean
    public Exchange canalExchange() {
        return ExchangeBuilder.directExchange(CANAL_EXCHANGE).durable(true).build();
    }

    @Bean
    public Binding canalBinding() {
        return BindingBuilder.bind(canalQueue())
                .to(canalExchange())
                .with(PEST_ROUTING_KEY)
                .noargs();
    }

    @Bean
    public Queue cropQueue() {
        return QueueBuilder.durable(CROP_QUEUE).build();
    }


    @Bean
    public Binding cropBinding() {
        return BindingBuilder.bind(cropQueue())
                .to(canalExchange())
                .with(CROP_ROUTING_KEY)
                .noargs();
    }
}
