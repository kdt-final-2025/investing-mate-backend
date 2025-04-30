package redlightBack.Post;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import redlightBack.Post.Dto.*;
import redlightBack.Post.Enum.Direction;
import redlightBack.Post.Enum.SortBy;
import redlightBack.loginUtils.LoginMemberId;

@RequiredArgsConstructor
@RestController
public class PostController {

    private final PostService postService;
    private final PostLikeService postLikeService;

    //게시물 생성
    @PostMapping("/posts")
    public PostResponse createPost (@LoginMemberId String userId,
                                    @Valid @RequestBody CreatePostRequest request){

        return postService.create(userId, request);
    }

    //게시물 상세보기
    @GetMapping("/posts/{postId}")
    public PostResponse getPost (@PathVariable Long postId){
        return postService.getDetailPost(postId);
    }

    //게시물 수정
    @PutMapping("posts/{postId}")
    public PostResponse updatePost (@LoginMemberId String userId,
                                    @PathVariable Long postId,
                                    @RequestBody CreatePostRequest request){
        return postService.update(userId, postId, request);
    }

    //게시물 삭제
    @DeleteMapping("posts/{postId}")
    public DeletePostResponse deletePost (@LoginMemberId String userId,
                                          @PathVariable Long postId){

        return postService.delete(userId, postId);
    }

    //게시물 목록보기
    @GetMapping("/posts")
    public PostListAndPagingResponse getPostList (
            @RequestParam Long boardId,
            @RequestParam (required = false) String postTitle,
            @RequestParam (required = false) String userId,
            @RequestParam (defaultValue = "NEWEST")SortBy sortBy,
            @RequestParam (defaultValue = "DESC")Direction direction,
            @RequestParam (defaultValue = "1") int pageNumber,
            @RequestParam (defaultValue = "10") int size){

        Pageable pageable = PageRequest.of(pageNumber -1 , size);

        return postService.getPosts(userId, boardId, postTitle, sortBy, direction, pageable);
    }

    //좋아요
    @PostMapping("/posts/{postId}/like")
    public PostLikeResponse like (@LoginMemberId String userId,
                                  @PathVariable Long postId){


        return postLikeService.toggleLike(userId, postId);
    }

    //좋아요 한 목록 보기
    @GetMapping("/boards/liked")
    public PostsLikedAndPagingResponse getLikedPostList (@LoginMemberId String userId,
                                                         @RequestParam (defaultValue = "1") int pageNumber,
                                                         @RequestParam (defaultValue = "10") int size){

        Pageable pageable = PageRequest.of(pageNumber - 1, size);

        return postService.likedPostList(userId, pageable);
    }


}
