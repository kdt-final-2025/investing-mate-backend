package redlightBack.Comment.Domain;

import jakarta.persistence.*;
import lombok.*;
import redlightBack.common.BaseEntity;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@ToString
@Entity
public class Comment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //** post.id 간접 참조 **/
    @Column(nullable = false)
    private Long postId;

    //** user.id 간접 참조 **/
    @Column(nullable = false)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    private Comment parent;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private int likeCount = 0;//좋아요

    @Column(nullable = false)
    private boolean likedByMe = false;

    @Column(nullable = true)
    private LocalDateTime deletedAt;


    public Comment(Long id, String userId, String content, int likeCount, boolean likedByMe) {
        this.id = id;
        this.userId = userId;
        this.content = content;
        this.likeCount = likeCount;
        this.likedByMe = likedByMe;
    }

    public Comment(String userId, String content, Long postId, Comment parent) {
        this.userId = userId;
        this.content = content;
        this.postId = postId;
        this.parent = parent;
    }

    public void deletedAt() {
        this.deletedAt = LocalDateTime.now();
    }

    // 좋아요 증가
    public void incrementLikeCount() {
        this.likeCount++;
    }

    // 좋아요 감소
    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public void setContent(String content) {
        this.content = content;
    }
}

