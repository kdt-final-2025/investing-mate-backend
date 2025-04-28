package redlightBack.indicator;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class IndicatorQueryRepository {

    private final QIndicator indicator = QIndicator.indicator;
    private final JPAQueryFactory queryFactory;
    private final QFavoriteIndicator favoriteIndicator = QFavoriteIndicator.favoriteIndicator;

    public IndicatorQueryRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public List<FavoriteIndicator> getAll(String userId,
                                          OrderSpecifier<?> orderSpecifier,
                                          long offset,
                                          long limit) {
        return queryFactory
                .selectFrom(favoriteIndicator)
                .join(favoriteIndicator.indicator, indicator).fetchJoin()
                .where(favoriteIndicator.userId.eq(userId))
                .orderBy(orderSpecifier)
                .offset(offset)
                .limit(limit)
                .fetch();
    }


    public long totalCount(String userId) {
        Long count = queryFactory.select(favoriteIndicator.count())
                .from(favoriteIndicator)
                .where(favoriteIndicator.userId.eq(userId))
                .fetchOne();
        return count != null ? count : 0L;
    }


}
