package redlightBack.Post.Dto;

import java.time.LocalDateTime;
import java.util.List;

public record DetailPostResponse(Long id,
                                 String postTitle,
                                 String userId,
                                 int viewCount,
                                 String content,
                                 List<String> imageUrls,
                                 LocalDateTime createdAt,
                                 LocalDateTime updatedAt,
                                 LocalDateTime deletedAt,
                                 int likeCount,
                                 boolean likedByMe,
                                 int commentCount) {
}
