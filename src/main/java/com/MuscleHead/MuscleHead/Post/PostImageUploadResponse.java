package com.MuscleHead.MuscleHead.Post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostImageUploadResponse {

    /** Presigned URL to PUT the image (client uploads directly to S3) */
    private String uploadUrl;

    /** S3 object key – pass this as imageLink when creating the post */
    private String objectKey;
}
