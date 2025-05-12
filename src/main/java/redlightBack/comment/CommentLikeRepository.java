package redlightBack.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import redlightBack.comment.domain.CommentLike;

import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike,Long> {
    Optional<CommentLike> findByCommentIdAndUserId(Long commentId, String userId);

}
