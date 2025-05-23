package redlightBack.post.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PostResponse(Long boardId,
                           Long id,
                           String postTitle,
                           String userId,
                           int viewCount,
                           String content,
                           List<String> imageUrls,
                           LocalDateTime createdAt,
                           LocalDateTime updatedAt,
                           long likeCount,
                           int commentCount) {
}
