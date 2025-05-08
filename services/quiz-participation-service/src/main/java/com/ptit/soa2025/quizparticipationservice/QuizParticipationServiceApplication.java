package com.ptit.soa2025.quizparticipationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
public class QuizParticipationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuizParticipationServiceApplication.class, args);
    }

}
