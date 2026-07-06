package com.cloudsafe.infrastructure.persistence.adapter;

import org.springframework.stereotype.Component;

import com.cloudsafe.domain.model.BackupJob;
import com.cloudsafe.domain.repository.BackupJobRepositoryPort;
import com.cloudsafe.infrastructure.persistence.entity.BackupJobEntity;
import com.cloudsafe.infrastructure.persistence.repository.BackupJobJpaRepository;

@Component
public class BackupJobRepositoryAdapter implements BackupJobRepositoryPort {
    private final BackupJobJpaRepository backupJobJpaRepository;

    public BackupJobRepositoryAdapter(BackupJobJpaRepository backupJobJpaRepository) {
        this.backupJobJpaRepository = backupJobJpaRepository;
    }

    @Override
    public BackupJob save(BackupJob backupJob) {
        BackupJobEntity entity = new BackupJobEntity(
                backupJob.getId(),
                backupJob.getUserId(),
                backupJob.getDeviceId(),
                backupJob.getStatus(),
                backupJob.getStoragePath()
        );
        BackupJobEntity saved = backupJobJpaRepository.save(entity);
        return new BackupJob(saved.getId(), saved.getUserId(), saved.getDeviceId(), saved.getStatus(), saved.getStoragePath());
    }

    @Override
    public BackupJob findById(String id) {
        return backupJobJpaRepository.findById(id)
                .map(entity -> new BackupJob(entity.getId(), entity.getUserId(), entity.getDeviceId(), entity.getStatus(), entity.getStoragePath()))
                .orElse(null);
    }
}
