package com.cloudsafe.domain.repository.repository;

import com.cloudsafe.domain.entity.model.BackupVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BackupVersionRepository extends JpaRepository<BackupVersion, String> {
    List<BackupVersion> findByBackupJobIdOrderByVersionNumberDesc(String backupJobId);
}
