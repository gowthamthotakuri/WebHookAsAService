package com.WebHookAsAService.ingestionservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class WebhookIngestionController {

    private final RabbitTemplate rabbitTemplate;

    @Value("${webhook.queue.name}")
    private String queueName;

    // TEST ENDPOINT: Hit this in your browser: http://localhost:8081/api/webhooks/test
    @GetMapping("/test")
    public String test() {
        return "Controller is ALIVE on port 8081!";
    }

    @PostMapping("/send")
    public ResponseEntity<String> receiveWebhook(@RequestBody Map<String, Object> payload) {
        log.info("Received payload: {}", payload);
        rabbitTemplate.convertAndSend(queueName, payload);
        return ResponseEntity.accepted().body("Webhook successfully queued!");
    }
}