package redlightBack.post.dto;

import java.time.LocalDateTime;

public record PostListResponse (Long id,
                                String postTitle,
                                String userId,
                                int viewCount,
                                int commentCount,
                                long likeCount,
                                LocalDateTime createdAt){
}
