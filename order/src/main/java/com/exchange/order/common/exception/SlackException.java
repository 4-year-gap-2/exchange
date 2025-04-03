package com.exchange.order.common.exception;

public class SlackException extends RuntimeException {
    public SlackException(String message) {
        super(message);
    }
}
