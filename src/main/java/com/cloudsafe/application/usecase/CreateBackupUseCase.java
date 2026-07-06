package com.cloudsafe.application.usecase;

import com.cloudsafe.application.dto.CreateBackupRequest;
import com.cloudsafe.application.dto.CreateBackupResponse;
import com.cloudsafe.domain.model.BackupJob;
import com.cloudsafe.domain.repository.BackupJobRepositoryPort;

public class CreateBackupUseCase {
    private final BackupJobRepositoryPort backupJobRepositoryPort;

    public CreateBackupUseCase(BackupJobRepositoryPort backupJobRepositoryPort) {
        this.backupJobRepositoryPort = backupJobRepositoryPort;
    }

    public CreateBackupResponse execute(CreateBackupRequest request) {
        BackupJob backupJob = new BackupJob(
                java.util.UUID.randomUUID().toString(),
                request.userId(),
                request.deviceId(),
                "PENDING",
                request.storagePath()
        );
        BackupJob saved = backupJobRepositoryPort.save(backupJob);
        return new CreateBackupResponse(saved.getId(), saved.getStatus());
    }
}
