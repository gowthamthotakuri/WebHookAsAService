package com.WebHookAsAService.version1.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.WebHookAsAService.version1.entity.WebhookLog;
import com.WebHookAsAService.version1.repository.WebhookLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // <-- Import this


@Slf4j // <-- Add this Lombok annotation
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/logs")
public class WebhookLogController {

    private final WebhookLogRepository repository;

    @PostMapping
    public WebhookLog saveLog(@RequestBody WebhookLog webhookData) {
        
        // Now 'log' correctly refers to the SLF4J logger!
        log.info("Received request to save new webhook log for URL: {}", webhookData.getTargetUrl());
        
        // And we use 'webhookData' to save to the database
        WebhookLog savedLog = repository.save(webhookData);
        
        log.info("Successfully saved log with ID: {}", savedLog.getId());
        return savedLog;
    }

    @GetMapping
    public List<WebhookLog> getAllLogs() {
        log.info("Fetching all webhook logs from the database");
        return repository.findAll();
    }
}