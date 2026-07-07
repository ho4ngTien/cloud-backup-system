package com.cloudsafe.backup.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cloudsafe.backup.entity.BackupFile;

@Repository
public interface BackupFileRepository extends JpaRepository<BackupFile, String> {
    List<BackupFile> findByBackupVersionId(String backupVersionId);
    Optional<BackupFile> findByStorageKey(String storageKey);
    boolean existsBySha256Hash(String sha256Hash);
}
