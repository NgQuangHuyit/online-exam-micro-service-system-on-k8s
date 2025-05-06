package com.ptit.soa2025.quizparticipationservice.constant;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public enum ErrorCode {
    QUIZ_NOT_FOUND("Không tìm thấy đề thi", HttpStatus.NOT_FOUND),
    QUIZ_PARTICIPATION_SUBMITTED("Bài thi đã kết thúc", HttpStatus.BAD_REQUEST),
    NOT_IN_ALLOWED_TIME("Ngoài thời gian cho phép làm bài", HttpStatus.FORBIDDEN),
    WRONG_ACCESS_CODE("Sai mã làm bài", HttpStatus.UNAUTHORIZED),
    QUIZ_PARTICIPATION_TIME_OVER("Bài thi đã kết thúc", HttpStatus.FORBIDDEN);


    @Getter
    private final String message;
    @Getter
    private final HttpStatus httpStatus;

    ErrorCode(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }


}
