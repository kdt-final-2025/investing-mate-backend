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

    public List<Stock> getAll(String userId, Pageable pageable) {
        // --- Pageable 에서 Sort 꺼내서 QueryDSL OrderSpecifier 로 변환 ---
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
        Sort sort = pageable.getSort();

        if (sort.isUnsorted()) {
            // 정렬 조건이 없으면 기본으로 ID 오름차순
            orderSpecifiers.add(stock.id.asc());
        } else {
            for (Sort.Order o : sort) {
                String prop = o.getProperty();
                boolean asc = o.isAscending();

                switch (prop) {
                    case "marketCap":
                        orderSpecifiers.add(asc
                                ? stock.marketCap.asc()
                                : stock.marketCap.desc());
                        break;

                    case "code":
                        orderSpecifiers.add(asc
                                ? stock.code.asc()
                                : stock.code.desc());
                        break;

                    default:
                        // 그 외 컬럼은 ID 정렬로 대체
                        orderSpecifiers.add(asc
                                ? stock.id.asc()
                                : stock.id.desc());
                }
            }
        }

        return queryFactory
                .selectFrom(stock)
                .join(favoriteStock).on(favoriteStock.stock.eq(stock))
                .where(favoriteStock.userId.eq(userId))
                .orderBy(orderSpecifiers.toArray(new OrderSpecifier<?>[0]))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
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
