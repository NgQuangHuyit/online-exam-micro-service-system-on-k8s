package com.ptit.soa2025.quizparticipationservice.dto.request;


import com.ptit.soa2025.quizparticipationservice.dto.StudentAnswer;
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
    private List<@Valid StudentAnswer> answers;
}