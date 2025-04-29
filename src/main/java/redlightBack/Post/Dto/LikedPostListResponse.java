package redlightBack.Post.Dto;

import java.time.LocalDateTime;

public record LikedPostListResponse(Long boardId,
                                    String boardName,
                                    String postTitle,
                                    String userId,
                                    int viewCount,
                                    int commentCount,
                                    int likeCount,
                                    LocalDateTime createdAt) {
}
