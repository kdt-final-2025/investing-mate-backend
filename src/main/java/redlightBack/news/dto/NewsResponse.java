package redlightBack.news.dto;

import java.time.LocalDateTime;
import java.util.List;

public record NewsResponse(
        Long id,
        String title,
        String description,
        List<String> imageUrls,
        LocalDateTime publishedAt,
        String userId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
