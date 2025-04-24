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
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"comment_id", "user_id"})
})
public class CommentLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long commentId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}