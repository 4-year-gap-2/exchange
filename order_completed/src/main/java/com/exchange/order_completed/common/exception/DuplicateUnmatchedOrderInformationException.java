package com.exchange.order_completed.common.exception;

public class DuplicateUnmatchedOrderInformationException extends RuntimeException {
    public DuplicateUnmatchedOrderInformationException(String message) {
        super(message);
    }
}
