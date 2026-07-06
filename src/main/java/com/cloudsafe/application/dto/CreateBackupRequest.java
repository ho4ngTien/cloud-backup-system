package com.cloudsafe.application.dto;

public record CreateBackupRequest(String userId, String deviceId, String storagePath) {
}
