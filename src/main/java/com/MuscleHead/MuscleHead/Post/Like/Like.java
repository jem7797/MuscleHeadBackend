package com.MuscleHead.MuscleHead.Post.Like;

import com.MuscleHead.MuscleHead.Post.Post;
import com.MuscleHead.MuscleHead.User.User;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post_likes", uniqueConstraints = @jakarta.persistence.UniqueConstraint(columnNames = { "post_id", "user_sub_id" }))
@Data
@NoArgsConstructor
public class Like {

    @EmbeddedId
    private LikeId id;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @MapsId("postId")
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne
    @MapsId("userSubId")
    @JoinColumn(name = "user_sub_id", referencedColumnName = "sub_id", nullable = false)
    private User user;
}
