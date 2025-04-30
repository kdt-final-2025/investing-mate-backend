package redlightBack.Comment;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import redlightBack.Comment.Domain.QComment;
import redlightBack.Comment.Domain.QCommentLike;
import redlightBack.Comment.Dto.CommentResponse;
import redlightBack.common.JpaConfig;

import java.util.List;

@Repository
public class LikeCountRepository extends JpaConfig {
    private final JPAQueryFactory jpaQueryFactory;

    public LikeCountRepository(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    QCommentLike qCommentLike = QCommentLike.commentLike;
    QComment qComment = QComment.comment;
    public List<CommentResponse> findCommentsSortedByLikes(Long postId, Pageable pageable) {
        return jpaQueryFactory
                .select(Projections.constructor(CommentResponse.class,
                        qComment.id,
                        qComment.content,
                        qComment.userId,
                        qComment.createdAt,
                        qCommentLike.count().as("likeCount")
                ))
                .from(qComment)
                .leftJoin(qCommentLike).on(qComment.id.eq(qCommentLike.comment.id))
                .where(qComment.postId.eq(postId))
                .groupBy(qComment.id)
                .orderBy(qCommentLike.count().desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

}