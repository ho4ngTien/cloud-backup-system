package com.cloudsafe.device.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

import com.cloudsafe.auth.entity.User;

@Entity
@Table(name = "devices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 100)
    private String operatingSystem;

    @Column(nullable = false)
    @Builder.Default
    private boolean online = false;

    /** Thư mục được giám sát để backup tự động. */
    @Column(length = 500)
    private String watchPath;

    /** Bật/tắt backup tự động theo lịch. */
    @Column(nullable = false)
    @Builder.Default
    private boolean autoBackupEnabled = false;

    @Column(name = "excluded_patterns", length = 1000)
    private String excludedPatterns;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column
    private Instant lastSeenAt;

    @PreUpdate
    protected void onUpdate() {
        if (online) {
            this.lastSeenAt = Instant.now();
        }
    }
}
