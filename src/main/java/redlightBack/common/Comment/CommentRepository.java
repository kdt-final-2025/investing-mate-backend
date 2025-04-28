package redlightBack.common.Comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import redlightBack.common.Comment.Domain.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment,Long> {


    List<Comment> findAllByPostIdAndUserId(Long postId, String userId);

    List<Comment> findAllByParentId(Long parentId);

    Page<Comment> findByAllPostId(Long postId, Pageable pageable);
}
