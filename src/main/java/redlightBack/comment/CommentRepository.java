package redlightBack.comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import redlightBack.comment.domain.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment,Long> {

    Optional<Comment> findByIdAndDeleteIsNull(Long commentId);
    List<Comment> findByPostIdAndDeleteIsNullOrderByCreatedAtDesc(Long postId);
}


