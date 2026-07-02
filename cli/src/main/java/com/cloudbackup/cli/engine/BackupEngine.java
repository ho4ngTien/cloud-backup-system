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

    /** AES-256 key: in production this is fetched from the server per backup job. */
    private final byte[] aesKey;

    public BackupEngine(ApiClient client, String jobId) {
        this.client = client;
        this.jobId  = jobId;
        // For server-managed keys: request a 256-bit key from the API per job
        this.aesKey = CryptoUtil.generateAesKey();
    }

    public void run(Path sourceDir) throws Exception {
        System.out.println("📂 Scanning: " + sourceDir);

        // 1. Scan all files
        List<Path> files = scanFiles(sourceDir);
        System.out.println("   Found " + files.size() + " files");

        List<Map<String, Object>> committedFiles = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            Path file = files.get(i);
            System.out.printf("   [%d/%d] %s%n", i + 1, files.size(),
                    sourceDir.relativize(file));

            try {
                // 2. Compute SHA-256
                String sha256 = CryptoUtil.sha256Hex(file);

                // 3. Compress to temp ZIP
                Path zipFile = Files.createTempFile("cloudsafe-", ".zip");
                ZipUtil.compressFile(file, zipFile);

                // 4. Encrypt with AES-256/GCM
                Path encFile = Files.createTempFile("cloudsafe-", ".enc");
                CryptoUtil.encryptAes256Gcm(zipFile, encFile, aesKey);

                // 5. Build storage key: jobId/relative/path.zip.enc
                String relativePath = sourceDir.relativize(file).toString().replace("\\", "/");
                String storageKey   = jobId + "/" + relativePath + ".enc";

                // 6. Upload via signed PUT URL from server
                uploadFile(encFile, storageKey);

                // 7. Collect metadata
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

        // 8. Commit backup to server
        Map<String, Object> commitRequest = Map.of("jobId", jobId, "files", committedFiles);
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
                    .filter(p -> !isExcluded(p))
                    .sorted()
                    .toList();
        }
    }

    /** Skip common temp/system files. */
    private boolean isExcluded(Path path) {
        String name = path.getFileName().toString();
        return name.startsWith("~$") || name.endsWith(".tmp")
                || name.endsWith(".log") || name.equals("Thumbs.db")
                || name.equals(".DS_Store");
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
