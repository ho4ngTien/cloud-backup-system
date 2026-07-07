package com.cloudsafe.auth.repository;

import com.cloudsafe.auth.entity.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface RevokedTokenRepository extends JpaRepository<RevokedToken, String> {
    boolean existsByTokenId(String tokenId);
    void deleteByExpiryTimeBefore(Instant now);
}
