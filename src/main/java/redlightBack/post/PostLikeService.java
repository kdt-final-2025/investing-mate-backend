package redlightBack.post;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import redlightBack.post.dto.PostLikeResponse;

import java.util.NoSuchElementException;

@RequiredArgsConstructor
@Service
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;

    @Transactional
    public PostLikeResponse toggleLike (String userId,
                                        Long postId){

        Post post = postRepository.findById(postId).orElseThrow(
                () -> new NoSuchElementException("해당 게시물이 존재하지 않습니다.")
        );

        PostLike postLike = postLikeRepository.findByPostIdAndUserId(postId, userId);

        boolean isLiked = switchIsLikeState(post, postLike, userId);

        return new PostLikeResponse(post.getId(),
                isLiked,
                post.getLikeCount());
    }

    private boolean switchIsLikeState(Post post, PostLike postLike, String userId){
        if(postLike != null){
            post.decreaseLikeCount();
            postLikeRepository.delete(postLike);
            return false;
        }else {
            post.increaseLikeCount();
            postLikeRepository.save(new PostLike(post, userId));
            return true;
        }
    }
}
