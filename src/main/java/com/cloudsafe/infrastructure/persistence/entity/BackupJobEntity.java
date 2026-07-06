package com.cloudsafe.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "backup_jobs")
public class BackupJobEntity {
    @Id
    private String id;
    private String userId;
    private String deviceId;
    private String status;
    private String storagePath;

    public BackupJobEntity() {}

    public BackupJobEntity(String id, String userId, String deviceId, String status, String storagePath) {
        this.id = id;
        this.userId = userId;
        this.deviceId = deviceId;
        this.status = status;
        this.storagePath = storagePath;
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getDeviceId() { return deviceId; }
    public String getStatus() { return status; }
    public String getStoragePath() { return storagePath; }
}
