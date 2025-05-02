package redlightBack.Post.Dto;

import java.util.List;

public record PostsLikedAndPagingResponse(List<PostsLikedResponse> likedPostsResponse,
                                          PageInfo pageInfo) {
}
