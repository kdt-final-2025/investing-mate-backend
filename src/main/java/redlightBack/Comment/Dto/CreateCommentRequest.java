package redlightBack.Comment.Dto;

public record CreateCommentRequest(
        Long postId,
        CreateReplyRequest parent,
        String content
) {
}
