package com.springcloud.user.common.exception;

public class CustomTimeoutException extends RuntimeException {
    public CustomTimeoutException(String message) {
        super(message);
    }
}
