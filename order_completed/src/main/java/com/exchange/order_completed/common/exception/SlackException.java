package com.exchange.order_completed.common.exception;

public class SlackException extends RuntimeException {
    public SlackException(String message) {
        super(message);
    }
}
