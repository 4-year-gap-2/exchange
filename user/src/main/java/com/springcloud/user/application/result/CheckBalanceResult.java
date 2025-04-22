package com.springcloud.user.application.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
public class CheckBalanceResult {
    private boolean success;
    private String message;


}
