package com.WebHookAsAService.dispatcherservice.service;

import com.WebHookAsAService.dispatcherservice.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookDispatcher {

    private final RabbitTemplate rabbitTemplate;
    private final RestTemplate restTemplate = new RestTemplate();
    
    // We will replace this with Eureka service discovery in the next phase!
    private final String AUDIT_SERVICE_URL = "http://localhost:8083/api/logs";

    /**
     * Listens to the Main Queue. If processing fails, it sends the message
     * to the Retry Queue with a TTL (Time To Live).
     */
    @RabbitListener(queues = RabbitMQConfig.MAIN_QUEUE)
    public void processWebhook(Map<String, Object> payload, Message message) {
        String targetUrl = (String) payload.get("targetUrl");
        
        // Retrieve the current retry count from the message headers (defaults to 1)
        Integer attempt = (Integer) message.getMessageProperties().getHeaders().getOrDefault("x-retry-count", 1);
        
        try {
            log.info(">>> Attempt #{} for URL: {}", attempt, targetUrl);
            
            // 1. Physically attempt to deliver the webhook to the external target
            restTemplate.postForEntity(targetUrl, payload, String.class);
            
            log.info("<<< Webhook delivered successfully to {}", targetUrl);
            
            // 2. Success: Update Audit Service
            updateAuditLog(targetUrl, payload, "SUCCESS", attempt);
            
        } catch (Exception e) {
            long delay = getWaitTime(attempt);
            
            if (delay != -1) {
                log.warn("!!! Delivery failed. Moving to Retry Queue for {}ms. (Finished attempt {})", delay, attempt);
                
                // 3. Update Audit Service to "RETRYING" status
                updateAuditLog(targetUrl, payload, "RETRYING", attempt);
                
                // 4. Send the message to the "Parking Lot" (Retry Queue)
                sendToRetryQueue(payload, attempt + 1, delay);
            } else {
                log.error("XXX All retry attempts (7) exhausted for {}. Marking as FAILED.", targetUrl);
                
                // 5. Final Failure: Update Audit Service
                updateAuditLog(targetUrl, payload, "FAILED", attempt);
            }
        }
    }

    /**
     * Sends the message to a side queue with no listeners. 
     * Once the 'expiration' (delay) hits, RabbitMQ kicks it back to the Main Queue.
     */
    private void sendToRetryQueue(Map<String, Object> payload, int nextAttempt, long delay) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.RETRY_QUEUE, payload, msg -> {
            msg.getMessageProperties().setExpiration(String.valueOf(delay));
            msg.getMessageProperties().setHeader("x-retry-count", nextAttempt);
            return msg;
        });
    }

    /**
     * Communicates the current status back to the Audit Service database.
     */
    private void updateAuditLog(String url, Map<String, Object> payload, String status, int attempts) {
        try {
            Map<String, Object> auditRequest = new HashMap<>();
            auditRequest.put("targetUrl", url);
            auditRequest.put("payload", payload.toString());
            auditRequest.put("status", status);
            auditRequest.put("attemptCount", attempts);

            restTemplate.postForEntity(AUDIT_SERVICE_URL, auditRequest, Void.class);
            log.info("Audit Service Notified: Status={}", status);
        } catch (Exception e) {
            log.error("CRITICAL: Dispatcher cannot reach Audit Service at {}. Error: {}", 
                      AUDIT_SERVICE_URL, e.getMessage());
        }
    }

    /**
     * Logic for your specific interval requirements.
     */
    private long getWaitTime(int attempt) {
        return switch (attempt) {
            case 1 -> 1000;      // 1 second
            case 2 -> 5000;      // 5 seconds
            case 3 -> 10000;     // 10 seconds
            case 4 -> 60000;     // 1 minute
            case 5 -> 1800000;   // 30 minutes
            case 6 -> 21600000;  // 6 hours
            case 7 -> 86400000;  // 1 day
            default -> -1;       // Stop after 7 tries
        };
    }
}