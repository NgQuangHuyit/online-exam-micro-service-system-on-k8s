package com.ptit.soa2025.quizparticipationservice.controller;

import com.ptit.soa2025.quizparticipationservice.dto.request.StartParticipationSessionRequest;
import com.ptit.soa2025.quizparticipationservice.dto.request.SubmitQuizRequest;
import com.ptit.soa2025.quizparticipationservice.dto.response.ExamSessionResponse;
import com.ptit.soa2025.quizparticipationservice.dto.response.SubmitQuizResponse;
import com.ptit.soa2025.quizparticipationservice.exception.QuizParticipationException;
import com.ptit.soa2025.quizparticipationservice.service.ExamParticipationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> startQuizWithCode(@Valid @RequestBody StartParticipationSessionRequest request) throws QuizParticipationException {
        log.info("Received start quiz with code request for student: {}, quiz: {}",
                request.getStudentId(), request.getQuizId());

        ExamSessionResponse response = examParticipationService.startQuizWithCode(request);

        if (response.getStatus() == com.ptit.soa2025.quizparticipationservice.model.ParticipationSession.SessionStatus.SUBMITTED) {
            // Trường hợp sinh viên đã nộp bài
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * API để nộp bài thi
     * 1. Xác thực phiên làm bài và thời gian nộp
     * 2. Lưu trạng thái phiên làm bài thành SUBMITTED
     * 3. Gửi event tới Kafka để xử lý chấm điểm
     * 
     * @param sessionId ID của phiên làm bài
     * @param request Danh sách câu trả lời
     * @return Thông báo nộp bài thành công
     */
    @PostMapping("/{sessionId}/submit")
    public ResponseEntity<?> submitQuiz(
            @PathVariable String sessionId,
            @Valid @RequestBody SubmitQuizRequest request) {
        
        log.info("Received submit quiz request for session: {}, with {} answers", 
                sessionId, request.getAnswers().size());
        
        try {
            SubmitQuizResponse response = examParticipationService.submitQuiz(sessionId, request);
            return ResponseEntity.ok(response);
        } catch (QuizParticipationException e) {
            log.error("Error submitting quiz: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(e.getErrorCode().getHttpStatus())
                    .body(createErrorResponse(e.getErrorCode().name(), e.getMessage()));
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
