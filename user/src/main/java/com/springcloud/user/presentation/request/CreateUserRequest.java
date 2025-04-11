package com.springcloud.user.presentation.request;

import com.springcloud.user.application.command.CreateUserCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CreateUserRequest {

    @NotBlank(message = "Username은 필수 입력값입니다.")
    @Size(min = 4, max = 10, message = "Username은 최소 4자 이상, 10자 이하여야 합니다.")
    @Pattern(regexp = "^[a-z0-9]+$", message = "Username은 알파벳 소문자(a~z)와 숫자(0~9)만 포함할 수 있습니다.")
    private String username;

    @NotBlank(message = "Password는 필수 입력값입니다.")
    @Size(min = 8, max = 15, message = "Password는 최소 8자 이상, 15자 이하여야 합니다.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$", message = "Password는 알파벳 대소문자(A~Z, a~z), 숫자(0~9), 특수문자를 포함해야 합니다.")
    private String password;

    @NotBlank(message = "휴대폰 번호는 필수 입력값입니다")
    @Pattern(regexp = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$", message = "010-XXXX-XXXX 형식을 따라야 합니다")
    private String phone;

    @NotBlank(message = "Email은 필수 입력값입니다.")
    @Email(message = "올바른 이메일 형식을 입력해 주세요.")
    private String email;

    @NotBlank(message = "계좌번호는 필수 입력값입니다.")
    @Pattern(regexp = "^\\d{3}-\\d{6}-\\d{2}-\\d{3}$",
            message = "계좌번호는 XXX-XXXXXX-XX-XXX 형식이어야 합니다")
    private String bankAccountNumber;

    public CreateUserCommand toCommand() {
        return CreateUserCommand.builder()
                .username(username)
                .password(password)
                .phone(phone)
                .email(email)
                .bankAccountNumber(bankAccountNumber)
                .build();
    }
}
