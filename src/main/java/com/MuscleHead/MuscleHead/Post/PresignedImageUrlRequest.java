package com.MuscleHead.MuscleHead.Post;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Data;

/**
 * Optional request body for POST /posts/api/presigned-image-url.
 * Accepts contentType or content_type; backend always uses "application/octet-stream" when generating the presigned URL.
 */
@Data
public class PresignedImageUrlRequest {

    @JsonAlias("content_type")
    private String contentType;
}
