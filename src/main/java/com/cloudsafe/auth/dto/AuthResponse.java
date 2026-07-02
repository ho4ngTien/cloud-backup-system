package com.cloudsafe.auth.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String tokenType;
    private long expiresIn;   // seconds
    private String userId;
    private String email;
    private String displayName;
    private List<String> roles;
}
