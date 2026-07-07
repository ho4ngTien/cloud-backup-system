package com.cloudsafe.backup.dto;

import java.util.List;
import lombok.*;

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
    private String excludedPatterns;
    private String createdAt;
    private String completedAt;
}
