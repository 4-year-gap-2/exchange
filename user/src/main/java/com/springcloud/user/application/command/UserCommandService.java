package com.springcloud.user.application.command;

import com.springcloud.user.application.result.FindUserResult;
import com.springcloud.user.domain.entity.User;
import com.springcloud.user.domain.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@AllArgsConstructor
@Service
public class UserCommandService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public FindUserResult signup(CreateUserCommand command) {
        Optional<User> checkUsername = userRepository.findByUsername(command.getUsername());
        if (checkUsername.isPresent()) throw new IllegalArgumentException("이미 사용 중인 사용자 이름입니다.");

        String encodedPassword = passwordEncoder.encode(command.getPassword());
        User user = command.toEntity(encodedPassword);
        User savedUser = userRepository.save(user);

        return new FindUserResult(savedUser.getUserId(),savedUser.getUsername(),savedUser.getPhone());
    }
}
