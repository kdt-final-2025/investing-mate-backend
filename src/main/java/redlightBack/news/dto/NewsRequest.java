package redlightBack.news.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record NewsRequest(
        @NotNull String title,
        @NotNull String description,
        List<String> imageUrls,
        @NotNull LocalDateTime publishedAt
) {
}
