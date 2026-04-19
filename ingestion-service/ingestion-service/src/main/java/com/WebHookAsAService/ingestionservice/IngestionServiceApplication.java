package com.WebHookAsAService.ingestionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan; // <-- Add this import

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.WebHookAsAService.ingestionservice.controller",
    "com.WebHookAsAService.ingestionservice.config"
}) // <-- Add this annotation
public class IngestionServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(IngestionServiceApplication.class, args);
    }
}