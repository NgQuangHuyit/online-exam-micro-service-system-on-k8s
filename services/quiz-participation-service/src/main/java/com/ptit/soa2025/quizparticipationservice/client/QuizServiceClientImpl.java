//package com.ptit.soa2025.quizparticipationservice.client;
//
//import com.ptit.soa2025.quizparticipationservice.dto.request.ValidateAccessCodeRequest;
//import com.ptit.soa2025.quizparticipationservice.dto.response.AccessValidationResponse;
//import com.ptit.soa2025.quizparticipationservice.dto.response.QuestionResponse;
//import com.ptit.soa2025.quizparticipationservice.exception.QuizServiceException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.ParameterizedTypeReference;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.client.WebClient;
//import org.springframework.web.reactive.function.client.WebClientResponseException;
//import reactor.core.publisher.Mono;
//
//import java.time.Duration;
//import java.util.List;
//
//@Service
//public class QuizServiceClientImpl implements QuizServiceClient {
//    private static final Logger logger = LoggerFactory.getLogger(QuizServiceClientImpl.class);
//    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);
//
//    private final WebClient webClient;
//
//    @Autowired
//    public QuizServiceClientImpl(WebClient quizServiceWebClient) {
//        this.webClient = quizServiceWebClient;
//    }
//
//    @Override
//    public AccessValidationResponse validateAccessCode(String quizId, ValidateAccessCodeRequest request) {
//        logger.info("Validating access code for quiz: {}, student: {}", quizId, request.getStudentId());
//
//        try {
//            return webClient.post()
//                    .uri("/{quizId}/access-validate", quizId)
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .bodyValue(request)
//                    .retrieve()
//                    .onStatus(status -> status.equals(HttpStatus.UNAUTHORIZED),
//                            response -> Mono.error(new QuizServiceException("Invalid access code", HttpStatus.UNAUTHORIZED)))
//                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
//                            response -> response.bodyToMono(String.class)
//                                    .flatMap(error -> Mono.error(new QuizServiceException(
//                                            "Error validating access code: " + error,
//                                            response.statusCode()))))
//                    .bodyToMono(AccessValidationResponse.class)
//                    .timeout(REQUEST_TIMEOUT)
//                    .block();
//        } catch (WebClientResponseException e) {
//            logger.error("Error validating access code: {}", e.getMessage());
//            throw new QuizServiceException("Error validating access code: " + e.getMessage(), e.getStatusCode());
//        } catch (Exception e) {
//            logger.error("Unexpected error validating access code: {}", e.getMessage());
//            throw new QuizServiceException("Unexpected error validating access code: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    @Override
//    public List<QuestionResponse> getQuestionsForQuiz(String quizId) {
//        logger.info("Getting questions for quiz: {}", quizId);
//
//        try {
//            return webClient.get()
//                    .uri("/{quizId}/questions", quizId)
//                    .accept(MediaType.APPLICATION_JSON)
//                    .retrieve()
//                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
//                            response -> response.bodyToMono(String.class)
//                                    .flatMap(error -> Mono.error(new QuizServiceException(
//                                            "Error retrieving questions: " + error,
//                                            response.statusCode()))))
//                    .bodyToMono(new ParameterizedTypeReference<List<QuestionResponse>>() {})
//                    .timeout(REQUEST_TIMEOUT)
//                    .block();
//        } catch (WebClientResponseException e) {
//            logger.error("Error retrieving questions: {}", e.getMessage());
//            throw new QuizServiceException("Error retrieving questions: " + e.getMessage(), e.getStatusCode());
//        } catch (Exception e) {
//            logger.error("Unexpected error retrieving questions: {}", e.getMessage());
//            throw new QuizServiceException("Unexpected error retrieving questions: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//}