package redlightBack.post.dto;

public record PostLikeResponse(Long postId,
                               boolean liked,
                               long likeCount) {
}
