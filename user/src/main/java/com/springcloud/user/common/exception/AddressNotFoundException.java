package com.springcloud.user.common.exception;

public class AddressNotFoundException extends RuntimeException {
    public AddressNotFoundException(String address) {
        super(address + "를 찾을수 없습니다.");
    }
}