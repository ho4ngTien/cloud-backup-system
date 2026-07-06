package com.cloudsafe.auth.user;

import com.cloudsafe.common.exception.AuthException;
import com.cloudsafe.common.exception.ResourceNotFoundException;
import com.cloudsafe.domain.entity.model.User;
import com.cloudsafe.domain.repository.repository.UserRepository;
import com.cloudsafe.auth.user.dto.UpdateUserRequest;
import com.cloudsafe.auth.user.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserDto getByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));
        return toDto(user);
    }

    @Transactional(readOnly = true)
    public UserDto getById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return toDto(user);
    }

    @Transactional
    public UserDto update(String email, UpdateUserRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));

        if (request.getDisplayName() != null) {
            user.setDisplayName(request.getDisplayName());
        }

        if (request.getNewPassword() != null) {
            if (request.getCurrentPassword() == null
                    || !passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
                throw new AuthException("Current password is incorrect");
            }
            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
            log.info("Password changed for user: {}", email);
        }

        userRepository.save(user);
        return toDto(user);
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional
    public void setEnabled(String userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        user.setEnabled(enabled);
        userRepository.save(user);
        log.info("User {} enabled={}", userId, enabled);
    }

    public UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .enabled(user.isEnabled())
                .storageQuotaBytes(user.getStorageQuotaBytes())
                .storageUsedBytes(user.getStorageUsedBytes())
                .createdAt(user.getCreatedAt().toString())
                .build();
    }
}
