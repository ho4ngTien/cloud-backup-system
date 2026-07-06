package com.cloudsafe.backup.presentation.request;

import com.cloudsafe.domain.entity.model.BackupJob;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class StartBackupRequest {

    @NotBlank(message = "Device ID is required")
    private String deviceId;

    @NotNull(message = "Backup type is required")
    private BackupJob.BackupType backupType;

    @NotBlank(message = "Source path is required")
    @Size(max = 500)
    private String sourcePath;
}
