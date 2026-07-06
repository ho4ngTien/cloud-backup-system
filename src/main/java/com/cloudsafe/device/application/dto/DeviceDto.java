package com.cloudsafe.device.application.dto;

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
    private String createdAt;
    private String lastSeenAt;
}
