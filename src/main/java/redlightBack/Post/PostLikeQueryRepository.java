package redlightBack.Post;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import redlightBack.Board.QBoard;
import redlightBack.Post.Dto.PostLikeDto;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class PostLikeQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final QPostLike qPostLike = QPostLike.postLike;
    private final QPost qPost = QPost.post;
    private final QBoard qBoard = QBoard.board;

    public List<PostLikeDto> findPostsLikedByUser (String userId, Long offset, int size){

        return queryFactory.select(Projections.constructor(PostLikeDto.class,
                        qPost.id,          // ② 추가: post.id
                        qBoard.id,
                        qBoard.boardName,
                        qPost.postTitle,
                        qPostLike.userId,
                        qPost.viewCount,
                        qPost.commentCount,
                        qPost.likeCount,
                        qPost.createdAt))
                .from(qPostLike)
                .join(qPostLike.post, qPost)
                .join(qBoard).on(qPost.boardId.eq(qBoard.id))
                .where(qPostLike.userId.eq(userId),
                        qPost.deletedAt.isNull())
                .orderBy(qPost.createdAt.desc())
                .offset(offset)
                .limit(size)
                .fetch();
    }

    public long countLikedPosts(String userId){
        Long totalLikedElements = queryFactory.select(qPost.count())
                .from(qPostLike)
                .join(qPostLike.post, qPost)
                .where(qPostLike.userId.eq(userId),
                        qPostLike.liked.isTrue(),
                        qPost.deletedAt.isNull())
                .fetchOne();

        return totalLikedElements != null ? totalLikedElements : 0L;
    }
}
