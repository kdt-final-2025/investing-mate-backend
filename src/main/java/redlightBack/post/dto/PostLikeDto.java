package redlightBack.post.dto;

import java.time.LocalDateTime;

public record PostLikeDto(Long boardId,
                          String boardName,
                          String postTitle,
                          String userId,
                          int viewCount,
                          int commentCount,
                          long likeCount,
                          LocalDateTime createdAt) {
}
