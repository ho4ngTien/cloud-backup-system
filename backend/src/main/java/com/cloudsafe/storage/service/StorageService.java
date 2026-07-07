package com.cloudsafe.storage.service;

import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.List;

/**
 * Abstraction over cloud object storage.
 * Implementations: GcsStorageService, S3StorageService.
 */
public interface StorageService {

    /** Upload an input stream to the given storage key. */
    void upload(String storageKey, InputStream data, long contentLength, String contentType);

    /** Download a file from the given storage key. Returns an InputStream. */
    InputStream download(String storageKey);

    /** Delete a file from storage. */
    void delete(String storageKey);

    /** List all objects under a given prefix (folder). */
    List<String> listObjects(String prefix);

    /** Generate a signed (pre-signed) URL valid for the given duration. */
    URL generateSignedUrl(String storageKey, Duration expiry);

    /** Check whether an object exists. */
    boolean exists(String storageKey);
}
