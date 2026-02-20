package com.MuscleHead.MuscleHead.s3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.MuscleHead.MuscleHead.config.SecurityUtils;

import jakarta.validation.Valid;

/**
 * Exposes presigned URL generation for S3 uploads/downloads.
 * Requires authentication. The frontend calls this to get a temporary URL,
 * then uses that URL to PUT (upload) or GET (download) directly to/from S3.
 */
@RestController
@RequestMapping("s3/api/")
public class S3Controller {

    private static final Logger logger = LoggerFactory.getLogger(S3Controller.class);

    @Autowired
    private S3Service s3Service;

    @PostMapping("presigned-url")
    public ResponseEntity<PresignedUrlResponse> getPresignedUrl(@Valid @RequestBody PresignedUrlRequest request) {
        String subId = SecurityUtils.getCurrentUserSub();
        if (subId == null) {
            logger.warn("Presigned URL requested without authentication");
            return ResponseEntity.status(401).build();
        }

        String url;
        if (request.getOperation() == PresignedUrlRequest.Operation.DOWNLOAD) {
            url = s3Service.generatePresignedDownloadUrl(request.getObjectKey());
        } else {
            url = s3Service.generatePresignedUploadUrl(request.getObjectKey());
        }

        return ResponseEntity.ok(new PresignedUrlResponse(url, request.getObjectKey()));
    }
}
