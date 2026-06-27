package com.cloudbackup.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "backup_files")
public class BackupFile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "backup_version_id", nullable = false)
    private BackupVersion backupVersion;

    @Column(nullable = false, length = 1000)
    private String path;

    @Column(nullable = false)
    private long sizeBytes;

    @Column(nullable = false, length = 1000)
    private String storageKey;

    @Column(nullable = false)
    private boolean encrypted;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public BackupFile() {
    }
}
