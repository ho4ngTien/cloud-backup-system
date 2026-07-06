package com.cloudsafe.domain.repository.repository;

import com.cloudsafe.domain.entity.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<Device, String> {
    List<Device> findByUserId(String userId);
    List<Device> findByUserIdAndOnlineTrue(String userId);
    List<Device> findByAutoBackupEnabledTrue();
}
