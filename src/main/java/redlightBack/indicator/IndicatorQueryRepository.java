package redlightBack.indicator;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public List<Indicator> getFavoriteAll(String userId,
                                                  OrderSpecifier<?> orderSpecifier,
                                                  long offset,
                                                  long limit) {
        return queryFactory
                .select(favoriteIndicator.indicator)
                .from(favoriteIndicator)
                .join(favoriteIndicator.indicator, indicator).fetchJoin()
                .where(favoriteIndicator.userId.eq(userId))
                .orderBy(orderSpecifier)
                .offset(offset)
                .limit(limit)
                .fetch();
    }


    public long favoriteTotalCount(String userId) {
        Long count = queryFactory.select(favoriteIndicator.count())
                .from(favoriteIndicator)
                .where(favoriteIndicator.userId.eq(userId))
                .fetchOne();
        return count != null ? count : 0L;
    }

    public List<Indicator> getAll(Pageable pageable) {
        OrderSpecifier<?>[] orders = toOrderSpecifiers(pageable.getSort());
        return queryFactory
                .selectFrom(indicator)
                .orderBy(orders)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private OrderSpecifier<?>[] toOrderSpecifiers(Sort sort) {
        if (sort == null || sort.isUnsorted()) {
            return new OrderSpecifier<?>[]{indicator.date.asc()};
        }

        return sort.stream()
                .map(order -> {
                    boolean asc = order.isAscending();
                    switch (order.getProperty()) {
                        default:
                            return asc ? indicator.date.asc() : indicator.date.desc();
                    }
                })
                .toArray(OrderSpecifier[]::new);
    }

    public long totalCount() {
        Long count = queryFactory.select(favoriteIndicator.count())
                .from(favoriteIndicator)
                .fetchOne();
        return count != null ? count : 0L;
    }
}
