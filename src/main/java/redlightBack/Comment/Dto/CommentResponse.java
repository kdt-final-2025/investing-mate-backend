package redlightBack.Comment.Dto;

import java.time.LocalDateTime;
import java.util.List;

public record CommentResponse(
        Long commentId,
        String userId,
        String content,
        int likeCount,
        boolean likeByMe,
        LocalDateTime createdAt,
        List<CommentResponse> children

) {
}
