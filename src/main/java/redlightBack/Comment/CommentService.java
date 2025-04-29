package redlightBack.Comment;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redlightBack.Comment.Domain.Comment;
import redlightBack.Comment.Domain.CommentLike;
import redlightBack.Comment.Dto.*;
import redlightBack.Post.Post;
import redlightBack.Post.PostRepository;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CommentService {
    public final CommentRepository commentRepository;
    public final CommentLikeRepository commentLikeRepository;
    public final  PostRepository postRepository;
    public final CommentTreeBuilder commentTreeBuilder;



    //생성
    public CommentResponse save(String userId, CreateCommentRequest request) {

        Post post = postRepository.findById(request.postId())
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        Comment parent = request.parent() != null ? request.parent().parent() : null;

        Comment comment = new Comment(
                userId,
                request.content(),
                post.getId(),
                parent
        );

        commentRepository.save(comment);

        return new CommentResponse(comment.getId(),
                comment.getUserId(),
                comment.getContent(),
                comment.getLikeCount(),
                comment.isLikedByMe(),
                comment.getCreatedAt(),
                List.of());
    }


//    //댓글 조회 및 페이징
//    public CommentResponseAndPaging getCommentTree(Long postId, Pageable pageable) {
//        Page<Comment> commentPage = commentRepository.findByAllPostId(postId, pageable);  // 댓글 + 대댓글 다 조회
//
//        List<CommentResponse> comments = commentPage.getContent().stream()
//                .map(comment -> new CommentResponse(
//                        comment.getId(),
//                        comment.getUserId(),
//                        comment.getContent(),
//                        comment.getLikeCount(),
//                        comment.isLikedByMe(),
//                        comment.getCreatedAt(),
//                        List.of()
//                ))
//                .collect(Collectors.toList());
//
//        PageMeta pageMeta = new PageMeta(
//                commentPage.getTotalPages(),
//                commentPage.getTotalElements(),
//                pageable.getPageNumber(),
//                pageable.getPageNumber());
//
//        return new CommentResponseAndPaging(comments, pageMeta);
//    }


    //댓글 조회 및 페이징 및 트리구조
    public CommentResponseAndPaging getCommentTree(Long postId, Pageable pageable) {
        Page<Comment> commentPage = commentRepository.findByPostId(postId, pageable);  // 댓글 + 대댓글 다 조회

        // 평면 → 트리로 변환
        List<CommentResponse> comments = commentTreeBuilder.build(commentPage.getContent());

        PageMeta pageMeta = new PageMeta(
                commentPage.getTotalPages(),
                commentPage.getTotalElements(),
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        return new CommentResponseAndPaging(comments, pageMeta);
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

    //좋아요
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

