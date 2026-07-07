package com.cloudsafe.device.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cloudsafe.device.entity.Device;

@Repository
public interface DeviceRepository extends JpaRepository<Device, String> {
    List<Device> findByUserId(String userId);
    List<Device> findByUserIdAndOnlineTrue(String userId);
    List<Device> findByAutoBackupEnabledTrue();
}
