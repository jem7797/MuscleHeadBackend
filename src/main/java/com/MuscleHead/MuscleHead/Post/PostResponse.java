package com.MuscleHead.MuscleHead.Post;

import com.MuscleHead.MuscleHead.Follow.UserSummary;
import com.MuscleHead.MuscleHead.Post.Comment.Comment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {

    private Long postId;
    private UserSummary user;
    private String imageLink;
    private String caption;
    private double score;
    private String timestamp;
    private int likeCount;
    private int commentCount;
    private List<CommentSummary> comments;

    public static PostResponse from(Post post) {
        if (post == null) return null;
        List<CommentSummary> commentSummaries = post.getComments() != null
                ? post.getComments().stream()
                        .map(CommentSummary::from)
                        .collect(Collectors.toList())
                : List.of();
        return new PostResponse(
                post.getPostId(),
                UserSummary.from(post.getUser()),
                post.getImageLink(),
                post.getCaption(),
                post.getScore(),
                post.getTimestamp(),
                post.getLikeCount(),
                post.getCommentCount(),
                commentSummaries);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentSummary {
        private Long commentId;
        private UserSummary user;
        private String text;
        private String timestamp;

        public static CommentSummary from(Comment c) {
            if (c == null) return null;
            return new CommentSummary(
                    c.getCommentId(),
                    UserSummary.from(c.getUser()),
                    c.getText(),
                    c.getTimestamp());
        }
    }
}
