package com.cloudsafe.backup.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cloudsafe.backup.entity.BackupJob;

@Repository
public interface BackupJobRepository extends JpaRepository<BackupJob, String> {
    Page<BackupJob> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    List<BackupJob> findByDeviceIdOrderByCreatedAtDesc(String deviceId);
    List<BackupJob> findByUserIdAndStatus(String userId, BackupJob.BackupStatus status);
    java.util.Optional<BackupJob> findFirstByDeviceIdAndStatusOrderByCreatedAtDesc(String deviceId, BackupJob.BackupStatus status);
    List<BackupJob> findByDeviceIdAndStatus(String deviceId, BackupJob.BackupStatus status);
}
