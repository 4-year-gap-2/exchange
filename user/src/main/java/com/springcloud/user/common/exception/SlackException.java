package com.springcloud.user.common.exception;

public class SlackException extends RuntimeException {
    public SlackException(String message) {
        super(message);
    }
}
