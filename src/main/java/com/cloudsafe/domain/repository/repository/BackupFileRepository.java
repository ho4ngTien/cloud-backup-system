package com.cloudsafe.domain.repository.repository;

import com.cloudsafe.domain.entity.model.BackupFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BackupFileRepository extends JpaRepository<BackupFile, String> {
    List<BackupFile> findByBackupVersionId(String backupVersionId);
    Optional<BackupFile> findByStorageKey(String storageKey);
    boolean existsBySha256Hash(String sha256Hash);
}
