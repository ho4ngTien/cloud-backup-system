package com.cloudsafe.storage;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Google Cloud Storage implementation of StorageService.
 * Activated when app.storage.provider=gcp (default).
 */
@Service
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "gcp", matchIfMissing = true)
@Slf4j
public class GcsStorageService implements StorageService {

    private final Storage storage;
    private final String bucketName;

    public GcsStorageService(
            @Value("${app.storage.gcp.bucket-name}") String bucketName,
            @Value("${app.storage.gcp.credentials-path:#{null}}") String credentialsPath)
            throws IOException {

        this.bucketName = bucketName;

        StorageOptions.Builder builder = StorageOptions.newBuilder();
        if (credentialsPath != null && !credentialsPath.isBlank()) {
            try (FileInputStream fis = new FileInputStream(credentialsPath)) {
                builder.setCredentials(GoogleCredentials.fromStream(fis)
                        .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform")));
            }
            log.info("GCS initialised with explicit credentials: {}", credentialsPath);
        } else {
            // Fall back to Application Default Credentials (ADC) – works on GCE, Cloud Run, etc.
            builder.setCredentials(GoogleCredentials.getApplicationDefault());
            log.info("GCS initialised with Application Default Credentials");
        }

        this.storage = builder.build().getService();
    }

    @Override
    public void upload(String storageKey, InputStream data, long contentLength, String contentType) {
        BlobId blobId = BlobId.of(bucketName, storageKey);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(contentType)
                .build();

        try (var writer = storage.writer(blobInfo)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = data.read(buffer)) != -1) {
                writer.write(java.nio.ByteBuffer.wrap(buffer, 0, read));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload to GCS: " + storageKey, e);
        }
        log.debug("Uploaded to GCS: {}/{}", bucketName, storageKey);
    }

    @Override
    public InputStream download(String storageKey) {
        Blob blob = storage.get(BlobId.of(bucketName, storageKey));
        if (blob == null) {
            throw new RuntimeException("File not found in GCS: " + storageKey);
        }
        return Channels.newInputStream(blob.reader());
    }

    @Override
    public void delete(String storageKey) {
        boolean deleted = storage.delete(BlobId.of(bucketName, storageKey));
        if (deleted) {
            log.debug("Deleted from GCS: {}/{}", bucketName, storageKey);
        } else {
            log.warn("Object not found for deletion: {}/{}", bucketName, storageKey);
        }
    }

    @Override
    public List<String> listObjects(String prefix) {
        Page<Blob> blobs = storage.list(bucketName,
                Storage.BlobListOption.prefix(prefix),
                Storage.BlobListOption.pageSize(1000));

        return StreamSupport.stream(blobs.iterateAll().spliterator(), false)
                .map(BlobInfo::getName)
                .collect(Collectors.toList());
    }

    @Override
    public URL generateSignedUrl(String storageKey, Duration expiry) {
        BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName, storageKey)).build();
        return storage.signUrl(blobInfo, expiry.toMinutes(), TimeUnit.MINUTES,
                Storage.SignUrlOption.withV4Signature());
    }

    @Override
    public boolean exists(String storageKey) {
        Blob blob = storage.get(BlobId.of(bucketName, storageKey));
        return blob != null && blob.exists();
    }
}
