package com.ptit.soa2025.quizparticipationservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitQuizRequest {
    @NotEmpty(message = "Danh sách câu trả lời không được rỗng")
    private List<@Valid SubmittedAnswer> answers;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubmittedAnswer {
        @NotNull(message = "ID câu hỏi không được để trống")
        private String questionId;
        
        @NotNull(message = "Đáp án được chọn không được để trống")
        private String selectedOption;
    }
}