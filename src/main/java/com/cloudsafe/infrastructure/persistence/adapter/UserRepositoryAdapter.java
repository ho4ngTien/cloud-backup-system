package com.cloudsafe.infrastructure.persistence.adapter;

import com.cloudsafe.domain.model.User;
import com.cloudsafe.domain.repository.UserRepositoryPort;
import com.cloudsafe.infrastructure.persistence.entity.UserEntity;
import com.cloudsafe.infrastructure.persistence.repository.UserJpaRepository;
import org.springframework.stereotype.Component;

@Component
public class UserRepositoryAdapter implements UserRepositoryPort {
    private final UserJpaRepository userJpaRepository;

    public UserRepositoryAdapter(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public User save(User user) {
        UserEntity entity = new UserEntity(user.getId(), user.getUsername(), user.getEmail(), user.getPasswordHash());
        UserEntity saved = userJpaRepository.save(entity);
        return new User(saved.getId(), saved.getUsername(), saved.getEmail(), saved.getPasswordHash());
    }

    @Override
    public User findByEmail(String email) {
        UserEntity entity = userJpaRepository.findByEmail(email);
        if (entity == null) {
            return null;
        }
        return new User(entity.getId(), entity.getUsername(), entity.getEmail(), entity.getPasswordHash());
    }
}
