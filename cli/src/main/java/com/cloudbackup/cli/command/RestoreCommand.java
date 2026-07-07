package com.cloudsafe.cli.command;

import com.cloudsafe.cli.ApiClient;
import com.cloudsafe.cli.engine.CryptoUtil;
import com.cloudsafe.cli.engine.RestoreEngine;
import picocli.CommandLine.*;

import java.nio.file.Path;
import java.util.*;

@Command(
    name = "restore",
    description = "Restore files from a backup version to a local folder",
    mixinStandardHelpOptions = true
)
public class RestoreCommand implements Runnable {

    @Option(names = {"-v", "--version"}, description = "Backup version ID to restore from",
            required = true)
    private String versionId;

    @Option(names = {"-d", "--device"}, description = "Device ID to restore to",
            required = true)
    private String deviceId;

    @Option(names = {"-o", "--output"}, description = "Local target directory",
            required = true)
    private Path targetPath;

    @Option(names = {"-f", "--files"}, description = "Specific files to restore (comma-separated relative paths). Leave blank for full restore.")
    private String selectedFiles;

    @Option(names = {"--type"}, description = "Restore type: FULL or SELECTIVE",
            defaultValue = "FULL")
    private String restoreType;

    @Override
    public void run() {
        ApiClient client = new ApiClient();
        if (!client.isLoggedIn()) {
            System.err.println("❌ Not logged in. Run: cloudsafe login");
            System.exit(1);
        }

        System.out.println();
        System.out.println("☁️  CloudSafe Restore");
        System.out.println("   Version ID : " + versionId);
        System.out.println("   Target     : " + targetPath.toAbsolutePath());
        System.out.println("   Type       : " + restoreType);
        System.out.println();

        String restoreId = null;
        try {
            // Build request
            Map<String, Object> request = new LinkedHashMap<>();
            request.put("backupVersionId", versionId);
            request.put("deviceId", deviceId);
            request.put("restoreType", restoreType);
            request.put("targetPath", targetPath.toAbsolutePath().toString());
            if (selectedFiles != null && !selectedFiles.isBlank()) {
                request.put("selectedFiles", List.of(selectedFiles.split(",")));
            }

            // Start restore job on server
            Map<String, Object> resp = client.startRestore(request);
            restoreId = (String) resp.get("id");

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> files = (List<Map<String, Object>>) resp.get("files");

            if (files == null || files.isEmpty()) {
                System.out.println("⚠️  No files found in this backup version.");
                return;
            }

            // Run restore engine: download → decrypt → unzip → verify
            String encryptionKeyBase64 = (String) resp.get("encryptionKey");
            byte[] aesKey;
            if (encryptionKeyBase64 != null && !encryptionKeyBase64.isBlank()) {
                aesKey = Base64.getDecoder().decode(encryptionKeyBase64);
            } else {
                throw new IllegalStateException("No encryption key found for this backup version. Cannot decrypt files.");
            }
            RestoreEngine engine = new RestoreEngine(aesKey);
            engine.restore(files, targetPath);

            // Notify server restore completed
            client.completeRestore(restoreId, true, null);
            System.out.println("✅ Restore completed! Files are in: " + targetPath.toAbsolutePath());

        } catch (Exception e) {
            System.err.println("❌ Restore failed: " + e.getMessage());
            if (restoreId != null) {
                try { client.completeRestore(restoreId, false, e.getMessage()); }
                catch (Exception ignored) {}
            }
            System.exit(1);
        }
    }
}
