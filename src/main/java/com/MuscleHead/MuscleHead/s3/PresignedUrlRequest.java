package com.MuscleHead.MuscleHead.s3;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PresignedUrlRequest {

    public enum Operation {
        UPLOAD,
        DOWNLOAD
    }

    @NotBlank(message = "Object key is required")
    private String objectKey;

    private Operation operation = Operation.UPLOAD;
}
