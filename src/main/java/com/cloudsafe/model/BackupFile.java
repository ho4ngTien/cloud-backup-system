package com.cloudsafe.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "backup_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BackupFile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "backup_version_id", nullable = false)
    private BackupVersion backupVersion;

    /** Đường dẫn tương đối so với sourcePath trên client. */
    @Column(nullable = false, length = 1000)
    private String relativePath;

    @Column(nullable = false)
    private long sizeBytes;

    /** SHA-256 hash của file gốc (trước khi nén/mã hóa). */
    @Column(nullable = false, length = 64)
    private String sha256Hash;

    /** Key trên Cloud Storage (path trong bucket). */
    @Column(nullable = false, unique = true, length = 500)
    private String storageKey;

    @Column(nullable = false)
    @Builder.Default
    private boolean encrypted = true;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
