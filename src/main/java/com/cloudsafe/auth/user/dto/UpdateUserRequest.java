package com.cloudsafe.auth.user.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @Size(min = 2, max = 100, message = "Display name must be between 2 and 100 characters")
    private String displayName;

    @Size(min = 8, max = 128, message = "Password must be at least 8 characters")
    private String newPassword;

    /** Must be provided when changing password. */
    private String currentPassword;
}
