package com.cloudsafe.restore.dto;

import jakarta.validation.constraints.*;
import java.util.List;
import lombok.Data;

import com.cloudsafe.device.entity.Device;
import com.cloudsafe.restore.entity.RestoreHistory;

@Data
public class StartRestoreRequest {

    @NotBlank(message = "Backup version ID is required")
    private String backupVersionId;

    @NotBlank(message = "Device ID is required")
    private String deviceId;

    @NotNull(message = "Restore type is required")
    private RestoreHistory.RestoreType restoreType;

    @NotBlank(message = "Target path is required")
    @Size(max = 500)
    private String targetPath;

    /** Required when restoreType = SELECTIVE. List of relativePaths to restore. */
    private List<String> selectedFiles;
}
