package com.MuscleHead.MuscleHead.Post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostPatchRequest {

    /** If true, increment likeCount by 1 */
    private Boolean like;

    /** If present, add a new comment from the current user */
    private String comment;
}
