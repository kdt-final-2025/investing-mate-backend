package redlightBack.Post.Dto;

import java.time.LocalDateTime;
import java.util.List;

public record PostResponse(Long boardId,
                           Long id,
                           String userId,
                           String postTitle,
                           String content,
                           List<String> imageUrls,
                           LocalDateTime createdAt,
                           LocalDateTime updatedAt) {
}
