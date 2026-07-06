package com.cloudsafe.domain.repository.repository;

import com.cloudsafe.domain.entity.model.RestoreHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestoreHistoryRepository extends JpaRepository<RestoreHistory, String> {
    Page<RestoreHistory> findByUserIdOrderByRequestedAtDesc(String userId, Pageable pageable);
}
