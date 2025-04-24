package redlightBack.common.Comment.Dto;

public record CreateCommentRequest(
        Long postId,
        Long parentId,
        String content
) {
}
