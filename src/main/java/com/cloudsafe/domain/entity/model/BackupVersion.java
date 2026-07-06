package com.cloudsafe.domain.entity.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "backup_versions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    @Builder.Default
    private boolean fullBackup = true;

    @Column(nullable = false)
    @Builder.Default
    private long totalSizeBytes = 0L;

    @Column(nullable = false)
    @Builder.Default
    private int fileCount = 0;

    /** AES-256 key (server-managed, encrypted with user's derived key). */
    @Column(length = 512)
    private String encryptionKeyRef;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
