package redlightBack.Comment;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import redlightBack.Comment.Domain.QComment;
import redlightBack.Comment.Domain.QCommentLike;
import redlightBack.Comment.Dto.CommentSortedByLikesResponse;

import java.util.List;

@Repository
public class LikeCountRepository {
    private final JPAQueryFactory jpaQueryFactory;

    public LikeCountRepository(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }


    QCommentLike qCommentLike = QCommentLike.commentLike;
    QComment qComment = QComment.comment;

    public List<CommentSortedByLikesResponse> findCommentsSortedByLikes(Long postId, Pageable pageable) {

        QComment qChild = new QComment("qChild");

        return jpaQueryFactory
                .select(Projections.constructor(CommentSortedByLikesResponse.class,
                        qComment.id,
                        qComment.parent,
                        qComment.userId,
                        qComment.content,
                        qCommentLike.count().intValue().as("likeCount"),
                        qComment.likedByMe,
                        qComment.createdAt,
                        JPAExpressions.select(qChild.count().intValue())
                                .from(qChild)
                                .where(qChild.parent.id.eq(qComment.id))
                ))
                .from(qComment)
                .leftJoin(qCommentLike).on(qComment.id.eq(qCommentLike.comment.id))
                .where(qComment.postId.eq(postId))
                .groupBy(qComment.id, qComment.content, qComment.userId, qComment.createdAt)
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
                .where(qComment.postId.eq(postId))
                .fetchOne();

        return result != null ? result : 0L;


}}