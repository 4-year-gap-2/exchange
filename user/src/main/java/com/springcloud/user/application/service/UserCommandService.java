package com.springcloud.user.application.service;

import com.springcloud.user.application.command.CreateUserCommand;
import com.springcloud.user.application.command.LoginUserCommand;
import com.springcloud.user.application.result.FindUserResult;
import com.springcloud.user.domain.entity.User;
import com.springcloud.user.domain.repository.UserRepository;
import com.springcloud.user.security.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@AllArgsConstructor
@Service
public class UserCommandService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public FindUserResult signUp(CreateUserCommand command) {
        // 1. 유저 중복 확인
        Optional<User> checkUsername = userRepository.findByUsername(command.getUsername());
        if (checkUsername.isPresent()) throw new IllegalArgumentException("이미 사용 중인 사용자 이름입니다.");
        // 2. 유저 생성
        String encodedPassword = passwordEncoder.encode(command.getPassword());
        User user = command.toEntity(encodedPassword);

        // 3. 저장
        User savedUser = userRepository.save(user);

        return new FindUserResult(savedUser.getUserId(),savedUser.getUsername(),savedUser.getPhone(),savedUser.getEmail(),savedUser.getBalances());
    }

    public void login(LoginUserCommand command, HttpServletResponse httpServletResponse) {
        User user = userRepository.findByUsername(command.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 사용자입니다."));

        if (!passwordEncoder.matches(command.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // JWT를 생성하고 쿠키에 저장한 후 HttpServletResponse에 추가하여 반환
        String token = jwtUtil.createToken(user.getUserId(), user.getRole(), user.getUsername());
        jwtUtil.addJwtToCookie(token, httpServletResponse);
    }
}
