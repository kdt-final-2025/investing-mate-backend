package redlightBack.Post.Dto;

import java.util.List;

public record PostListAndPagingResponse(String boardName,
                                        List<PostListResponse> postListResponse,
                                        PageInfo pageInfo) {
}
