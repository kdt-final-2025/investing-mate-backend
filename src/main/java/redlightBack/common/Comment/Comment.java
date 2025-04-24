package redlightBack.common.Comment;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
@Entity
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //** post.id 간접 참조 **/
    @Column(nullable = false)
    private Long postId;

    //** user.id 간접 참조 **/
    @Column(nullable = false)
    private String userId;

    private Long parentId; // null이면 일반 댓글, 있으면 대댓글

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private int likeCount = 0;//좋아요

    @Column(nullable = false)
    private boolean deleted = false;//소프트 삭제

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;//수정시간


}

