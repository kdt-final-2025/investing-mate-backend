package redlightBack.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import redlightBack.comment.dto.CommentLikeResponse;
import redlightBack.comment.dto.CommentResponse;
import redlightBack.comment.dto.CommentResponseAndPaging;
import redlightBack.comment.dto.CreateCommentRequest;
import redlightBack.loginUtils.LoginMemberId;

import java.nio.file.AccessDeniedException;

@RequestMapping("/comments")
@RequiredArgsConstructor
@RestController
public class CommentController {

    public final CommentService commentService;


    @PostMapping
    public CommentResponse createComment(@LoginMemberId String userId,
                                         @RequestBody CreateCommentRequest request) {
        return commentService.save(userId, request);
    }


    //댓글 수정 로그인 유저만
    @PutMapping("/{commentId}")
    public void updateComment(@LoginMemberId String userId,
                              @PathVariable Long commentId,
                              @RequestBody CreateCommentRequest request) throws AccessDeniedException {
        commentService.updateComment(request, userId, commentId);
    }


    //소프트삭제
    @DeleteMapping("/{commentId}")
    public void deleteComment(@LoginMemberId String userId,
                              @PathVariable Long commentId) throws AccessDeniedException {
        commentService.deleteComment(commentId, userId);
    }


    //좋아요 버튼
    @PostMapping("/{commentId}/likes")
    public CommentLikeResponse toggleLike(
            @LoginMemberId String userId,
            @PathVariable Long commentId) {
        return commentService.toggleLikeComment(userId, commentId);

    }

    //댓글+대댓글 조회(좋아요순)
    @GetMapping
    public CommentResponseAndPaging getCommentsSortedByLikes(
            @RequestParam Long postId,
            @RequestParam(defaultValue = "Time") String sortType,
            @RequestParam(defaultValue = "1") int size,
            @RequestParam(defaultValue = "150") int pageNumber) {

        Pageable pageable = PageRequest.of(pageNumber - 1, size);


        return commentService.getCommentTree(postId, pageable, sortType);
    }


}
