package com.springcloud.user.application.service;

import com.springcloud.user.application.command.CreateUserCommand;
import com.springcloud.user.application.result.FindUserResult;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    FindUserResult signup(CreateUserCommand command);
}
