package com.WebHookAsAService.dispatcherservice.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {

    public static final String MAIN_QUEUE = "webhook.processing.queue";
    public static final String RETRY_QUEUE = "webhook.retry.queue";
    public static final String EXCHANGE = "webhook.exchange";

    @Bean
    public Queue mainQueue() {
        return new Queue(MAIN_QUEUE, true);
    }

    @Bean
    public Queue retryQueue() {
        return QueueBuilder.durable(RETRY_QUEUE)
                .withArgument("x-dead-letter-exchange", "") // Send back to default exchange
                .withArgument("x-dead-letter-routing-key", MAIN_QUEUE) // Send back to main queue
                .build();
    }
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}