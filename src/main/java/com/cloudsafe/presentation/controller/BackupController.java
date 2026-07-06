package com.cloudsafe.presentation.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudsafe.application.dto.CreateBackupRequest;
import com.cloudsafe.application.dto.CreateBackupResponse;
import com.cloudsafe.application.usecase.CreateBackupUseCase;

@RestController
@RequestMapping("/backups")
public class BackupController {
    private final CreateBackupUseCase createBackupUseCase;

    public BackupController(CreateBackupUseCase createBackupUseCase) {
        this.createBackupUseCase = createBackupUseCase;
    }

    @PostMapping
    public CreateBackupResponse create(@RequestBody CreateBackupRequest request) {
        return createBackupUseCase.execute(request);
    }
}
