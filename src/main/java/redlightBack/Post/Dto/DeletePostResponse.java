package redlightBack.Post.Dto;

import java.time.LocalDateTime;

public record DeletePostResponse(LocalDateTime deletedAt) {
}
