package com.exchange.order.common.exception;

import java.util.UUID;

public class AddressNotFoundException extends RuntimeException {
    public AddressNotFoundException(String address) {
        super(address + "를 찾을수 없습니다.");
    }
}