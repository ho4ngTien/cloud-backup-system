package com.cloudsafe.device;

import com.cloudsafe.device.dto.CreateDeviceRequest;
import com.cloudsafe.device.dto.DeviceDto;
import com.cloudsafe.exception.ResourceNotFoundException;
import com.cloudsafe.model.Device;
import com.cloudsafe.model.User;
import com.cloudsafe.repository.DeviceRepository;
import com.cloudsafe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;

    @Transactional
    public DeviceDto register(String userEmail, CreateDeviceRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));

        Device device = Device.builder()
                .name(request.getName())
                .operatingSystem(request.getOperatingSystem())
                .watchPath(request.getWatchPath())
                .autoBackupEnabled(request.isAutoBackupEnabled())
                .user(user)
                .online(true)
                .build();

        device = deviceRepository.save(device);
        log.info("Device registered: {} for user: {}", device.getId(), userEmail);
        return toDto(device);
    }

    @Transactional(readOnly = true)
    public List<DeviceDto> listByUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));
        return deviceRepository.findByUserId(user.getId()).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public DeviceDto getById(String deviceId, String userEmail) {
        Device device = findDeviceOwnedBy(deviceId, userEmail);
        return toDto(device);
    }

    @Transactional
    public DeviceDto update(String deviceId, String userEmail, CreateDeviceRequest request) {
        Device device = findDeviceOwnedBy(deviceId, userEmail);
        if (request.getName() != null) device.setName(request.getName());
        if (request.getWatchPath() != null) device.setWatchPath(request.getWatchPath());
        device.setAutoBackupEnabled(request.isAutoBackupEnabled());
        return toDto(deviceRepository.save(device));
    }

    @Transactional
    public void delete(String deviceId, String userEmail) {
        Device device = findDeviceOwnedBy(deviceId, userEmail);
        deviceRepository.delete(device);
        log.info("Device deleted: {}", deviceId);
    }

    @Transactional
    public void heartbeat(String deviceId, String userEmail, boolean online) {
        Device device = findDeviceOwnedBy(deviceId, userEmail);
        device.setOnline(online);
        device.setLastSeenAt(Instant.now());
        deviceRepository.save(device);
    }

    private Device findDeviceOwnedBy(String deviceId, String userEmail) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", deviceId));
        if (!device.getUser().getEmail().equals(userEmail)) {
            throw new ResourceNotFoundException("Device", deviceId);
        }
        return device;
    }

    public DeviceDto toDto(Device device) {
        return DeviceDto.builder()
                .id(device.getId())
                .name(device.getName())
                .operatingSystem(device.getOperatingSystem())
                .online(device.isOnline())
                .watchPath(device.getWatchPath())
                .autoBackupEnabled(device.isAutoBackupEnabled())
                .userId(device.getUser().getId())
                .createdAt(device.getCreatedAt().toString())
                .lastSeenAt(device.getLastSeenAt() != null ? device.getLastSeenAt().toString() : null)
                .build();
    }
}
