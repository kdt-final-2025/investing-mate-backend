package redlightBack.Post;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import redlightBack.Post.Dto.PostLikeResponse;

import java.util.NoSuchElementException;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;

    @Transactional
    public PostLikeResponse toggleLike (String userId,
                                        Long postId){

        Post post = postRepository.findById(postId).orElseThrow(
                () -> new NoSuchElementException("해당 개시물이 존재하지 않습니다.")
        );

        Optional<PostLike> existingLike = postLikeRepository.findByPostIdAndUserId(postId, userId);

        boolean liked;

        if(existingLike.isPresent()){
            postLikeRepository.delete(existingLike.get());
            post.decreaseLikeCount();
            liked = false;
        }
        else {
            PostLike newPostLike = new PostLike(post, userId);
            post.increaseLikeCount();
            postLikeRepository.save(newPostLike);
            liked = true;
        }
        postRepository.save(post);

        return new PostLikeResponse(post.getId(),
                liked,
                post.getLikeCount());
    }
}
