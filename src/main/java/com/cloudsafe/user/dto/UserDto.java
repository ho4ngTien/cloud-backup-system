package com.cloudsafe.user.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String id;
    private String email;
    private String displayName;
    private boolean enabled;
    private long storageQuotaBytes;
    private long storageUsedBytes;
    private String createdAt;
}
