package com.cloudsafe.backup.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

import com.cloudsafe.auth.entity.User;
import com.cloudsafe.device.entity.Device;

@Entity
@Table(name = "backup_jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BackupJob {

    public enum BackupType { FULL, INCREMENTAL }
    public enum BackupStatus { PENDING, IN_PROGRESS, COMPLETED, FAILED, CANCELLED }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BackupType backupType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BackupStatus status = BackupStatus.PENDING;

    @Column(nullable = false)
    @Builder.Default
    private long totalSizeBytes = 0L;

    @Column(nullable = false)
    @Builder.Default
    private int fileCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private int version = 1;

    /** Thư mục nguồn trên thiết bị client. */
    @Column(length = 500)
    private String sourcePath;

    @Column
    private String errorMessage;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column
    private Instant completedAt;
}
