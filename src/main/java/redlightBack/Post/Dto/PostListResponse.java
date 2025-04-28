package redlightBack.Post.Dto;

public record PostListResponse (Long id,
                                String postTitle,
                                String userId,
                                int viewCount,
                                int commentCount,
                                int likeCount){
}
