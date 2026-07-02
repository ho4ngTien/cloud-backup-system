package com.cloudsafe.exception;

/** Thrown for authentication/authorization failures (HTTP 401 / 403). */
public class AuthException extends RuntimeException {
    public AuthException(String message) {
        super(message);
    }
}
