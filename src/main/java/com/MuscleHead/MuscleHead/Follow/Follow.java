package com.MuscleHead.MuscleHead.Follow;

import com.MuscleHead.MuscleHead.User.User;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "follows")
@Data
@NoArgsConstructor
public class Follow {

    @EmbeddedId
    private FollowId id;

    @ManyToOne
    @MapsId("followerSubId")
    @JoinColumn(name = "follower_sub_id", referencedColumnName = "sub_id", nullable = false)
    private User follower;

    @ManyToOne
    @MapsId("followeeSubId")
    @JoinColumn(name = "followee_sub_id", referencedColumnName = "sub_id", nullable = false)
    private User followee;
}
