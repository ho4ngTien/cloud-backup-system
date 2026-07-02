package com.cloudsafe.backup.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackupFileDto {
    private String id;
    private String relativePath;
    private String sha256Hash;
    private String storageKey;
    private long sizeBytes;
    private boolean encrypted;
    private String createdAt;
}
