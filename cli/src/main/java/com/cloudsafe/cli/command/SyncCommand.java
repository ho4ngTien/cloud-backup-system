package com.cloudsafe.cli.command;

import com.cloudsafe.cli.ApiClient;
import com.cloudsafe.cli.engine.BackupEngine;
import com.cloudsafe.cli.engine.RestoreEngine;
import com.cloudsafe.cli.engine.CryptoUtil;
import picocli.CommandLine.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

@Command(
    name = "sync",
    description = "Synchronize local folder 2-way with CloudSafe cloud storage",
    mixinStandardHelpOptions = true
)
public class SyncCommand implements Runnable {

    @Parameters(index = "0", description = "Local directory to synchronize")
    private Path localPath;

    @Option(names = {"-d", "--device"}, description = "Device ID to synchronize", required = true)
    private String deviceId;

    @Override
    public void run() {
        ApiClient client = new ApiClient();
        if (!client.isLoggedIn()) {
            System.err.println("❌ Not logged in. Run: cloudsafe login --email <email>");
            System.exit(1);
        }

        if (!Files.isDirectory(localPath)) {
            System.err.println("❌ Local path is not a directory: " + localPath);
            System.exit(1);
        }

        System.out.println();
        System.out.println("🔄 CloudSafe Synchronization");
        System.out.println("   Local Path: " + localPath.toAbsolutePath());
        System.out.println("   Device ID : " + deviceId);
        System.out.println();

        try {
            // Step 1: Scan local files and compute SHA-256
            System.out.println("📂 Scanning local directory...");
            Map<String, String> localFiles = new HashMap<>();
            scanLocalFiles(localPath, localPath, localFiles);
            System.out.println("   Found " + localFiles.size() + " local file(s).");

            // Step 2: Fetch latest cloud files from server
            System.out.println("☁️  Fetching latest backup state from server...");
            @SuppressWarnings("unchecked")
            Map<String, Object> cloudState = (Map<String, Object>) client.get(
                    "/api/backups/latest-completed-files?deviceId=" + deviceId, client.getToken());

            List<Map<String, Object>> cloudFilesList = new ArrayList<>();
            byte[] aesKey = null;

            if (cloudState != null && cloudState.containsKey("files")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> rawList = (List<Map<String, Object>>) cloudState.get("files");
                if (rawList != null) {
                    cloudFilesList = rawList;
                }
                String keyBase64 = (String) cloudState.get("encryptionKey");
                if (keyBase64 != null) {
                    aesKey = Base64.getDecoder().decode(keyBase64);
                }
            }

            System.out.println("   Found " + cloudFilesList.size() + " file(s) in cloud.");

            // Map: relPath -> cloudFileInfo
            Map<String, Map<String, Object>> cloudFilesMap = new HashMap<>();
            for (Map<String, Object> fileInfo : cloudFilesList) {
                String relPath = (String) fileInfo.get("relativePath");
                cloudFilesMap.put(relPath, fileInfo);
            }

            // Step 3: Identify files to download (Cloud only)
            List<Map<String, Object>> downloadList = new ArrayList<>();
            for (String relPath : cloudFilesMap.keySet()) {
                if (!localFiles.containsKey(relPath)) {
                    Map<String, Object> cloudInfo = cloudFilesMap.get(relPath);
                    String storageKey = (String) cloudInfo.get("storageKey");
                    @SuppressWarnings("unchecked")
                    Map<String, Object> urlResp = client.get(
                            "/api/storage/signed-url?storageKey=" +
                            java.net.URLEncoder.encode(storageKey, "UTF-8"), client.getToken());
                    
                    Map<String, Object> downloadInfo = new HashMap<>(cloudInfo);
                    downloadInfo.put("downloadUrl", urlResp.get("url"));
                    downloadList.add(downloadInfo);
                }
            }

            // Step 4: Identify files to upload (Local only or modified)
            boolean needsUpload = false;
            for (String relPath : localFiles.keySet()) {
                String localHash = localFiles.get(relPath);
                if (!cloudFilesMap.containsKey(relPath)) {
                    System.out.println("   ➕ New local file: " + relPath);
                    needsUpload = true;
                } else {
                    String cloudHash = (String) cloudFilesMap.get(relPath).get("sha256Hash");
                    if (!localHash.equalsIgnoreCase(cloudHash)) {
                        System.out.println("   ✏️  Modified local file: " + relPath);
                        needsUpload = true;
                    }
                }
            }

            // Step 5: Execute Downloads
            if (!downloadList.isEmpty()) {
                if (aesKey == null) {
                    System.err.println("❌ Cannot restore: Encryption key not returned by server.");
                    System.exit(1);
                }
                System.out.println("📥 Downloading " + downloadList.size() + " missing file(s) from cloud...");
                RestoreEngine restoreEngine = new RestoreEngine(aesKey);
                restoreEngine.restore(downloadList, localPath);
            } else {
                System.out.println("✅ All cloud files are present locally.");
            }

            // Step 6: Execute Uploads
            if (needsUpload) {
                System.out.println("🚀 Local files are new or modified. Running sync backup...");
                Map<String, Object> job = client.startBackup(deviceId, "INCREMENTAL",
                        localPath.toAbsolutePath().toString());
                String jobId = (String) job.get("id");
                
                BackupEngine backupEngine = new BackupEngine(
                        client, jobId, "INCREMENTAL", deviceId, (String) job.get("excludedPatterns"));
                backupEngine.run(localPath);
                System.out.println("✅ Sync backup completed. Cloud state updated!");
            } else {
                System.out.println("✅ Local directory is up-to-date with cloud.");
            }

            System.out.println();
            System.out.println("🔄 Synchronization finished successfully!");
            System.out.println();

        } catch (Exception e) {
            System.err.println("❌ Synchronization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void scanLocalFiles(Path baseDir, Path currentDir, Map<String, String> result) throws IOException {
        try (Stream<Path> stream = Files.list(currentDir)) {
            List<Path> paths = stream.toList();
            for (Path path : paths) {
                if (Files.isDirectory(path)) {
                    scanLocalFiles(baseDir, path, result);
                } else if (Files.isRegularFile(path)) {
                    String name = path.getFileName().toString();
                    if (name.startsWith("~$") || name.endsWith(".tmp")
                            || name.endsWith(".log") || name.equals("Thumbs.db")
                            || name.equals(".DS_Store")) {
                        continue;
                    }
                    String relPath = baseDir.relativize(path).toString().replace("\\", "/");
                    try {
                        String hash = CryptoUtil.sha256Hex(path);
                        result.put(relPath, hash);
                    } catch (Exception e) {
                        // ignore unreadable files
                    }
                }
            }
        }
    }
}
