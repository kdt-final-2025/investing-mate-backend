package redlightBack.Comment.Dto;

import java.time.LocalDateTime;
import java.util.List;

public record CommentCreateResponse(
        Long commentId,
        String userId,
        String content,
        int likeCount,
        LocalDateTime createdAt,
        List<CommentCreateResponse> children
) {
}
