package redlightBack.comment.domain;

import jakarta.persistence.*;
import lombok.*;
import redlightBack.common.BaseEntity;

@NoArgsConstructor
@Getter
@ToString
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"comment_id", "user_id"})
})
public class CommentLike extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @ManyToOne
    @JoinColumn(nullable = false)
    private  Comment comment;

    public CommentLike(Comment comment, String userId) {
        this.userId = userId;
        this.comment = comment;
    }
}