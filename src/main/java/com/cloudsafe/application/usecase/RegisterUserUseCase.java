package com.cloudsafe.application.usecase;

import com.cloudsafe.application.dto.RegisterUserRequest;
import com.cloudsafe.application.dto.RegisterUserResponse;
import com.cloudsafe.domain.model.User;
import com.cloudsafe.domain.repository.UserRepositoryPort;

public class RegisterUserUseCase {
    private final UserRepositoryPort userRepositoryPort;

    public RegisterUserUseCase(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    public RegisterUserResponse execute(RegisterUserRequest request) {
        User user = new User(
                java.util.UUID.randomUUID().toString(),
                request.username(),
                request.email(),
                request.passwordHash()
        );
        User savedUser = userRepositoryPort.save(user);
        return new RegisterUserResponse(savedUser.getId(), savedUser.getEmail());
    }
}
