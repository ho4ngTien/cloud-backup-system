package com.cloudsafe.application.dto;

public record RegisterUserRequest(String username, String email, String passwordHash) {
}
