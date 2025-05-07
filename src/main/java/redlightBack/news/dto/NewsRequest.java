package redlightBack.news.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record NewsRequest(
        @NotBlank String title,
        @NotBlank String description,
        @NotEmpty List<String> imageUrls,
        @NotNull LocalDateTime publishedAt
) {
}
