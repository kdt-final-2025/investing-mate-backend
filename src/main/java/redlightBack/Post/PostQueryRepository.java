package redlightBack.Post;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PostQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final QPost qPost = QPost.post;

    public PostQueryRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public List<Post> searchAndOrderingPosts (Long boardId, String postTitle, String userId, String sortBy, String direction, Long offset, int size){

        return queryFactory.select(qPost)
                .from(qPost)
                .where(qPost.boardId.eq(boardId), qPost.deletedAt.isNull(), searchByTitle(postTitle), searchByUserId(userId))
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
    private OrderSpecifier<?>[] orderSpecifiers (String sortBy, String direction){

        boolean asc = "asc".equals(direction);

        //좋아요 정렬 조건 있을 때
        if("likeCount".equals(sortBy)){
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




