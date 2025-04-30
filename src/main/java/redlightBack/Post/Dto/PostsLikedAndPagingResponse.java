package redlightBack.Post.Dto;

import java.util.List;

public record PostsLikedAndPagingResponse(List<PostsLikedResponse> likedPosts,
                                          PageInfo pageInfo) {
}
