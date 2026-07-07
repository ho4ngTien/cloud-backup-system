package com.cloudsafe.dashboard.service;

import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudsafe.auth.entity.User;
import com.cloudsafe.auth.repository.UserRepository;
import com.cloudsafe.backup.entity.BackupJob;
import com.cloudsafe.backup.repository.BackupJobRepository;
import com.cloudsafe.common.exception.ResourceNotFoundException;
import com.cloudsafe.device.entity.Device;
import com.cloudsafe.device.repository.DeviceRepository;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final BackupJobRepository backupJobRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> getSummary(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));

        List<Device> devices = deviceRepository.findByUserId(user.getId());
        long totalDevices = devices.size();

        // Get total backup jobs
        List<BackupJob> jobs = backupJobRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), 
                org.springframework.data.domain.PageRequest.of(0, 1000)).getContent();
        long totalBackupJobs = jobs.size();

        // Find latest completed job details
        Map<String, Object> latestJobInfo = null;
        Optional<BackupJob> latestJob = jobs.stream()
                .filter(j -> j.getStatus() == BackupJob.BackupStatus.COMPLETED)
                .findFirst();
        if (latestJob.isPresent()) {
            BackupJob j = latestJob.get();
            latestJobInfo = Map.of(
                "jobId", j.getId(),
                "deviceName", j.getDevice().getName(),
                "completedAt", j.getCompletedAt() != null ? j.getCompletedAt().toString() : "",
                "totalSizeBytes", j.getTotalSizeBytes(),
                "fileCount", j.getFileCount()
            );
        }

        return Map.of(
            "totalDevices", totalDevices,
            "totalBackupJobs", totalBackupJobs,
            "storageUsedBytes", user.getStorageUsedBytes(),
            "storageQuotaBytes", user.getStorageQuotaBytes(),
            "storageAvailableBytes", user.getStorageQuotaBytes() - user.getStorageUsedBytes(),
            "latestBackupJob", latestJobInfo != null ? latestJobInfo : Map.of()
        );
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStorageStatistics(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));

        // Get all completed backup jobs for user
        List<BackupJob> jobs = backupJobRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), 
                org.springframework.data.domain.PageRequest.of(0, 1000)).getContent();
        List<BackupJob> completedJobs = jobs.stream()
                .filter(j -> j.getStatus() == BackupJob.BackupStatus.COMPLETED)
                .sorted(Comparator.comparing(BackupJob::getCreatedAt))
                .toList();

        List<Map<String, Object>> dataPoints = new ArrayList<>();
        long cumulativeSize = 0;
        for (BackupJob job : completedJobs) {
            cumulativeSize += job.getTotalSizeBytes();
            dataPoints.add(Map.of(
                "timestamp", job.getCreatedAt().toString(),
                "sizeBytes", job.getTotalSizeBytes(),
                "cumulativeSizeBytes", cumulativeSize,
                "deviceName", job.getDevice().getName()
            ));
        }

        return Map.of(
            "email", user.getEmail(),
            "storageUsedBytes", user.getStorageUsedBytes(),
            "storageQuotaBytes", user.getStorageQuotaBytes(),
            "history", dataPoints
        );
    }
}
