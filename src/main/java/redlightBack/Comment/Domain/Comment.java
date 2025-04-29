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
    @JoinColumn(name = "parent_id")
    private Comment parent;

//    private Long parentId; // null이면 일반 댓글, 있으면 대댓글

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private int likeCount = 0;//좋아요

    @Column(nullable = false)
    private boolean likedByMe = false;

    @Column(nullable = false)
    private boolean deleted = false;//소프트 삭제

    public Comment(String userId, String content, Long post, Long aLong) {

    }

    public Comment(Long id) {
        this.id = id;
    }

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

    public void deletedAt(){
        this.content = "삭제된 댓글입니다.";  // 삭제된 댓글 내용으로 수정
        this.deleted = true;
        LocalDateTime deletedAt = LocalDateTime.now();
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

