package com.springcloud.user.presentation.controller;

import com.springcloud.user.application.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/management")
public class ManagementController {

    private final UserService userService;

    @PostMapping("/fee")
    public String createFee(@RequestParam BigDecimal feeValue) {
        userService.createFee(feeValue);
        return null;
    }
}
