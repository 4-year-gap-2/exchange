package com.exchange.order_completed.common.exception;

public class DuplicateMatchedOrderInformationException extends RuntimeException {
    public DuplicateMatchedOrderInformationException(String message) {
        super(message);
    }
}
