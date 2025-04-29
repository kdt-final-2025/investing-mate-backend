package redlightBack.Post;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import redlightBack.Post.Dto.PostLikeResponse;
import redlightBack.loginUtils.LoginMemberId;

@RestController
public class PostLikeController {

    private final PostLikeService postLikeService;

    public PostLikeController(PostLikeService postLikeService) {
        this.postLikeService = postLikeService;
    }

    @PostMapping("/posts/{postId}/like")
    public PostLikeResponse like (@LoginMemberId String userId,
                                  @PathVariable Long postId){

        return postLikeService.toggleLike(userId, postId);
    }
}
