package com.cloudsafe.domain.repository;

import com.cloudsafe.domain.model.BackupJob;

public interface BackupJobRepositoryPort {
    BackupJob save(BackupJob backupJob);
    BackupJob findById(String id);
}
