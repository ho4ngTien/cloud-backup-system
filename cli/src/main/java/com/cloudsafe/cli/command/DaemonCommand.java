package com.cloudsafe.cli.command;

import com.cloudsafe.cli.ApiClient;
import com.cloudsafe.cli.engine.BackupEngine;
import picocli.CommandLine.*;

import java.nio.file.Path;
import java.util.*;

@Command(
    name = "daemon",
    description = "Start the CloudSafe Background Agent Daemon",
    mixinStandardHelpOptions = true
)
public class DaemonCommand implements Runnable {

    @Option(names = {"-d", "--device"}, description = "Device ID to monitor", required = true)
    private String deviceId;

    @Option(names = {"-i", "--interval"}, description = "Polling interval in seconds", defaultValue = "30")
    private int interval;

    @Override
    public void run() {
        ApiClient client = new ApiClient();
        if (!client.isLoggedIn()) {
            System.err.println("❌ Not logged in. Run: cloudsafe login --email <email>");
            System.exit(1);
        }

        System.out.println();
        System.out.println("🤖 CloudSafe Background Agent Daemon Started");
        System.out.println("   Device ID : " + deviceId);
        System.out.println("   Interval  : " + interval + "s");
        System.out.println("   Press Ctrl+C to terminate");
        System.out.println();

        while (true) {
            try {
                // 1. Send heartbeat to notify online status
                client.post("/api/devices/" + deviceId + "/heartbeat?online=true", null, client.getToken());

                // 2. Fetch pending jobs from server
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> pendingJobs = (List<Map<String, Object>>) client.get("/api/backups/pending?deviceId=" + deviceId, client.getToken());
                
                if (pendingJobs != null && !pendingJobs.isEmpty()) {
                    System.out.println("📅 Found " + pendingJobs.size() + " pending backup job(s) from server.");
                    for (Map<String, Object> job : pendingJobs) {
                        String jobId = (String) job.get("id");
                        String backupType = (String) job.get("backupType");
                        String sourcePathStr = (String) job.get("sourcePath");

                        if (sourcePathStr == null || sourcePathStr.isBlank()) {
                            System.err.println("   ⚠️  Skip job " + jobId + ": No source path configured on device");
                            continue;
                        }

                        Path sourcePath = Path.of(sourcePathStr);
                        System.out.println("🚀 Executing automatic backup for job: " + jobId + " (" + backupType + ")");
                        System.out.println("   Path: " + sourcePath);

                        try {
                            BackupEngine engine = new BackupEngine(client, jobId, backupType, deviceId, (String) job.get("excludedPatterns"));
                            engine.run(sourcePath);
                            System.out.println("✅ Backup job " + jobId + " completed successfully!");
                        } catch (Exception e) {
                            System.err.println("❌ Backup job " + jobId + " failed: " + e.getMessage());
                            // Notify failure to server
                            try {
                                Map<String, String> failReq = Map.of("errorMessage", e.getMessage());
                                client.post("/api/backups/" + jobId + "/fail", failReq, client.getToken());
                            } catch (Exception ex) {
                                // Ignore
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("⚠️  Daemon cycle warning: " + e.getMessage());
            }

            try {
                Thread.sleep(interval * 1000L);
            } catch (InterruptedException e) {
                System.out.println("🤖 Daemon terminated.");
                break;
            }
        }
    }
}
