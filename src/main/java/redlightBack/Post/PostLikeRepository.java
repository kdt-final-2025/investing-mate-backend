package redlightBack.Post;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    PostLike findByPostIdAndUserId(Long postId, String userId);
}
