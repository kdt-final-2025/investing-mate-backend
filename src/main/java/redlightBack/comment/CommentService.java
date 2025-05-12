package redlightBack.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redlightBack.comment.domain.Comment;
import redlightBack.comment.domain.CommentLike;
import redlightBack.comment.dto.*;
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
    public final LikeCountRepository likeCountRepository;
    public final LikeSortedCommentTreeBuilder likeSortedCommentTreeBuilder;

    //생성
    public CommentResponse save(String userId, CreateCommentRequest request) {

        Post post = postRepository.findById(request.postId())
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        Comment parent = null;
        if (request.parentId() != null) {
            parent = commentRepository.findById(request.parentId())
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글이 존재하지 않습니다."));
        }

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
                false,
                comment.getCreatedAt(),
                List.of());
    }



    //댓글 삭제(대댓글 남기고)
    @Transactional
    public void deleteComment(Long commentId, String userId) throws AccessDeniedException {
        // 댓글 조회
        Comment comment = commentRepository.findByIdAndDeleteIsNull(commentId)
                .orElseThrow(() -> new NoSuchElementException("댓글을 찾을 수 없습니다. commentId: " + commentId));

        // 작성자 확인
        if (!comment.getUserId().equals(userId)) {
            throw new AccessDeniedException("작성자만 삭제할 수 있습니다.");
        }

        // 삭제 처리 (소프트 삭제)
        comment.delete();

        // 댓글 저장 (소프트 삭제된 상태로)
        commentRepository.save(comment);
    }


    //댓글수정
    @Transactional
    public void updateComment(CreateCommentRequest request, String userId, Long commentId) throws AccessDeniedException {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("댓글을 찾을 수 없습니다. commentId: " + commentId));

        if (comment.getUserId() == null || !comment.getUserId().equals(userId)) {
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
            commentLikeRepository.save(new CommentLike(comment, userId));
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
    //좋아요 순 조회
    @Transactional
    public CommentResponseAndPaging getCommentTree(Long postId, Pageable pageable, String sortType) {
        PageMeta pageMeta;

        if (sortType.equals("LIKE")) {
            // 좋아요순 댓글 조회
            List<CommentSortedByLikesResponse> likeComments = likeCountRepository.findCommentsSortedByLikes(postId, pageable);

            long totalElements = likeCountRepository.countCommentsByPostId(postId);

            // 좋아요순 트리 빌드
            List<CommentSortedByLikesResponse> likeCommentTree = likeSortedCommentTreeBuilder.buildFromResponses(likeComments);

            // CommentResponse로 변환 (children도 재귀적으로 변환)
            List<CommentResponse> commentResponses = likeCommentTree.stream()
                    .map(this::convertToCommentResponse)
                    .toList();

            pageMeta = new PageMeta(
                    (int) Math.ceil((double) totalElements / pageable.getPageSize()),
                    totalElements,
                    pageable.getPageNumber(),
                    pageable.getPageSize()
            );

            return new CommentResponseAndPaging(commentResponses, pageMeta);
        } else {

            // 시간순 조회 (기존 로직)
            Page<Comment> commentPage = commentRepository.findByPostId(postId, pageable);

            List<CommentResponse> comments = commentTreeBuilder.build(commentPage.getContent());

            pageMeta = new PageMeta(
                    commentPage.getTotalPages(),
                    commentPage.getTotalElements(),
                    pageable.getPageNumber(),
                    pageable.getPageSize()
            );

            return new CommentResponseAndPaging(comments, pageMeta);
        }


    }
    private CommentResponse convertToCommentResponse(CommentSortedByLikesResponse response) {
        List<CommentResponse> children = response.getChildren() == null ? List.of() :
                response.getChildren().stream()
                        .map(this::convertToCommentResponse)
                        .toList();

        return new CommentResponse(
                response.getCommentId(),
                response.getUserId(),
                response.getContent(),
                response.getLikeCount(),
                response.isLikedByMe(),
                response.getCreatedAt(),
                children
        );
    }
}
