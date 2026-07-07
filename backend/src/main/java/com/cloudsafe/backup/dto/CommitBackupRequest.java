package com.cloudsafe.backup.dto;

import jakarta.validation.constraints.*;
import java.util.List;
import lombok.Data;

/**
 * Sent by the CLI agent after all files have been uploaded to GCS.
 * Contains metadata for each file so the server can persist them.
 */
@Data
public class CommitBackupRequest {

    @NotBlank
    private String jobId;

    private String encryptionKey;

    @NotNull
    private List<FileMetadata> files;

    @Data
    public static class FileMetadata {
        @NotBlank private String relativePath;
        @NotBlank private String sha256Hash;
        @NotBlank private String storageKey;
        @Positive  private long sizeBytes;
        private boolean encrypted = true;
    }
}
