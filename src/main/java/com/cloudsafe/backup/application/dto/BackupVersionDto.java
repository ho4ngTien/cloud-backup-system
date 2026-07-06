package com.cloudsafe.backup.application.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackupVersionDto {
    private String id;
    private int versionNumber;
    private boolean fullBackup;
    private int fileCount;
    private long totalSizeBytes;
    private String encryptionKeyRef;
    private String createdAt;
}
