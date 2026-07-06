package com.cloudsafe.infrastructure.persistence.repository;

import com.cloudsafe.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, String> {
    UserEntity findByEmail(String email);
}
