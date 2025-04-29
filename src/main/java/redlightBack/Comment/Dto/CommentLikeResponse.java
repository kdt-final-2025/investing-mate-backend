package redlightBack.Comment.Dto;

public record CommentLikeResponse(
        Long commentId,
        int likeCount,
        boolean likedByMe

) {
}
