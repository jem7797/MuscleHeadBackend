package com.MuscleHead.MuscleHead.s3;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PresignedUrlRequest {

    public static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    public enum Operation {
        UPLOAD,
        DOWNLOAD
    }

    @NotBlank(message = "Object key is required")
    private String objectKey;

    private Operation operation = Operation.UPLOAD;

    /** Content-Type for uploads. Accepts contentType or content_type. Defaults to application/octet-stream. */
    @JsonAlias("content_type")
    private String contentType;

    /** Effective content type for signing: request value if set, else application/octet-stream. */
    public String getEffectiveContentType() {
        return (contentType != null && !contentType.isBlank()) ? contentType : DEFAULT_CONTENT_TYPE;
    }
}
