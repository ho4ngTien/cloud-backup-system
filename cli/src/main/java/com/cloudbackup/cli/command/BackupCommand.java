package com.cloudsafe.cli.command;

import com.cloudsafe.cli.ApiClient;
import com.cloudsafe.cli.engine.BackupEngine;
import picocli.CommandLine.*;

import java.nio.file.Path;
import java.util.*;

@Command(
    name = "backup",
    description = "Backup a local folder to CloudSafe cloud storage",
    mixinStandardHelpOptions = true
)
public class BackupCommand implements Runnable {

    @Parameters(index = "0", description = "Folder path to backup (e.g. C:\\Users\\Data)")
    private Path sourcePath;

    @Option(names = {"-d", "--device"}, description = "Device ID (register device first)",
            required = true)
    private String deviceId;

    @Option(names = {"-t", "--type"}, description = "Backup type: FULL or INCREMENTAL",
            defaultValue = "INCREMENTAL")
    private String backupType;

    @Override
    public void run() {
        ApiClient client = new ApiClient();
        if (!client.isLoggedIn()) {
            System.err.println("❌ Not logged in. Run: cloudsafe login --email <email>");
            System.exit(1);
        }

        System.out.println();
        System.out.println("☁️  CloudSafe Backup");
        System.out.println("   Source : " + sourcePath.toAbsolutePath());
        System.out.println("   Type   : " + backupType);
        System.out.println();

        try {
            // Step 1: Create backup job on server
            Map<String, Object> job = client.startBackup(deviceId, backupType,
                    sourcePath.toAbsolutePath().toString());
            String jobId = (String) job.get("id");
            System.out.println("✅ Job created: " + jobId);

            // Step 2: Run backup engine (scan → hash → zip → encrypt → upload)
            BackupEngine engine = new BackupEngine(client, jobId, backupType, deviceId, (String) job.get("excludedPatterns"));
            engine.run(sourcePath);

            System.out.println();
            System.out.println("✅ Backup completed successfully!");
            System.out.println("   Job ID : " + jobId);
            System.out.println("   Check your email for a confirmation.");
            System.out.println();

        } catch (Exception e) {
            System.err.println("❌ Backup failed: " + e.getMessage());
            System.exit(1);
        }
    }
}
