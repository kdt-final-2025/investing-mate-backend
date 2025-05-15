package redlightBack.Comment;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import redlightBack.Comment.Domain.QComment;
import redlightBack.Comment.Domain.QCommentLike;
import redlightBack.Comment.Dto.CommentSortedByLikesResponse;
import redlightBack.Comment.Dto.QCommentSortedByLikesResponse;

import java.util.List;

@Repository
public class LikeCountRepository {
    private final JPAQueryFactory jpaQueryFactory;

    public LikeCountRepository(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    public List<CommentSortedByLikesResponse> findCommentsSortedByLikes(Long postId, Pageable pageable) {
        QCommentLike qCommentLike = QCommentLike.commentLike;
        QComment qComment = QComment.comment;

        return jpaQueryFactory
                .select(new QCommentSortedByLikesResponse(
                        qComment.id,
                        qComment.parent.id !=null ? qComment.parent.id : null, // parentId
                        qComment.userId,
                        qComment.content,
                        qCommentLike.count().intValue(), // likeCount
                        Expressions.constant(false), // likedByMe: 사용자 로그인 정보 필요시 동적 처리
                        qComment.createdAt,
                        qComment.delete
                        // children은 트리 구성에서 추가로 처리
                ))
                .from(qComment)
                .leftJoin(qCommentLike).on(qComment.id.eq(qCommentLike.comment.id))
                .where(qComment.postId.eq(postId).and(qComment.delete.isNull()))
                .groupBy(qComment.id, qComment.parent.id, qComment.userId, qComment.content, qComment.createdAt)
                .orderBy(qCommentLike.count().desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    public long countCommentsByPostId(Long postId) {
        QComment qComment = QComment.comment;

        Long result = jpaQueryFactory
                .select(qComment.count())
                .from(qComment)
                .where(qComment.postId.eq(postId).and(qComment.delete.isNull()))
                .fetchOne();

        return result != null ? result : 0L;


}}