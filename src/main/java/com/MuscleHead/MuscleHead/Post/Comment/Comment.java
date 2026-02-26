package com.MuscleHead.MuscleHead.Post.Comment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.MuscleHead.MuscleHead.Post.Post;
import com.MuscleHead.MuscleHead.User.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comments")
@Data
@NoArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    @NotNull(message = "Post is required")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_sub_id", referencedColumnName = "sub_id", nullable = false)
    @NotNull(message = "User is required")
    private User user;

    @NotBlank(message = "Comment text cannot be blank")
    private String text;

    @Column(nullable = false, updatable = false)
    private String timestamp;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null || timestamp.isBlank()) {
            timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
    }
}
