package com.cloudsafe.domain.model;

public class BackupJob {
    private final String id;
    private final String userId;
    private final String deviceId;
    private final String status;
    private final String storagePath;

    public BackupJob(String id, String userId, String deviceId, String status, String storagePath) {
        this.id = id;
        this.userId = userId;
        this.deviceId = deviceId;
        this.status = status;
        this.storagePath = storagePath;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getStatus() {
        return status;
    }

    public String getStoragePath() {
        return storagePath;
    }
}
