package com.cloudsafe.cli.engine;

import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.stream.Stream;

import com.cloudsafe.cli.ApiClient;

/**
 * Core Backup Engine – Giai đoạn 7.
 *
 * Pipeline:
 *   1. Scan folder recursively
 *   2. Compute SHA-256 for each file
 *   3. Filter unchanged files (incremental)
 *   4. Compress each file to ZIP
 *   5. Encrypt with AES-256/GCM
 *   6. Upload to server / GCS via signed URL
 *   7. Commit metadata to server
 */
public class BackupEngine {

    private final ApiClient client;
    private final String jobId;
    private final String backupType;
    private final String deviceId;
    private final String excludedPatterns;

    /** AES-256 key: in production this is fetched from the server per backup job. */
    private byte[] aesKey;

    public BackupEngine(ApiClient client, String jobId, String backupType, String deviceId, String excludedPatterns) {
        this.client = client;
        this.jobId  = jobId;
        this.backupType = backupType;
        this.deviceId = deviceId;
        this.excludedPatterns = excludedPatterns;
        this.aesKey = CryptoUtil.generateAesKey();
    }

    public void run(Path sourceDir) throws Exception {
        System.out.println("📂 Scanning: " + sourceDir);

        // 1. Scan all files
        List<Path> files = scanFiles(sourceDir);
        System.out.println("   Found " + files.size() + " files");

        // 2. Fetch latest version files if INCREMENTAL
        Map<String, Map<String, Object>> previousFilesMap = new HashMap<>();
        if ("INCREMENTAL".equalsIgnoreCase(backupType)) {
            try {
                Map<String, Object> latestResp = client.getLatestCompletedFiles(deviceId);
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> previousFiles = (List<Map<String, Object>>) latestResp.get("files");
                String prevKeyBase64 = (String) latestResp.get("encryptionKey");
                if (prevKeyBase64 != null && !prevKeyBase64.isBlank()) {
                    this.aesKey = Base64.getDecoder().decode(prevKeyBase64);
                }
                if (previousFiles != null) {
                    for (Map<String, Object> f : previousFiles) {
                        String relPath = (String) f.get("relativePath");
                        if (relPath != null) {
                            previousFilesMap.put(relPath, f);
                        }
                    }
                }
                System.out.println("   Fetched " + previousFilesMap.size() + " file(s) from previous backup version.");
            } catch (Exception e) {
                System.out.println("   ⚠️  Could not fetch previous backup files (running full scan): " + e.getMessage());
            }
        }

        List<Map<String, Object>> committedFiles = new ArrayList<>();
        int uploadedCount = 0;
        int reusedCount = 0;

        for (int i = 0; i < files.size(); i++) {
            Path file = files.get(i);
            String relativePath = sourceDir.relativize(file).toString().replace("\\", "/");
            System.out.printf("   [%d/%d] %s%n", i + 1, files.size(), relativePath);

            try {
                // 3. Compute SHA-256
                String sha256 = CryptoUtil.sha256Hex(file);

                // 4. Compare SHA-256 for Incremental backup
                if (previousFilesMap.containsKey(relativePath)) {
                    Map<String, Object> prevFile = previousFilesMap.get(relativePath);
                    String prevSha = (String) prevFile.get("sha256Hash");
                    if (sha256.equalsIgnoreCase(prevSha)) {
                        System.out.println("         ↳ Unchanged (reusing storage key)");
                        Map<String, Object> meta = new LinkedHashMap<>();
                        meta.put("relativePath", relativePath);
                        meta.put("sha256Hash",   sha256);
                        meta.put("storageKey",   prevFile.get("storageKey"));
                        meta.put("sizeBytes",    ((Number) prevFile.get("sizeBytes")).longValue());
                        meta.put("encrypted",    prevFile.get("encrypted"));
                        committedFiles.add(meta);
                        reusedCount++;
                        continue;
                    }
                }

                // 5. Compress to temp ZIP
                Path zipFile = Files.createTempFile("cloudsafe-", ".zip");
                ZipUtil.compressFile(file, zipFile);

                // 6. Encrypt with AES-256/GCM
                Path encFile = Files.createTempFile("cloudsafe-", ".enc");
                CryptoUtil.encryptAes256Gcm(zipFile, encFile, aesKey);

                // 7. Build storage key: jobId/relative/path.zip.enc
                String storageKey = jobId + "/" + relativePath + ".enc";

                // 8. Upload via signed PUT URL from server
                uploadFile(encFile, storageKey);
                uploadedCount++;

                // 9. Collect metadata
                Map<String, Object> meta = new LinkedHashMap<>();
                meta.put("relativePath", relativePath);
                meta.put("sha256Hash",   sha256);
                meta.put("storageKey",   storageKey);
                meta.put("sizeBytes",    Files.size(file));
                meta.put("encrypted",    true);
                committedFiles.add(meta);

                // Cleanup temp files
                Files.deleteIfExists(zipFile);
                Files.deleteIfExists(encFile);

            } catch (Exception e) {
                System.err.println("   ⚠️  Skipped (error): " + e.getMessage());
            }
        }

        System.out.println();
        System.out.printf("✅ Processed: %d file(s) uploaded, %d file(s) reused (unchanged)%n", uploadedCount, reusedCount);
        System.out.println();

        // 10. Commit backup to server
        String encryptionKeyBase64 = Base64.getEncoder().encodeToString(aesKey);
        Map<String, Object> commitRequest = Map.of(
                "jobId", jobId,
                "encryptionKey", encryptionKeyBase64,
                "files", committedFiles
        );
        client.commitBackup(commitRequest);
    }

    // ============================================================
    // Helpers
    // ============================================================

    /** Recursively scan all regular files in a directory. */
    private List<Path> scanFiles(Path dir) throws IOException {
        if (!Files.isDirectory(dir)) {
            return Files.isRegularFile(dir) ? List.of(dir) : Collections.emptyList();
        }
        try (Stream<Path> stream = Files.walk(dir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(p -> !isExcluded(p, dir))
                    .sorted()
                    .toList();
        }
    }

    /** Skip common temp/system files and user-defined patterns. */
    private boolean isExcluded(Path path, Path sourceDir) {
        String name = path.getFileName().toString();
        if (name.startsWith("~$") || name.endsWith(".tmp")
                || name.endsWith(".log") || name.equals("Thumbs.db")
                || name.equals(".DS_Store")) {
            return true;
        }

        if (excludedPatterns != null && !excludedPatterns.isBlank()) {
            String relPath = sourceDir.relativize(path).toString().replace("\\", "/");
            String[] patterns = excludedPatterns.split(",");
            for (String pattern : patterns) {
                pattern = pattern.trim();
                if (pattern.isEmpty()) continue;
                try {
                    java.nio.file.PathMatcher matcher = java.nio.file.FileSystems.getDefault()
                            .getPathMatcher("glob:" + pattern);
                    java.nio.file.Path rel = java.nio.file.Path.of(relPath);
                    if (matcher.matches(path.getFileName()) || matcher.matches(rel) ||
                        java.nio.file.FileSystems.getDefault().getPathMatcher("glob:**/" + pattern).matches(rel)) {
                        return true;
                    }
                } catch (Exception e) {
                    if (relPath.contains(pattern)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** Upload encrypted file to GCS via API server. */
    private void uploadFile(Path encFile, String storageKey) throws Exception {
        // Get a signed upload URL from server
        Map<String, Object> urlResp = client.get(
                "/api/storage/signed-url?storageKey=" +
                java.net.URLEncoder.encode(storageKey, "UTF-8"), client.getToken());

        String signedUrl = (String) urlResp.get("url");

        // PUT the file using Java HttpClient
        java.net.http.HttpClient httpClient = java.net.http.HttpClient.newHttpClient();
        java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(signedUrl))
                .timeout(java.time.Duration.ofMinutes(5))
                .header("Content-Type", "application/octet-stream")
                .PUT(java.net.http.HttpRequest.BodyPublishers.ofFile(encFile))
                .build();

        java.net.http.HttpResponse<String> resp =
                httpClient.send(req, java.net.http.HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() >= 400) {
            throw new IOException("Upload failed (HTTP " + resp.statusCode() + "): " + storageKey);
        }
    }
}
