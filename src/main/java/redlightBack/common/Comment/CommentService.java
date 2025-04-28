package redlightBack.common.Comment;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redlightBack.common.Comment.Domain.Comment;
import redlightBack.common.Comment.Domain.CommentLike;
import redlightBack.common.Comment.Dto.CommentLikeResponse;
import redlightBack.common.Comment.Dto.CommentResponse;
import redlightBack.common.Comment.Dto.CommentResponseAndPaging;
import redlightBack.common.Comment.Dto.CreateCommentRequest;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CommentService {
public final CommentRepository commentRepository;
public final CommentLikeRepository commentLikeRepository;



//    public void save(CreateCommentRequest request) {
//
//        commentRepository.save(new Comment(
//                request.postId(),
//                request.parentId(),
//                request.content()
//        ));

//    }
public CommentResponseAndPaging getCommentTree(Long postId, String userId, Long parentId, Pageable pageable) {
    Page<Comment> page = commentRepository.findByAllPostId(postId, pageable);
    List<Comment> allComments = page.getContent();

    CommentResponseAndPaging.PageMeta pageMeta = new CommentResponseAndPaging.PageMeta(
            page.getTotalPages(),
            (int) page.getTotalElements(),
            page.getNumber(),
            page.getSize()
    );

    if (parentId == null) {
        // 댓글 + 대댓글 트리 구성
        List<CommentResponse.CommentItem> items = allComments.stream()
                .filter(c -> c.getParentId() == null)
                .map(comment -> {
                    List<CommentResponse.CommentItem.ReplyItem> replies = allComments.stream()
                            .filter(reply -> comment.getId().equals(reply.getParentId()))
                            .map(reply -> new CommentResponse.CommentItem.ReplyItem(
                                    reply.getId(),
                                    reply.getUserId(),
                                    reply.getContent(),
                                    reply.getLikeCount(),
                                    reply.isLikedByMe(),
                                    reply.getCreatedAt()
                            ))
                            .toList();

                    return new CommentResponse.CommentItem(
                            comment.getId(),
                            comment.getUserId(),
                            comment.getContent(),
                            comment.getLikeCount(),
                            comment.isLikedByMe(),
                            comment.getCreatedAt(),
                            replies
                    );
                })
                .toList();

        return new CommentResponseAndPaging(items, pageMeta);

    } else {
        // 특정 댓글의 대댓글만 반환
        List<Comment> replies = commentRepository.findAllByParentId(parentId);

        List<CommentResponse.CommentItem.ReplyItem> replyItems = replies.stream()
                .map(reply -> new CommentResponse.CommentItem.ReplyItem(
                        reply.getId(),
                        reply.getUserId(),
                        reply.getContent(),
                        reply.getLikeCount(),
                        reply.isLikedByMe(),
                        reply.getCreatedAt()
                ))
                .toList();

        CommentResponse.CommentItem commentItem = new CommentResponse.CommentItem(
                parentId,
                null,
                null,
                0,
                false,
                null,
                replyItems
        );

        return new CommentResponseAndPaging(List.of(commentItem), pageMeta);
    }
}


    //댓글 삭제(대댓글 남기고)
    @Transactional
    public void deleteComment(Long commentId, String userId) throws AccessDeniedException {
        // 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("댓글을 찾을 수 없습니다. commentId: " + commentId));

        // 작성자 확인
        if (!comment.getUserId().equals(userId)) {
            throw new AccessDeniedException("작성자만 삭제할 수 있습니다.");
        }

        // 삭제 처리 (소프트 삭제)
        comment.deletedAt();

        // 댓글 저장 (소프트 삭제된 상태로)
        commentRepository.save(comment);
    }


    //댓글수정
    @Transactional
    public void updateComment(CreateCommentRequest request, String userId, Long commentId) throws AccessDeniedException {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("댓글을 찾을 수 없습니다. commentId: " + commentId));

        if (!comment.getUserId().equals(userId)) {
            throw new AccessDeniedException("작성자만 수정할 수 있습니다.");

        }
        comment.setContent(request.content());
    }

    @Transactional
    public CommentLikeResponse toggleLikeComment(String userId, Long commentId) {

        // 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("댓글이 없습니다. id=" + commentId));

        // 기존 좋아요 여부 확인
        Optional<CommentLike> existing = commentLikeRepository.findByCommentIdAndUserId(commentId, userId);
        boolean nowLiked;

        if (existing.isPresent()) {
            // 이미 좋아요가 눌려있으면 → 좋아요 취소
            commentLikeRepository.delete(existing.get());
            comment.decrementLikeCount(); // 좋아요 수 감소
            nowLiked = false;
        } else {
            // 좋아요가 눌려있지 않으면 → 좋아요 추가
            commentLikeRepository.save(new CommentLike(commentId, userId));
            comment.incrementLikeCount(); // 좋아요 수 증가
            nowLiked = true;
        }

        // 댓글 업데이트
        commentRepository.save(comment);

        // 최종 응답 DTO 반환
        return new CommentLikeResponse(
                commentId,
                comment.getLikeCount(),
                nowLiked
        );
    }
}

