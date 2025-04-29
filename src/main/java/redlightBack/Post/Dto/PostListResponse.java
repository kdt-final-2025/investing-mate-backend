package redlightBack.Post.Dto;

import java.time.LocalDateTime;

public record PostListResponse (Long id,
                                String postTitle,
                                String userId,
                                int viewCount,
                                int commentCount,
                                int likeCount,
                                LocalDateTime createdAt){
}
