package com.cloudsafe.backup.service;

import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudsafe.auth.entity.User;
import com.cloudsafe.auth.repository.UserRepository;
import com.cloudsafe.backup.dto.BackupFileDto;
import com.cloudsafe.backup.dto.BackupJobDto;
import com.cloudsafe.backup.dto.BackupVersionDto;
import com.cloudsafe.backup.dto.CommitBackupRequest;
import com.cloudsafe.backup.dto.StartBackupRequest;
import com.cloudsafe.backup.entity.BackupFile;
import com.cloudsafe.backup.entity.BackupJob;
import com.cloudsafe.backup.entity.BackupVersion;
import com.cloudsafe.backup.repository.BackupFileRepository;
import com.cloudsafe.backup.repository.BackupJobRepository;
import com.cloudsafe.backup.repository.BackupVersionRepository;
import com.cloudsafe.common.exception.ResourceNotFoundException;
import com.cloudsafe.common.exception.StorageLimitExceededException;
import com.cloudsafe.device.entity.Device;
import com.cloudsafe.device.repository.DeviceRepository;
import com.cloudsafe.notification.service.NotificationService;

@Service
@RequiredArgsConstructor
@Slf4j
public class BackupService {

    private final BackupJobRepository backupJobRepository;
    private final BackupVersionRepository backupVersionRepository;
    private final BackupFileRepository backupFileRepository;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * Creates a new backup job (PENDING status).
     * The CLI will then upload files to GCS and call commit().
     */
    @Transactional
    public BackupJobDto startBackup(String userEmail, StartBackupRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));
        Device device = deviceRepository.findById(request.getDeviceId())
                .orElseThrow(() -> new ResourceNotFoundException("Device", request.getDeviceId()));

        // Compute next version number
        List<BackupJob> existing = backupJobRepository.findByDeviceIdOrderByCreatedAtDesc(device.getId());
        int nextVersion = existing.isEmpty() ? 1 : existing.get(0).getVersion() + 1;

        BackupJob job = BackupJob.builder()
                .user(user)
                .device(device)
                .backupType(request.getBackupType())
                .sourcePath(request.getSourcePath())
                .status(BackupJob.BackupStatus.IN_PROGRESS)
                .version(nextVersion)
                .build();

        job = backupJobRepository.save(job);
        log.info("Started backup job: {} (type={}, device={})",
                job.getId(), job.getBackupType(), device.getId());
        return toDto(job);
    }

    /**
     * Called after CLI has finished uploading all files.
     * Saves file metadata, updates job status to COMPLETED, and sends notification.
     */
    @Transactional
    public BackupJobDto commitBackup(String userEmail, CommitBackupRequest request) {
        BackupJob job = backupJobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("BackupJob", request.getJobId()));

        if (!job.getUser().getEmail().equals(userEmail)) {
            throw new ResourceNotFoundException("BackupJob", request.getJobId());
        }

        // Compute total size
        long totalSize = request.getFiles().stream()
                .mapToLong(CommitBackupRequest.FileMetadata::getSizeBytes)
                .sum();

        // Check quota
        User user = job.getUser();
        long newUsed = user.getStorageUsedBytes() + totalSize;
        if (newUsed > user.getStorageQuotaBytes()) {
            job.setStatus(BackupJob.BackupStatus.FAILED);
            job.setErrorMessage("Storage quota exceeded");
            backupJobRepository.save(job);
            throw new StorageLimitExceededException(newUsed, user.getStorageQuotaBytes());
        }

        // Create backup version
        BackupVersion version = BackupVersion.builder()
                .backupJob(job)
                .versionNumber(job.getVersion())
                .fullBackup(job.getBackupType() == BackupJob.BackupType.FULL)
                .totalSizeBytes(totalSize)
                .fileCount(request.getFiles().size())
                .encryptionKeyRef(request.getEncryptionKey())
                .build();
        version = backupVersionRepository.save(version);

        // Save file metadata
        final BackupVersion savedVersion = version;
        List<BackupFile> files = request.getFiles().stream().map(f -> BackupFile.builder()
                .backupVersion(savedVersion)
                .relativePath(f.getRelativePath())
                .sha256Hash(f.getSha256Hash())
                .storageKey(f.getStorageKey())
                .sizeBytes(f.getSizeBytes())
                .encrypted(f.isEncrypted())
                .build()).toList();
        backupFileRepository.saveAll(files);

        // Update job status
        job.setStatus(BackupJob.BackupStatus.COMPLETED);
        job.setTotalSizeBytes(totalSize);
        job.setFileCount(request.getFiles().size());
        job.setCompletedAt(Instant.now());
        backupJobRepository.save(job);

        // Update user storage
        user.setStorageUsedBytes(newUsed);
        userRepository.save(user);

        log.info("Committed backup job: {} ({} files, {} bytes)", job.getId(),
                request.getFiles().size(), totalSize);

        // Send email notification asynchronously
        notificationService.sendBackupSuccess(user, job);

        return toDto(job);
    }

    /**
     * Mark a backup job as FAILED (called when CLI encounters an error).
     */
    @Transactional
    public void failBackup(String jobId, String userEmail, String errorMessage) {
        BackupJob job = backupJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("BackupJob", jobId));
        job.setStatus(BackupJob.BackupStatus.FAILED);
        job.setErrorMessage(errorMessage);
        job.setCompletedAt(Instant.now());
        backupJobRepository.save(job);
        notificationService.sendBackupFailed(job.getUser(), job, errorMessage);
    }

    @Transactional(readOnly = true)
    public Page<BackupJobDto> listJobs(String userEmail, int page, int size) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));
        Pageable pageable = PageRequest.of(page, size);
        return backupJobRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(this::toDto);
    }

    @Transactional(readOnly = true)
    public List<BackupVersionDto> listVersions(String jobId, String userEmail) {
        BackupJob job = backupJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("BackupJob", jobId));
        if (!job.getUser().getEmail().equals(userEmail)) {
            throw new ResourceNotFoundException("BackupJob", jobId);
        }
        return backupVersionRepository.findByBackupJobIdOrderByVersionNumberDesc(jobId)
                .stream().map(v -> BackupVersionDto.builder()
                        .id(v.getId())
                        .versionNumber(v.getVersionNumber())
                        .fullBackup(v.isFullBackup())
                        .fileCount(v.getFileCount())
                        .totalSizeBytes(v.getTotalSizeBytes())
                        .encryptionKeyRef(v.getEncryptionKeyRef())
                        .createdAt(v.getCreatedAt().toString())
                        .build()).toList();
    }

    @Transactional(readOnly = true)
    public List<BackupFileDto> listFiles(String versionId, String userEmail) {
        BackupVersion version = backupVersionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("BackupVersion", versionId));
        if (!version.getBackupJob().getUser().getEmail().equals(userEmail)) {
            throw new ResourceNotFoundException("BackupVersion", versionId);
        }
        return backupFileRepository.findByBackupVersionId(versionId)
                .stream().map(f -> BackupFileDto.builder()
                        .id(f.getId())
                        .relativePath(f.getRelativePath())
                        .sha256Hash(f.getSha256Hash())
                        .storageKey(f.getStorageKey())
                        .sizeBytes(f.getSizeBytes())
                        .encrypted(f.isEncrypted())
                        .createdAt(f.getCreatedAt().toString())
                        .build()).toList();
    }

    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getLatestCompletedFilesAndKey(String userEmail, String deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", deviceId));
        if (!device.getUser().getEmail().equals(userEmail)) {
            throw new ResourceNotFoundException("Device", deviceId);
        }

        java.util.Optional<BackupJob> latestJobOpt = backupJobRepository
                .findFirstByDeviceIdAndStatusOrderByCreatedAtDesc(deviceId, BackupJob.BackupStatus.COMPLETED);
        if (latestJobOpt.isEmpty()) {
            return java.util.Map.of("files", java.util.List.of());
        }

        List<BackupVersion> versions = backupVersionRepository
                .findByBackupJobIdOrderByVersionNumberDesc(latestJobOpt.get().getId());
        if (versions.isEmpty()) {
            return java.util.Map.of("files", java.util.List.of());
        }

        BackupVersion latestVersion = versions.get(0);
        List<BackupFileDto> files = listFiles(latestVersion.getId(), userEmail);

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("files", files);
        if (latestVersion.getEncryptionKeyRef() != null) {
            response.put("encryptionKey", latestVersion.getEncryptionKeyRef());
        }
        return response;
    }

    @Transactional(readOnly = true)
    public List<BackupJobDto> getPendingJobs(String userEmail, String deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", deviceId));
        if (!device.getUser().getEmail().equals(userEmail)) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied");
        }
        
        List<BackupJob> pendingJobs = backupJobRepository.findByDeviceIdAndStatus(deviceId, BackupJob.BackupStatus.PENDING);
        return pendingJobs.stream().map(this::toDto).toList();
    }

    public BackupJobDto toDto(BackupJob job) {
        return BackupJobDto.builder()
                .id(job.getId())
                .deviceId(job.getDevice().getId())
                .deviceName(job.getDevice().getName())
                .userId(job.getUser().getId())
                .backupType(job.getBackupType().name())
                .status(job.getStatus().name())
                .totalSizeBytes(job.getTotalSizeBytes())
                .fileCount(job.getFileCount())
                .version(job.getVersion())
                .sourcePath(job.getSourcePath())
                .errorMessage(job.getErrorMessage())
                .excludedPatterns(job.getDevice().getExcludedPatterns())
                .createdAt(job.getCreatedAt().toString())
                .completedAt(job.getCompletedAt() != null ? job.getCompletedAt().toString() : null)
                .build();
    }
}
