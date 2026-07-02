package com.cloudsafe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CloudSafe – Cloud Backup & Disaster Recovery System
 * Entry point for the Spring Boot server application.
 */
@SpringBootApplication
public class CloudBackupApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudBackupApplication.class, args);
    }
}
