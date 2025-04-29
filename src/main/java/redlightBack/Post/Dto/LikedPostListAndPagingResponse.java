package redlightBack.Post.Dto;

import java.util.List;

public record LikedPostListAndPagingResponse(List<LikedPostListResponse> likedPosts,
                                             PageInfo pageInfo) {
}
