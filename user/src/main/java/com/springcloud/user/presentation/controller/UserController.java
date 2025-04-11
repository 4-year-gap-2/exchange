package com.springcloud.user.presentation.controller;

import com.springcloud.user.application.command.CreateUserCommand;
import com.springcloud.user.application.result.FindUserResult;
import com.springcloud.user.application.service.UserService;
import com.springcloud.user.application.service.UserServiceImpl;
import com.springcloud.user.presentation.request.CreateUserRequest;
import com.springcloud.user.presentation.response.CreateUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public CreateUserResponse signUp(@RequestBody CreateUserRequest request) {
        CreateUserCommand command = request.toCommand();
        FindUserResult result = userService.signup(command);
        CreateUserResponse response = new CreateUserResponse(result.getUserId(),result.getUsername(),result.getPhone());

        return ResponseEntity.ok(response).getBody();
    }





}
