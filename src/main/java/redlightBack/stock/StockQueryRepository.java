package redlightBack.stock;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StockQueryRepository {

    private final QStock stock = QStock.stock;
    private final QFavoriteStock favoriteStock = QFavoriteStock.favoriteStock;
    private final JPAQueryFactory queryFactory;

    public StockQueryRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public List<Stock> getAll(
            String userId,
            String sortBy,
            String order,
            int page,
            int size
    ) {
        // --- 동적 ORDER BY 조립 ---
        OrderSpecifier<?> orderSpec;
        boolean asc = "asc".equalsIgnoreCase(order);
        if ("marketCap".equals(sortBy)) {
            orderSpec = asc
                    ? stock.marketCap.asc()
                    : stock.marketCap.desc();
        } else if ("code".equals(sortBy)) {
            orderSpec = asc
                    ? stock.code.asc()
                    : stock.code.desc();
        } else {
            // 기본 정렬: id 오름차순
            orderSpec = stock.id.asc();
        }

        // --- 페이징용 offset, limit 계산 ---
        long offset = (long) page * size;
        long limit = size;

        return queryFactory
                .selectFrom(stock)
                .join(favoriteStock).on(favoriteStock.stock.eq(stock))
                .where(favoriteStock.userId.eq(userId))       // null 인 조건은 자동 무시
                .orderBy(orderSpec)
                .offset(offset)
                .limit(limit)
                .fetch();
    }

    public Long totalCount(String userId) {
        Long count = queryFactory.select(favoriteStock.count())
                .from(favoriteStock)
                .where(favoriteStock.userId.eq(userId))
                .fetchOne();
        return count != null ? count : 0L;
    }
}
