package com.cloudsafe.storage.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.List;

@Service
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "s3")
@Slf4j
public class S3StorageService implements StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucketName;

    public S3StorageService(
            @Value("${app.storage.s3.bucket-name:#{null}}") String bucketName,
            @Value("${app.storage.s3.region:#{null}}") String region,
            @Value("${app.storage.s3.access-key-id:#{null}}") String accessKey,
            @Value("${app.storage.s3.secret-access-key:#{null}}") String secretKey) {

        this.bucketName = bucketName != null ? bucketName : "default-bucket";
        String awsRegion = region != null ? region : "us-east-1";

        AwsCredentialsProvider credentialsProvider;
        if (accessKey != null && !accessKey.isBlank() && secretKey != null && !secretKey.isBlank()) {
            credentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
        } else {
            credentialsProvider = DefaultCredentialsProvider.create();
        }

        Region s3Region = Region.of(awsRegion);

        this.s3Client = S3Client.builder()
                .credentialsProvider(credentialsProvider)
                .region(s3Region)
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();

        this.s3Presigner = S3Presigner.builder()
                .credentialsProvider(credentialsProvider)
                .region(s3Region)
                .build();

        log.info("S3 Storage Service initialized in region {} with bucket {}", awsRegion, this.bucketName);
    }

    @Override
    public void upload(String storageKey, InputStream data, long contentLength, String contentType) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storageKey)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(data, contentLength));
            log.debug("Uploaded to S3: {}", storageKey);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload to S3: " + storageKey, e);
        }
    }

    @Override
    public InputStream download(String storageKey) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storageKey)
                    .build();

            return s3Client.getObject(getObjectRequest);
        } catch (Exception e) {
            throw new RuntimeException("Failed to download from S3: " + storageKey, e);
        }
    }

    @Override
    public void delete(String storageKey) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storageKey)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.debug("Deleted from S3: {}", storageKey);
        } catch (Exception e) {
            log.warn("Failed to delete from S3: " + storageKey, e);
        }
    }

    @Override
    public List<String> listObjects(String prefix) {
        try {
            ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
                    .build();

            ListObjectsV2Response response = s3Client.listObjectsV2(request);
            return response.contents().stream()
                    .map(S3Object::key)
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to list objects in S3 with prefix: " + prefix, e);
        }
    }

    @Override
    public URL generateSignedUrl(String storageKey, Duration expiry) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storageKey)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(expiry)
                    .getObjectRequest(getObjectRequest)
                    .build();

            return s3Presigner.presignGetObject(presignRequest).url();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate presigned S3 URL for: " + storageKey, e);
        }
    }

    @Override
    public boolean exists(String storageKey) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storageKey)
                    .build();

            s3Client.headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Failed to check if S3 object exists: " + storageKey, e);
        }
    }
}
