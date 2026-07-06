package com.cloudsafe.restore.presentation.request;

import com.cloudsafe.domain.entity.model.RestoreHistory;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

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
