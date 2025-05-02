package redlightBack.Comment.Dto;

import redlightBack.Comment.Domain.Comment;

import java.time.LocalDateTime;
import java.util.List;

public record CommentSortedByLikesResponse(
        Long commentId,
        Comment parentId,
        String userId,
        String content,
        int likeCount,
        boolean likedByMe,
        LocalDateTime createdAt,
        List<CommentSortedByLikesResponse> children
) {
}
