package com.cloudsafe.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cloudsafe.infrastructure.persistence.entity.BackupJobEntity;

public interface BackupJobJpaRepository extends JpaRepository<BackupJobEntity, String> {
}
