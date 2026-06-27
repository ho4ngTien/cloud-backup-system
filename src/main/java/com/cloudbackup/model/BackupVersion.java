package com.cloudbackup.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "backup_versions")
public class BackupVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "backup_job_id", nullable = false)
    private BackupJob backupJob;

    @Column(nullable = false)
    private int versionNumber;

    @Column(nullable = false)
    private boolean fullBackup;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public BackupVersion() {
    }
}
