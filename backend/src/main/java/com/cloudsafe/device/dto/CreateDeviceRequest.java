package com.cloudsafe.device.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import com.cloudsafe.device.entity.Device;

@Data
public class CreateDeviceRequest {

    @NotBlank(message = "Device name is required")
    @Size(max = 200)
    private String name;

    @NotBlank(message = "Operating system is required")
    @Size(max = 100)
    private String operatingSystem;

    @Size(max = 500)
    private String watchPath;

    private boolean autoBackupEnabled = false;

    @Size(max = 1000)
    private String excludedPatterns;
}
