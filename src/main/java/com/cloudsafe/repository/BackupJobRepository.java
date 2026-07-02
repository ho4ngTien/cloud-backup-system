package com.cloudsafe.repository;

import com.cloudsafe.model.BackupJob;
import com.cloudsafe.model.BackupJob.BackupStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BackupJobRepository extends JpaRepository<BackupJob, String> {
    Page<BackupJob> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    List<BackupJob> findByDeviceIdOrderByCreatedAtDesc(String deviceId);
    List<BackupJob> findByUserIdAndStatus(String userId, BackupStatus status);
}
