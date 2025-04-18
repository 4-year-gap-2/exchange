package com.springcloud.user.application.command;

import com.springcloud.user.domain.entity.Fee;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ManagementCommandService {

    public void createFee(BigDecimal feeValue) {
        Fee fee = Fee.builder()
                .feeValue(feeValue)  // 필수 값 설정
                .build();


    }
}
