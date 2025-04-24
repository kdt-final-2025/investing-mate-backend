package redlightBack.news.dto;

import java.time.LocalDateTime;
import java.util.List;

public record NewsRequest(
        String title,
        String description,
        List<String> imageUrls,
        LocalDateTime publishedAt
) {
}
