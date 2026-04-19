package com.WebHookAsAService.version1.repository;

import com.WebHookAsAService.version1.entity.WebhookLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookLogRepository extends JpaRepository<WebhookLog, Long> {
}