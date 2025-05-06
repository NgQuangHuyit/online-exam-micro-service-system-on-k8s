package com.ptit.soa2025.quizparticipationservice.utils;

import com.ptit.soa2025.quizparticipationservice.constant.ErrorCode;
import com.ptit.soa2025.quizparticipationservice.exception.QuizParticipationException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.data.crossstore.ChangeSetPersister;

public class QuizClientErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        return switch (response.status()) {
            case 400 -> new ChangeSetPersister.NotFoundException();
            case 401 -> new QuizParticipationException(ErrorCode.WRONG_ACCESS_CODE);
            default -> defaultDecoder.decode(methodKey, response);
        };
    }
}
