package redlightBack.Comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import redlightBack.Comment.Domain.Comment;

public interface CommentRepository extends JpaRepository<Comment,Long> {

//
//    List<Comment> findAllByPostIdAndUserId(Long postId, String userId);
//
//    List<Comment> findAllByParentId(Long parentId);

    Page<Comment> findByPostId(Long postId, Pageable pageable);
}
