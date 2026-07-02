package com.cloudsafe.cli.engine;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;

/**
 * Core Restore Engine – Giai đoạn 9.
 *
 * Pipeline:
 *   1. Receive signed download URLs from server
 *   2. Download each encrypted file
 *   3. Decrypt with AES-256/GCM
 *   4. Unzip to target directory
 *   5. Verify SHA-256 integrity
 */
public class RestoreEngine {

    private final byte[] aesKey;
    private final HttpClient httpClient;

    public RestoreEngine(byte[] aesKey) {
        this.aesKey = aesKey;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    /**
     * Restores all files described by the server response to the target directory.
     *
     * @param files      list of {relativePath, sha256Hash, downloadUrl, sizeBytes}
     * @param targetDir  local directory to restore into
     */
    public void restore(List<Map<String, Object>> files, Path targetDir) throws Exception {
        Files.createDirectories(targetDir);
        System.out.println("📥 Restoring " + files.size() + " file(s) to: " + targetDir);
        System.out.println();

        int success = 0, failed = 0;

        for (int i = 0; i < files.size(); i++) {
            Map<String, Object> fileInfo = files.get(i);
            String relativePath = (String) fileInfo.get("relativePath");
            String sha256       = (String) fileInfo.get("sha256Hash");
            String downloadUrl  = (String) fileInfo.get("downloadUrl");

            System.out.printf("   [%d/%d] %s%n", i + 1, files.size(), relativePath);

            try {
                // Step 1: Download encrypted file
                Path encFile = Files.createTempFile("cloudsafe-restore-", ".enc");
                downloadFile(downloadUrl, encFile);

                // Step 2: Decrypt AES-256/GCM
                Path zipFile = Files.createTempFile("cloudsafe-restore-", ".zip");
                CryptoUtil.decryptAes256Gcm(encFile, zipFile, aesKey);
                Files.deleteIfExists(encFile);

                // Step 3: Determine output path
                Path outDir = targetDir.resolve(
                        Path.of(relativePath).getParent() != null
                                ? Path.of(relativePath).getParent().toString()
                                : "");
                Files.createDirectories(outDir);

                // Step 4: Unzip to target
                ZipUtil.decompress(zipFile, outDir);
                Files.deleteIfExists(zipFile);

                // Step 5: Verify SHA-256 of restored file
                Path restoredFile = targetDir.resolve(relativePath);
                if (Files.exists(restoredFile)) {
                    boolean valid = CryptoUtil.verifySha256(restoredFile, sha256);
                    if (valid) {
                        System.out.println("         ✅ Verified");
                        success++;
                    } else {
                        System.out.println("         ⚠️  SHA-256 mismatch – file may be corrupt!");
                        failed++;
                    }
                } else {
                    System.out.println("         ✅ Extracted");
                    success++;
                }

            } catch (Exception e) {
                System.err.println("         ❌ Failed: " + e.getMessage());
                failed++;
            }
        }

        System.out.println();
        System.out.printf("✅ Restore complete: %d succeeded, %d failed%n", success, failed);
    }

    private void downloadFile(String url, Path destination) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(10))
                .GET()
                .build();

        HttpResponse<Path> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofFile(destination));

        if (response.statusCode() >= 400) {
            throw new IOException("Download failed: HTTP " + response.statusCode());
        }
    }
}
