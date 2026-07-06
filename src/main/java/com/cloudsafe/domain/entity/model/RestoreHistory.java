package com.cloudsafe.domain.entity.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "restore_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestoreHistory {

    public enum RestoreType { FULL, SELECTIVE }
    public enum RestoreStatus { PENDING, IN_PROGRESS, COMPLETED, FAILED }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "backup_version_id", nullable = false)
    private BackupVersion backupVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RestoreType restoreType = RestoreType.FULL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RestoreStatus status = RestoreStatus.PENDING;

    /** Đường dẫn thư mục đích để restore. */
    @Column(length = 500)
    private String targetPath;

    /** Danh sách file cần restore (JSON array) khi restoreType = SELECTIVE. */
    @Column(columnDefinition = "TEXT")
    private String selectedFiles;

    @Column
    private String errorMessage;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant requestedAt = Instant.now();

    @Column
    private Instant completedAt;
}
