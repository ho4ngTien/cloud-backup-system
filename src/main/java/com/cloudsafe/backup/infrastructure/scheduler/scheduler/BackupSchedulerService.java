package com.cloudsafe.backup.infrastructure.scheduler.scheduler;

import com.cloudsafe.backup.application.service.BackupService;
import com.cloudsafe.backup.presentation.request.StartBackupRequest;
import com.cloudsafe.domain.entity.model.BackupJob;
import com.cloudsafe.domain.entity.model.Device;
import com.cloudsafe.domain.repository.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Automatically triggers a backup job for every device that has autoBackupEnabled=true.
 * Runs daily at midnight (server time). The CLI agent is expected to pick up
 * PENDING jobs on next heartbeat and execute the actual file upload.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BackupSchedulerService {

    private final DeviceRepository deviceRepository;
    private final BackupService backupService;

    /** Runs every day at 00:00 server time. Cron: sec min hour day month weekday */
    @Scheduled(cron = "0 0 0 * * *")
    public void triggerScheduledBackups() {
        List<Device> devices = deviceRepository.findByAutoBackupEnabledTrue();
        log.info("Scheduler: found {} devices with auto-backup enabled", devices.size());

        for (Device device : devices) {
            try {
                StartBackupRequest req = new StartBackupRequest();
                req.setDeviceId(device.getId());
                req.setBackupType(BackupJob.BackupType.INCREMENTAL);
                req.setSourcePath(device.getWatchPath() != null ? device.getWatchPath() : "/");

                backupService.startBackup(device.getUser().getEmail(), req);
                log.info("Scheduler: triggered backup for device {} (user: {})",
                        device.getId(), device.getUser().getEmail());
            } catch (Exception e) {
                log.error("Scheduler: failed to trigger backup for device {}: {}",
                        device.getId(), e.getMessage());
            }
        }
    }

    /** Cleanup: runs every Sunday at 02:00 – can be extended to delete old versions */
    @Scheduled(cron = "0 0 2 * * SUN")
    public void weeklyCleanup() {
        log.info("Scheduler: running weekly cleanup job");
        // TODO: delete backup versions older than retention policy
    }
}
