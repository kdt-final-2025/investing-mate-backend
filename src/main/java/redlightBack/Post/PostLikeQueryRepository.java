package redlightBack.Post;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import redlightBack.Board.QBoard;
import redlightBack.Post.Dto.LikedPostListResponse;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class PostLikeQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final QPostLike qPostLike = QPostLike.postLike;
    private final QPost qPost = QPost.post;
    private final QBoard qBoard = QBoard.board;


    public PostLikeQueryRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public List<LikedPostListResponse> postListLikedByUser (String userId, Long offset, int size){

        List<Tuple> tupleList = queryFactory.select(qPost.id, qPost.postTitle, qPost.createdAt, qBoard.id, qBoard.boardName)
                .from(qPostLike)
                .join(qPostLike.post, qPost)
                .join(qBoard).on(qPost.boardId.eq(qBoard.id))
                .where(qPostLike.userId.eq(userId),
                        qPost.deletedAt.isNull())
                .orderBy(qPost.createdAt.desc())
                .offset(offset)
                .limit(size)
                .fetch();

        return tupleList.stream().map(list -> new LikedPostListResponse(
                list.get(qPost.boardId),
                list.get(qBoard.boardName),
                list.get(qPost.postTitle),
                list.get(qPost.userId),
                Optional.ofNullable(list.get(qPost.viewCount)).orElse(0),
                Optional.ofNullable(list.get(qPost.commentCount)).orElse(0),
                Optional.ofNullable(list.get(qPost.likeCount)).orElse(0),
                list.get(qPost.createdAt))
        ).toList();
    }

    public long countLikedPosts(String userId){
        Long totalLikedElements = queryFactory.select(qPost.count())
                .from(qPostLike)
                .join(qPostLike.post, qPost)
                .where(qPostLike.userId.eq(userId),
                        qPost.deletedAt.isNull())
                .fetchOne();

        return totalLikedElements != null ? totalLikedElements : 0L;
    }
}
