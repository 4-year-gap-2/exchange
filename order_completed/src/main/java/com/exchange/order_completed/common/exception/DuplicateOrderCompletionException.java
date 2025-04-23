package com.exchange.order_completed.common.exception;

public class DuplicateOrderCompletionException extends RuntimeException {
    public DuplicateOrderCompletionException(String message) {
        super(message);
    }
}
