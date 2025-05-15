package redlightBack.post.dto;

import java.util.List;

public record PostsLikedAndPagingResponse(List<PostsLikedResponse> likedPostsResponse,
                                          PageInfo pageInfo) {
}
