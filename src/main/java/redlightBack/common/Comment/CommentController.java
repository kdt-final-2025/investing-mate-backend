package redlightBack.common.Comment;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import redlightBack.common.Comment.Dto.CommentLikeResponse;
import redlightBack.common.Comment.Dto.CommentResponse;
import redlightBack.common.Comment.Dto.CommentResponseAndPaging;
import redlightBack.common.Comment.Dto.CreateCommentRequest;
import redlightBack.loginUtils.LoginMemberId;

import java.nio.file.AccessDeniedException;


@RequiredArgsConstructor
@RestController
public class CommentController {

    public final CommentService commentService;


    @PostMapping("")
    public CommentResponse.CommentItem createComment(@LoginMemberId String userId,
                                         @RequestBody CreateCommentRequest request){
     return commentService.save(userId,request);
    }

    //댓글+대댓글 조회
    @GetMapping("")
    public CommentResponseAndPaging getCommentTree(@RequestParam Long postId,
                                                   @RequestParam String userId,
                                                   @PathVariable Long parentId
    , @RequestParam(defaultValue = "1") int size , @RequestParam(defaultValue = "150") int pageNumber ){

        Pageable pageable = PageRequest.of(pageNumber -1, size);

        return commentService.getCommentTree(postId,userId,parentId, pageable);
    }


    //댓글 수정 로그인 유저만
    @PutMapping()
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
    @PostMapping()
    public CommentLikeResponse toggleLike(@LoginMemberId String userId,@PathVariable Long commentId){
        return  commentService.toggleLikeComment(userId,commentId);

    }
}
