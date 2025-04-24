package redlightBack.common.Comment.Dto;

import java.time.LocalDateTime;

public record CommentResponse(
        Long commentId,
        String userId,
        String content,
        int likeCount,
        boolean likedByMe,
        LocalDateTime createdAt
) {
}
