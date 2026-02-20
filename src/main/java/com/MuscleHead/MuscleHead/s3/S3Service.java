package com.MuscleHead.MuscleHead.s3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

/**
 * Generates presigned URLs so the frontend can upload/download objects
 * to/from S3 directly without proxying through the backend.
 * Credentials are loaded from AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY env vars
 * or ~/.aws/credentials.
 */
@Service
public class S3Service {

    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.region:us-east-1}")
    private String region;

    @Value("${aws.s3.presigned-url-expiry-minutes:15}")
    private int expiryMinutes;

    /**
     * Generates a presigned PUT URL for uploading an object.
     * Frontend uses this URL with a PUT request and the file bytes in the body.
     *
     * @param objectKey S3 object key (e.g., "users/{subId}/profile.jpg")
     * @return Presigned URL
     */
    public String generatePresignedUploadUrl(String objectKey) {
        try (S3Presigner presigner = S3Presigner.builder()
                .region(Region.of(region))
                .build()) {

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(expiryMinutes))
                    .putObjectRequest(putRequest)
                    .build();

            String url = presigner.presignPutObject(presignRequest).url().toExternalForm();
            logger.info("Generated presigned upload URL for key: {}", objectKey);
            return url;
        } catch (Exception e) {
            logger.error("Failed to generate presigned upload URL for key: {}", objectKey, e);
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
        try (S3Presigner presigner = S3Presigner.builder()
                .region(Region.of(region))
                .build()) {

            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(expiryMinutes))
                    .getObjectRequest(getRequest)
                    .build();

            String url = presigner.presignGetObject(presignRequest).url().toExternalForm();
            logger.info("Generated presigned download URL for key: {}", objectKey);
            return url;
        } catch (Exception e) {
            logger.error("Failed to generate presigned download URL for key: {}", objectKey, e);
            throw new RuntimeException("Failed to generate download URL", e);
        }
    }
}
