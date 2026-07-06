package com.cloudsafe.presentation.controller;

import com.cloudsafe.application.dto.RegisterUserRequest;
import com.cloudsafe.application.dto.RegisterUserResponse;
import com.cloudsafe.application.usecase.RegisterUserUseCase;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final RegisterUserUseCase registerUserUseCase;

    public AuthController(RegisterUserUseCase registerUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
    }

    @PostMapping("/register")
    public RegisterUserResponse register(@RequestBody RegisterUserRequest request) {
        return registerUserUseCase.execute(request);
    }
}
