package com.cloudsafe.restore.application.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestoreJobDto {
    private String id;
    private String backupVersionId;
    private int versionNumber;
    private String deviceId;
    private String userId;
    private String restoreType;
    private String status;
    private String targetPath;
    private String errorMessage;
    private String requestedAt;
    private String completedAt;
    /** Download URLs for each file (signed GCS URLs, 15 min validity). */
    private List<RestoreFileInfo> files;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RestoreFileInfo {
        private String relativePath;
        private String sha256Hash;
        private long sizeBytes;
        private String downloadUrl;
        private boolean encrypted;
    }
}
