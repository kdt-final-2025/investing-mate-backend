package redlightBack.post.dto;

import java.time.LocalDateTime;

public record DeletePostResponse(LocalDateTime deletedAt) {
}
