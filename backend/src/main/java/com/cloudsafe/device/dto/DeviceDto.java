package com.cloudsafe.device.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceDto {
    private String id;
    private String name;
    private String operatingSystem;
    private boolean online;
    private String watchPath;
    private boolean autoBackupEnabled;
    private String userId;
    private String excludedPatterns;
    private String createdAt;
    private String lastSeenAt;
}
