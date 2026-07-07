package com.cloudsafe.audit.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

import com.cloudsafe.auth.entity.User;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    public enum EventType {
        LOGIN_SUCCESS,
        LOGIN_FAILED,
        REGISTER_SUCCESS,
        RESTORE_REQUEST,
        DEVICE_REGISTERED,
        DEVICE_REMOVED,
        USER_BLOCKED,
        USER_UNBLOCKED,
        STORAGE_WARNING
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(nullable = false, length = 45)
    private String ipAddress;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
