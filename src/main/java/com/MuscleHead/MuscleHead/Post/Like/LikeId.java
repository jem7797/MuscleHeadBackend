package com.MuscleHead.MuscleHead.Post.Like;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeId implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long postId;
    private String userSubId;
}
