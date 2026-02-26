package com.MuscleHead.MuscleHead.Post;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.MuscleHead.MuscleHead.Post.Comment.Comment;
import com.MuscleHead.MuscleHead.User.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "posts")
@Data
@NoArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_sub_id", referencedColumnName = "sub_id", nullable = false)
    @NotNull(message = "User is required")
    private User user;

    private String imageLink;

    private String caption;

    /** Score for ranking/sorting (e.g. hot/trending algorithm) */
    private double score = 0;

    @Column(nullable = false, updatable = false)
    private String timestamp;

    @Min(value = 0, message = "Like count cannot be negative")
    private int likeCount = 0;

    @Min(value = 0, message = "Comment count cannot be negative")
    private int commentCount = 0;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (timestamp == null || timestamp.isBlank()) {
            timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
    }
}
