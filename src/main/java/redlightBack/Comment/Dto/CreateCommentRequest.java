package redlightBack.Comment.Dto;

public record CreateCommentRequest(
        Long postId,
        Long parentId, // parent 댓글의 ID만 받음
        String content
) {}

