package com.cloudsafe.backup.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cloudsafe.backup.entity.BackupVersion;

@Repository
public interface BackupVersionRepository extends JpaRepository<BackupVersion, String> {
    List<BackupVersion> findByBackupJobIdOrderByVersionNumberDesc(String backupJobId);
}
