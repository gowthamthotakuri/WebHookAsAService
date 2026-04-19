package com.WebHookAsAService.ingestionservice.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

@Configuration
public class RabbitMQConfig {

    // This grabs the name from your application.properties file
    @Value("${webhook.queue.name}")
    private String queueName;

    // This @Bean tells Spring to ensure this queue exists in RabbitMQ on startup.
    // The "true" means the queue is "durable" (it survives if Docker restarts).
    @Bean
    public Queue webhookQueue() {
        return new Queue(queueName, true);
    }

    @Bean
public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
}
}