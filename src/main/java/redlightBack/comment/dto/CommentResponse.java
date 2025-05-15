package redlightBack.comment.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CommentResponse(
        Long commentId,
        String userId,
        String content,
        int likeCount,
        boolean likeByMe,
        Long parentId,
        LocalDateTime createdAt,
        List<CommentResponse> children

) {
}
