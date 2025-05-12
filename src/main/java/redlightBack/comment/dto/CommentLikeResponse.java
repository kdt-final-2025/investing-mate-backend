package redlightBack.comment.dto;

public record CommentLikeResponse(
        Long commentId,
        int likeCount,
        boolean likedByMe

) {
}
