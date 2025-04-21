package com.springcloud.user.common.exception;

public class CustomNotFoundException extends RuntimeException {
    public CustomNotFoundException(String message) {
        super(message);
    }
}
