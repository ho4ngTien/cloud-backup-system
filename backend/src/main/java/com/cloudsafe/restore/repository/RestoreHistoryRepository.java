package com.cloudsafe.restore.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cloudsafe.restore.entity.RestoreHistory;

@Repository
public interface RestoreHistoryRepository extends JpaRepository<RestoreHistory, String> {
    Page<RestoreHistory> findByUserIdOrderByRequestedAtDesc(String userId, Pageable pageable);
}
