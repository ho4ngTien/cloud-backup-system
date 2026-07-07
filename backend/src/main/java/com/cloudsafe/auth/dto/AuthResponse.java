package com.cloudsafe.auth.dto;

import java.util.List;
import lombok.*;

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
