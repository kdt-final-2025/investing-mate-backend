package redlightBack.Comment.Dto;

import java.time.LocalDateTime;

public record CommentDTO(
        Long commentId,
        String userId,
        String content,
        int likeCount,
        boolean likedByMe,
        LocalDateTime createdAt
) {}