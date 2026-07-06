package com.cloudsafe.backup.application.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackupJobDto {
    private String id;
    private String deviceId;
    private String deviceName;
    private String userId;
    private String backupType;
    private String status;
    private long totalSizeBytes;
    private int fileCount;
    private int version;
    private String sourcePath;
    private String errorMessage;
    private String createdAt;
    private String completedAt;
}
