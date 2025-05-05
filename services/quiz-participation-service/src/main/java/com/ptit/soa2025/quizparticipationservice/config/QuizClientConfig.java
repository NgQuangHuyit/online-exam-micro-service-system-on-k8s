package com.ptit.soa2025.quizparticipationservice.config;

import com.ptit.soa2025.quizparticipationservice.utils.QuizClientErrorDecoder;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuizClientConfig {
    @Bean
    public ErrorDecoder quizServiceErrorDecoder() {
        return new QuizClientErrorDecoder();
    }
}
