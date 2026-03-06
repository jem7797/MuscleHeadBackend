package com.MuscleHead.MuscleHead.Post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostRequest {

    /** Cognito sub_id; must match authenticated user. Required for achievement posts. */
    private String userId;

    /** Optional. S3 object key for image posts; null/blank for text-only posts. */
    private String imageLink;

    private String caption;

    /** True for achievement posts; optional, default false. */
    private Boolean isTrophy;

    /** user_medals.id; required when isTrophy is true. */
    private Long achievementId;
}
