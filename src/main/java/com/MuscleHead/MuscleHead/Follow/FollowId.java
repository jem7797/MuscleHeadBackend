package com.MuscleHead.MuscleHead.Follow;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowId implements Serializable {

    private static final long serialVersionUID = 1L;

    private String followerSubId;
    private String followeeSubId;
}
