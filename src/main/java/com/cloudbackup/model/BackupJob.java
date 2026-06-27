package com.cloudbackup.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "backup_jobs")
public class BackupJob {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String backupType;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(nullable = false)
    private long totalSizeBytes;

    @Column(nullable = false)
    private int version;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column
    private Instant completedAt;

    public BackupJob() {
    }
}
