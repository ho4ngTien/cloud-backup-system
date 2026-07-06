package com.cloudsafe.common.exception;

/** Thrown when user exceeds their cloud storage quota (HTTP 429). */
public class StorageLimitExceededException extends RuntimeException {
    public StorageLimitExceededException(long usedBytes, long quotaBytes) {
        super(String.format("Storage quota exceeded: used %d bytes of %d bytes limit",
                usedBytes, quotaBytes));
    }
}
