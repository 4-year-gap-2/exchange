package com.springcloud.user.application.service;

import com.springcloud.user.application.command.CreateUserCommand;
import com.springcloud.user.application.command.UserCommandService;
import com.springcloud.user.application.query.UserQueryService;
import com.springcloud.user.application.result.FindUserResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;

    @Override
    public FindUserResult signUp(CreateUserCommand command) {
        return userCommandService.signUp(command);
    }
}
