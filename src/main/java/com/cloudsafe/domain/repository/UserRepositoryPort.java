package com.cloudsafe.domain.repository;

import com.cloudsafe.domain.model.User;

public interface UserRepositoryPort {
    User save(User user);
    User findByEmail(String email);
}
