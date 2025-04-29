package com.exchange.order_completed.common.exception;

import com.exchange.order_completed.common.response.ResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(
            {
                    IllegalArgumentException.class,
                    CustomNotFoundException.class,
                    Exception.class,
                    CustomForbiddenException.class,
                    CustomConflictException.class,
                    CustomTimeoutException.class,
                    DuplicateMatchedOrderInformationException.class,
                    DuplicateUnmatchedOrderInformationException.class
            })
    @ResponseBody
    public ResponseEntity<ResponseDto<Object>> handleExceptions(Exception e) {
        HttpStatus status = getHttpStatus(e);
        return createErrorResponse(status, e.getMessage());
    }

    private HttpStatus getHttpStatus(Exception e) {
        if (e instanceof IllegalArgumentException) {
            return HttpStatus.BAD_REQUEST;
        } else if (e instanceof CustomNotFoundException) {
            return HttpStatus.NOT_FOUND;
        } else if (e instanceof CustomForbiddenException) {
            return HttpStatus.FORBIDDEN;
        } else if (e instanceof CustomConflictException) {
            return HttpStatus.CONFLICT;
        } else if (e instanceof CustomTimeoutException) {
            return HttpStatus.GATEWAY_TIMEOUT;
        } else if (e instanceof DuplicateMatchedOrderInformationException) {
            return HttpStatus.CONFLICT;
        } else if (e instanceof DuplicateUnmatchedOrderInformationException) {
            return HttpStatus.CONFLICT;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private ResponseEntity<ResponseDto<Object>> createErrorResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(ResponseDto.failure(status, message));
    }
}
