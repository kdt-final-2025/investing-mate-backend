package redlightBack.Comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import redlightBack.Comment.Domain.Comment;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment,Long> {

    Page<Comment> findByPostId(Long postId, Pageable pageable);
    Optional<Comment> findByIdAndDeleteIsNull(Long id);

}


