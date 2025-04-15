package com.springcloud.user.presentation.request;

import com.springcloud.user.application.command.UpdateIncrementBalanceCommand;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class UpdateIncrementBalanceRequest {
    @Comment("지갑 주소")
    @NotNull
    private String wallet;

    @Positive(message = "금액은 0보다 커야 합니다")
    private BigDecimal amount;
}
