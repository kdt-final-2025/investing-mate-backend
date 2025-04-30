package redlightBack.Post.Dto;

import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDateTime;
public record PostDto(Long id,
                      String postTitle,
                      String userId,
                      int viewCount,
                      int commentCount,
                      long likeCount,
                      LocalDateTime createdAt) {
}
