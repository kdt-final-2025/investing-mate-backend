package redlightBack.Post;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import redlightBack.Post.Dto.CreatePostRequest;
import redlightBack.Post.Dto.PostResponse;
import redlightBack.loginUtils.LoginMemberId;

@RestController
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping("/posts")
    public PostResponse createPost (@LoginMemberId String userId,
                                    @RequestBody CreatePostRequest request){

        return postService.create(userId, request);

    }
}
