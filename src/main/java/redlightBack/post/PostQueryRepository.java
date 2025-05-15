package redlightBack.post;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import redlightBack.post.dto.PostDto;
import redlightBack.post.enums.Direction;
import redlightBack.post.enums.SortBy;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class PostQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final QPost qPost = QPost.post;
    private final QPostLike qPostLike = QPostLike.postLike;

    public List<PostDto> searchAndOrderingPosts(Long boardId, String postTitle, String userId, SortBy sortBy, Direction direction, Long offset, int size) {

        return queryFactory.select(Projections.constructor(PostDto.class, qPost.id,
                        qPost.postTitle,
                        qPost.userId,
                        qPost.viewCount,
                        qPost.commentCount,
                        qPostLike.id.count(),
                        qPost.createdAt))
                .from(qPost)
                .where(qPost.boardId.eq(boardId),
                        qPost.deletedAt.isNull(),
                        searchByTitle(postTitle),
                        searchByUserId(userId))
                .leftJoin(qPostLike).on(qPostLike.post.id.eq(qPost.id))
                .groupBy(qPost.id)
                .orderBy(orderSpecifiers(sortBy, direction))
                .offset(offset)
                .limit(size)
                .fetch();
    }

    //제목 검색 조건
    private BooleanExpression searchByTitle (String postTitle){
        return postTitle == null ? null : QPost.post.postTitle.containsIgnoreCase(postTitle);
    }

    //userId 검색 조건
    private BooleanExpression searchByUserId (String userId){
        return userId == null ? null : QPost.post.userId.contains(userId);
    }

    //정렬 조건
    private OrderSpecifier<?>[] orderSpecifiers (SortBy sortBy, Direction direction){

        boolean asc = direction.equals(Direction.ASC);

        //좋아요 정렬 조건 있을 때
        if(sortBy.equals(SortBy.MOST_LIKED)){
            if(asc){
                return new OrderSpecifier<?>[]{
                        qPost.likeCount.desc(),
                        qPost.createdAt.asc()
                };
            }

            return new OrderSpecifier<?>[]{
                    qPost.likeCount.desc(),
                    qPost.createdAt.desc()
            };
        }

        //createdAt 정렬 조건이거나 조건이 없을 때
        if (asc){
            return new OrderSpecifier<?>[]{
                    qPost.createdAt.asc()
            };
        }return new OrderSpecifier<?>[]{
                qPost.createdAt.desc()
        };

    }

    public long countPosts(Long boardId, String postTitle, String userId){
        Long totalElements = queryFactory.select(qPost.count())
                .from(qPost)
                .where(qPost.boardId.eq(boardId),
                        qPost.deletedAt.isNull(),
                        searchByTitle(postTitle),
                        searchByUserId(userId))
                .fetchOne();

        return totalElements != null ? totalElements : 0L;
    }
}
