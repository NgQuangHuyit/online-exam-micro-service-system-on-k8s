package com.ptit.soa2025.quizparticipationservice.controller;

import com.ptit.soa2025.quizparticipationservice.dto.request.StartParticipationSessionRequest;
import com.ptit.soa2025.quizparticipationservice.dto.response.ExamSessionResponse;
import com.ptit.soa2025.quizparticipationservice.service.ExamParticipationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@Slf4j
public class QuizParticipationController {
    
    private final ExamParticipationService examParticipationService;
    
    /**
     * API để bắt đầu phiên thi bằng mã truy cập
     * 1. Xác thực mã truy cập với Quiz Service
     * 2. Nếu hợp lệ, tạo hoặc lấy phiên làm bài
     * 3. Lấy danh sách câu hỏi (có cache)
     * 4. Trả về thông tin phiên và danh sách câu hỏi
     * 
     * @param request Dữ liệu yêu cầu bắt đầu phiên
     * @return ExamSessionResponse chứa thông tin phiên và câu hỏi
     */
    @PostMapping("/start-with-code")
    public ResponseEntity<?> startQuizWithCode(@Valid @RequestBody StartParticipationSessionRequest request) {
        try {
            log.info("Received start quiz with code request for student: {}, quiz: {}", 
                    request.getStudentId(), request.getQuizId());
            
            ExamSessionResponse response = examParticipationService.startQuizWithCode(request);
            
            if (response.getStatus() == com.ptit.soa2025.quizparticipationservice.model.ParticipationSession.SessionStatus.SUBMITTED) {
                // Trường hợp sinh viên đã nộp bài
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException ex) {
            // Xác thực thất bại
            log.error("Authentication failed: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Authentication failed", ex.getMessage()));
        } catch (Exception ex) {
            // Lỗi khác
            log.error("Error starting quiz session", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error processing request", "An unexpected error occurred"));
        }
    }
    
    /**
     * Helper method để tạo response lỗi
     */
    private Object createErrorResponse(String error, String message) {
        return new ErrorResponse(error, message);
    }
    
    /**
     * Inner class để response lỗi
     */
    private record ErrorResponse(String error, String message) {}
}
