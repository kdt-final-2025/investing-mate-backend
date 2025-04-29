package redlightBack.Post.Dto;

public record PostLikeResponse(Long postId,
                               boolean liked,
                               int likeCount) {
}
