package com.cloudsafe.backup.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import com.cloudsafe.backup.entity.BackupJob;
import com.cloudsafe.device.entity.Device;

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
