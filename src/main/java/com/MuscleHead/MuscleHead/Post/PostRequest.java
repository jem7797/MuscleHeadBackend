package com.MuscleHead.MuscleHead.Post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostRequest {

    /** Optional. S3 object key for image posts; null/blank for text-only posts. */
    private String imageLink;

    private String caption;
}
