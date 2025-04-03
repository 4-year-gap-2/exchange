package com.exchange.matching.common.exception;

public class CustomTimeoutException extends RuntimeException {
    public CustomTimeoutException(String message) {
        super(message);
    }
}
