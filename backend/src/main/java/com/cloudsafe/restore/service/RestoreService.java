package com.cloudsafe.restore.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudsafe.audit.entity.AuditLog;
import com.cloudsafe.audit.service.AuditLogService;
import com.cloudsafe.auth.entity.User;
import com.cloudsafe.auth.repository.UserRepository;
import com.cloudsafe.backup.entity.BackupFile;
import com.cloudsafe.backup.entity.BackupVersion;
import com.cloudsafe.backup.repository.BackupFileRepository;
import com.cloudsafe.backup.repository.BackupVersionRepository;
import com.cloudsafe.common.exception.ResourceNotFoundException;
import com.cloudsafe.device.entity.Device;
import com.cloudsafe.device.repository.DeviceRepository;
import com.cloudsafe.notification.service.NotificationService;
import com.cloudsafe.restore.dto.RestoreJobDto;
import com.cloudsafe.restore.dto.StartRestoreRequest;
import com.cloudsafe.restore.entity.RestoreHistory;
import com.cloudsafe.restore.repository.RestoreHistoryRepository;
import com.cloudsafe.storage.service.StorageService;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestoreService {

    private final RestoreHistoryRepository restoreHistoryRepository;
    private final BackupVersionRepository backupVersionRepository;
    private final BackupFileRepository backupFileRepository;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    /**
     * Creates a restore job and returns signed download URLs for all requested files.
     * The CLI uses these URLs to download, decrypt, and restore the files locally.
     */
    @Transactional
    public RestoreJobDto startRestore(String userEmail, StartRestoreRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));
        BackupVersion version = backupVersionRepository.findById(request.getBackupVersionId())
                .orElseThrow(() -> new ResourceNotFoundException("BackupVersion", request.getBackupVersionId()));
        Device device = deviceRepository.findById(request.getDeviceId())
                .orElseThrow(() -> new ResourceNotFoundException("Device", request.getDeviceId()));

        // Ownership check
        if (!version.getBackupJob().getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("BackupVersion", request.getBackupVersionId());
        }

        // Build restore record
        RestoreHistory restore = RestoreHistory.builder()
                .backupVersion(version)
                .user(user)
                .device(device)
                .restoreType(request.getRestoreType())
                .targetPath(request.getTargetPath())
                .selectedFiles(request.getSelectedFiles() != null
                        ? String.join(",", request.getSelectedFiles()) : null)
                .status(RestoreHistory.RestoreStatus.IN_PROGRESS)
                .build();
        restore = restoreHistoryRepository.save(restore);

        // Fetch file list
        List<BackupFile> allFiles = backupFileRepository.findByBackupVersionId(version.getId());
        List<BackupFile> filesToRestore = filterFiles(allFiles, request);

        // Generate signed download URLs (15 minutes validity)
        List<RestoreJobDto.RestoreFileInfo> fileInfos = filesToRestore.stream()
                .map(f -> RestoreJobDto.RestoreFileInfo.builder()
                        .relativePath(f.getRelativePath())
                        .sha256Hash(f.getSha256Hash())
                        .sizeBytes(f.getSizeBytes())
                        .encrypted(f.isEncrypted())
                        .downloadUrl(storageService.generateSignedUrl(
                                f.getStorageKey(), Duration.ofMinutes(15)).toString())
                        .build())
                .collect(Collectors.toList());

        log.info("Restore started: {} ({} files) for user: {}",
                restore.getId(), filesToRestore.size(), userEmail);

        auditLogService.log(userEmail, AuditLog.EventType.RESTORE_REQUEST,
                "Requested restore of version " + version.getVersionNumber() + " (Job ID: " + version.getBackupJob().getId() + ") for device: " + device.getName());

        return toDto(restore, version, fileInfos);
    }

    /**
     * Called by CLI when restore is done. Updates status and sends notification.
     */
    @Transactional
    public void completeRestore(String restoreId, String userEmail, boolean success, String errorMsg) {
        RestoreHistory restore = restoreHistoryRepository.findById(restoreId)
                .orElseThrow(() -> new ResourceNotFoundException("RestoreHistory", restoreId));

        restore.setStatus(success
                ? RestoreHistory.RestoreStatus.COMPLETED
                : RestoreHistory.RestoreStatus.FAILED);
        restore.setErrorMessage(errorMsg);
        restore.setCompletedAt(Instant.now());
        restoreHistoryRepository.save(restore);

        if (success) {
            notificationService.sendRestoreSuccess(restore.getUser(), restore);
        }
        log.info("Restore {}: {} for user {}", restoreId, restore.getStatus(), userEmail);
    }

    @Transactional(readOnly = true)
    public Page<RestoreJobDto> listHistory(String userEmail, int page, int size) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));
        Pageable pageable = PageRequest.of(page, size);
        return restoreHistoryRepository
                .findByUserIdOrderByRequestedAtDesc(user.getId(), pageable)
                .map(r -> toDto(r, r.getBackupVersion(), null));
    }

    // ===================== helpers =====================

    private List<BackupFile> filterFiles(List<BackupFile> all, StartRestoreRequest request) {
        if (request.getRestoreType() == RestoreHistory.RestoreType.FULL
                || request.getSelectedFiles() == null
                || request.getSelectedFiles().isEmpty()) {
            return all;
        }
        return all.stream()
                .filter(f -> request.getSelectedFiles().contains(f.getRelativePath()))
                .collect(Collectors.toList());
    }

    private RestoreJobDto toDto(RestoreHistory r, BackupVersion v,
                                List<RestoreJobDto.RestoreFileInfo> files) {
        return RestoreJobDto.builder()
                .id(r.getId())
                .backupVersionId(v.getId())
                .versionNumber(v.getVersionNumber())
                .deviceId(r.getDevice().getId())
                .userId(r.getUser().getId())
                .restoreType(r.getRestoreType().name())
                .status(r.getStatus().name())
                .targetPath(r.getTargetPath())
                .errorMessage(r.getErrorMessage())
                .requestedAt(r.getRequestedAt().toString())
                .completedAt(r.getCompletedAt() != null ? r.getCompletedAt().toString() : null)
                .encryptionKey(v.getEncryptionKeyRef())
                .files(files)
                .build();
    }
}
