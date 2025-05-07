package redlightBack.Post;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    PostLike findByPostIdAndUserId(Long postId, String userId);
}
