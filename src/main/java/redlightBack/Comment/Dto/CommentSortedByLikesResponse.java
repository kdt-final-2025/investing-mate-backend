package redlightBack.Comment.Dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import redlightBack.Comment.Domain.Comment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Getter
public class CommentSortedByLikesResponse {
    private final Long commentId;
    private final Long parentId;
    private final String userId;
    private final String content;
    private final int likeCount;
    private final boolean likedByMe;
    private final LocalDateTime createdAt;
    private List<CommentSortedByLikesResponse> children;

    @QueryProjection
    public CommentSortedByLikesResponse(Long commentId, Long parentId, String userId, String content,
                                        int likeCount, boolean likedByMe, LocalDateTime createdAt) {
        this.commentId = commentId;
        this.parentId = parentId;
        this.userId = userId;
        this.content = content;
        this.likeCount = likeCount;
        this.likedByMe = likedByMe;
        this.createdAt = createdAt;
        this.children = new ArrayList<>();
    }

    // getter + setter or Lombok 사용
}
