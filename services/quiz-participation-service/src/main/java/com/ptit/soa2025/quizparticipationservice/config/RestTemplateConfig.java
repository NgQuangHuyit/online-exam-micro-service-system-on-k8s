package com.ptit.soa2025.quizparticipationservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {
    
    @Value("${services.quiz-service.url}")
    private String quizServiceUrl;
    
    @Value("${services.connection.timeout:5000}")
    private int connectionTimeout;
    
    @Value("${services.read.timeout:5000}")
    private int readTimeout;
    
    @Bean(name = "quizServiceRestTemplate")
    public RestTemplate quizServiceRestTemplate() {
        return new RestTemplateBuilder()
                .rootUri(quizServiceUrl)
                .setConnectTimeout(Duration.ofMillis(connectionTimeout))
                .setReadTimeout(Duration.ofMillis(readTimeout))
                .build();
    }
}