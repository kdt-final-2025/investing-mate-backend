package redlightBack.common.Comment.Dto;

public record CommentLikeResponse(
        Long commentId,
        int likeCount,
        boolean likedByMe

) {
}
