package com.cloudsafe.repository;

import com.cloudsafe.model.BackupVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BackupVersionRepository extends JpaRepository<BackupVersion, String> {
    List<BackupVersion> findByBackupJobIdOrderByVersionNumberDesc(String backupJobId);
}
