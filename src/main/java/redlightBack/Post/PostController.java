package redlightBack.Post;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import redlightBack.Post.Dto.*;
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
    public PostResponse getPost (@PathVariable Long postId){
        return postService.getDetailPost(postId);
    }

    @PutMapping("posts/{postId}")
    public PostResponse updatePost (@LoginMemberId String userId,
                                    @PathVariable Long postId,
                                    @RequestBody CreatePostRequest request){
        return postService.update(userId, postId, request);
    }

    @DeleteMapping("posts/{postId}")
    public DeletePostResponse deletePost (@LoginMemberId String userId,
                                          @PathVariable Long postId){

        return postService.delete(userId, postId);
    }

    @GetMapping("/posts")
    public PostListAndPagingResponse getPostList (
                                                  @RequestParam Long boardId,
                                                  @RequestParam (required = false) String postTitle,
                                                  @RequestParam (required = false) String userId,
                                                  @RequestParam (required = false) String sortBy,
                                                  @RequestParam (required = false) String direction,
                                                  @RequestParam (defaultValue = "0") int pageNumber,
                                                  @RequestParam (defaultValue = "10") int size){

        Pageable pageable = PageRequest.of(pageNumber, size);

        return postService.getPosts(userId, boardId, postTitle, sortBy, direction, pageable);
    }
}
