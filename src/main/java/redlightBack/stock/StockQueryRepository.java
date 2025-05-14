package redlightBack.stock;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class StockQueryRepository {

    private final QStock stock = QStock.stock;
    private final QFavoriteStock favoriteStock = QFavoriteStock.favoriteStock;
    private final JPAQueryFactory queryFactory;

    public StockQueryRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public List<Stock> getFavoriteAll(String userId, Pageable pageable) {
        OrderSpecifier<?>[] orders = toOrderSpecifiers(pageable.getSort());
        return queryFactory
                .selectFrom(stock)
                .join(favoriteStock).on(favoriteStock.stock.eq(stock))
                .where(favoriteStock.userId.eq(userId))
                .orderBy(orders)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    public Long favoriteTotalCount(String userId) {
        Long count = queryFactory.select(favoriteStock.count())
                .from(favoriteStock)
                .where(favoriteStock.userId.eq(userId))
                .fetchOne();
        return count != null ? count : 0L;
    }

    public Long totalCount(String symbol) {
        Long count = queryFactory.select(stock.count())
                .from(stock)
                .where(findBySymbol(symbol))
                .fetchOne();
        return count != null ? count : 0L;
    }

    public List<Stock> getAll(String symbol, Pageable pageable) {
        OrderSpecifier<?>[] orders = toOrderSpecifiers(pageable.getSort());
        return queryFactory
                .selectFrom(stock)
                .where(findBySymbol(symbol))
                .orderBy(orders)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    public BooleanExpression findBySymbol(String symbol) {
        if (symbol == null) {
            return null;
        }
        return stock.symbol.contains(symbol);
    }

    private OrderSpecifier<?>[] toOrderSpecifiers(Sort sort) {
        if (sort == null || sort.isUnsorted()) {
            return new OrderSpecifier<?>[]{stock.marketCap.asc()};
        }

        return sort.stream()
                .map(order -> {
                    boolean asc = order.isAscending();
                    switch (order.getProperty()) {
                        case "marketCap":
                            return asc ? stock.marketCap.asc() : stock.marketCap.desc();
                        case "code":
                            // 프론트에서 property="code"로 내려준다고 가정
                            return asc ? stock.symbol.asc() : stock.symbol.desc();
                        default:
                            // 그 외에는 ID로 대체
                            return asc ? stock.id.asc() : stock.id.desc();
                    }
                })
                .toArray(OrderSpecifier[]::new);
    }
}
