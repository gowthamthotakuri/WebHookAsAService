package com.WebHookAsAService.version1.entity;



import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data // Lombok annotation to automatically generate getters, setters, and constructors
public class WebhookLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String targetUrl;

    @Column(columnDefinition = "TEXT")
    private String payload;

    private String status; // e.g., PENDING, DELIVERED, FAILED
    
    private int attemptCount;

    private LocalDateTime createdAt = LocalDateTime.now();
}