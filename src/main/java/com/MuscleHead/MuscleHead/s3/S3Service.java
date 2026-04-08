package com.MuscleHead.MuscleHead.s3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import jakarta.annotation.PostConstruct;

import java.time.Duration;

/**
 * Generates presigned URLs so the frontend can upload/download objects
 * to/from S3 directly without proxying through the backend.
 * Credentials: from .env (AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY) or system env vars.
 *
 * IMPORTANT: Upload and download use the same bucket. Frontend MUST send the exact
 * Content-Type header (from PresignedUrlResponse.contentType) when PUTting to the upload URL.
 */
@Service
public class S3Service {

    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

    @Value("${aws.s3.bucket}")
    private String bucketName;

    /**
     * Bucket for uploads. Defaults to main bucket so upload and download use the same bucket.
     * Set aws.s3.upload-bucket to override (e.g. for a pending/moderation workflow).
     */
    @Value("${aws.s3.upload-bucket:${aws.s3.bucket}}")
    private String uploadBucketName;

    @Value("${aws.s3.region:us-east-1}")
    private String region;

    @Value("${aws.s3.presigned-url-expiry-minutes:15}")
    private int expiryMinutes;

    @Value("${AWS_ACCESS_KEY_ID:}")
    private String accessKeyId;

    @Value("${AWS_SECRET_ACCESS_KEY:}")
    private String secretAccessKey;

    /**
     * Generates a presigned PUT URL for uploading an object.
     * Frontend must use the same Content-Type header when uploading to avoid 403 signature mismatch.
     *
     * @param objectKey   S3 object key (e.g., "users/{subId}/profile.jpg")
     * @param contentType Content-Type for the upload (use "application/octet-stream" if unsure)
     * @return Presigned URL
     */
    public String generatePresignedUploadUrl(String objectKey, String contentType) {
        String effectiveContentType = (contentType != null && !contentType.isBlank())
                ? contentType
                : "application/octet-stream";
        try (S3Presigner presigner = createPresigner()) {

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(uploadBucketName)
                    .key(objectKey)
                    .contentType(effectiveContentType)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(expiryMinutes))
                    .putObjectRequest(putRequest)
                    .build();

            return presigner.presignPutObject(presignRequest).url().toExternalForm();
        } catch (Exception e) {
            logger.warn("Failed to generate presigned upload URL for key {}: {}", objectKey, e.getMessage());
            throw new RuntimeException("Failed to generate upload URL", e);
        }
    }

    /**
     * Generates a presigned GET URL for downloading an object.
     * Frontend uses this URL with a GET request to fetch the file.
     *
     * @param objectKey S3 object key (e.g., "users/{subId}/profile.jpg")
     * @return Presigned URL
     */
    public String generatePresignedDownloadUrl(String objectKey) {
        try (S3Presigner presigner = createPresigner()) {

            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(expiryMinutes))
                    .getObjectRequest(getRequest)
                    .build();

            return presigner.presignGetObject(presignRequest).url().toExternalForm();
        } catch (Exception e) {
            logger.warn("Failed to generate presigned download URL for key {}: {}", objectKey, e.getMessage());
            throw new RuntimeException("Failed to generate download URL", e);
        }
    }

    @PostConstruct
    void logBucketConfig() {
        logger.info("[S3] Bucket config: upload={} | download={} | region={} | expiry={}min",
                uploadBucketName, bucketName, region, expiryMinutes);
    }

    private S3Presigner createPresigner() {
        var builder = S3Presigner.builder().region(Region.of(region));
        if (accessKeyId != null && !accessKeyId.isBlank() && secretAccessKey != null && !secretAccessKey.isBlank()) {
            builder.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey)));
        }
        return builder.build();
    }
}
