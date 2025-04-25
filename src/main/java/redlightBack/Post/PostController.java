package redlightBack.Post;

import org.springframework.web.bind.annotation.*;
import redlightBack.Post.Dto.CreatePostRequest;
import redlightBack.Post.Dto.DetailPostResponse;
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

    @GetMapping("/posts/{postId}")
    public DetailPostResponse getPost (@PathVariable Long postId){
        return postService.getDetailPost(postId);
    }

    @PutMapping("posts/{postId}")
    public PostResponse updatePost (@LoginMemberId String userId,
                                    @PathVariable Long postId,
                                    @RequestBody CreatePostRequest request){
        return postService.update(userId, postId, request);
    }

}
