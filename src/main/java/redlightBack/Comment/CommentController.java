package redlightBack.Comment;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import redlightBack.Comment.Dto.CommentLikeResponse;
import redlightBack.Comment.Dto.CommentResponse;
import redlightBack.Comment.Dto.CommentResponseAndPaging;
import redlightBack.Comment.Dto.CreateCommentRequest;
import redlightBack.loginUtils.LoginMemberId;

import java.nio.file.AccessDeniedException;


@RequiredArgsConstructor
@RestController
public class CommentController {

    public final CommentService commentService;


    @PostMapping("/comments")
    public CommentResponse createComment(@LoginMemberId String userId,
                                         @RequestBody CreateCommentRequest request){
     return commentService.save(userId,request);
    }

    //댓글+대댓글 조회
    @GetMapping("/comments")
    public CommentResponseAndPaging getCommentTree(@RequestParam Long postId,
                                                   @RequestParam(defaultValue = "1") int size,
                                                   @RequestParam(defaultValue = "150") int pageNumber ){

        Pageable pageable = PageRequest.of(pageNumber -1, size);

        return commentService.getCommentTree(postId, pageable);
    }


    //댓글 수정 로그인 유저만
    @PutMapping("/comments/{commentId}")
    public void updateComment(@LoginMemberId String userId,
                              @PathVariable Long commentId,
                              @RequestBody CreateCommentRequest request) throws AccessDeniedException {
        commentService.updateComment(request,userId,commentId);
    }


    //소프트삭제
    @DeleteMapping("/{commentId}")
    public void deleteComment(@LoginMemberId String userId,
                              @PathVariable Long commentId) throws AccessDeniedException {
        commentService.deleteComment(commentId, userId);
    }


    //좋아요 버튼
    @PostMapping("/comments/{commentId}/likes")
    public CommentLikeResponse toggleLike(@LoginMemberId String userId,@PathVariable Long commentId){
        return  commentService.toggleLikeComment(userId,commentId);

    }

    //댓글+대댓글 조회(좋아요순)
    @GetMapping("/comments/likes")
    public CommentResponseAndPaging getCommentTreeByLikeCount(@RequestParam Long postId,
                                                              @RequestParam(defaultValue = "time") String sort,
                                                   @RequestParam(defaultValue = "1") int size,
                                                   @RequestParam(defaultValue = "150") int pageNumber ){

        Pageable pageable = PageRequest.of(pageNumber -1, size);

        return commentService.getCommentTreeByLikeCount(postId, pageable,sort);
    }


}
