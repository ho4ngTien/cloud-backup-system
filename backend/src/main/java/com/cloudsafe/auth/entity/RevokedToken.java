package com.cloudsafe.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "revoked_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevokedToken {

    @Id
    @Column(name = "token_id", length = 500)
    private String tokenId;

    @Column(name = "revoked_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant revokedAt = Instant.now();

    @Column(name = "expiry_time", nullable = false)
    private Instant expiryTime;
}
