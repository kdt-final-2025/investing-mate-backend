package redlightBack.Post.Dto;

import java.time.LocalDateTime;

public record PostsLikedResponse(
        Long postId,
        Long boardId,
        String boardName,
        String postTitle,
        String userId,
        int viewCount,
        int commentCount,
        long likeCount,
        LocalDateTime createdAt
) {
}
