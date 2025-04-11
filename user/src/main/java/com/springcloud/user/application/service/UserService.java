package com.springcloud.user.application.service;

import com.springcloud.user.application.command.CreateUserCommand;
import com.springcloud.user.application.command.LoginUserCommand;
import com.springcloud.user.application.result.FindUserResult;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    FindUserResult signUp(CreateUserCommand command);

    void login(LoginUserCommand command, HttpServletResponse httpServletResponse);
}
