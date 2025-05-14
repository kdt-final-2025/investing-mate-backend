package redlightBack.stockRecommendation;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import redlightBack.stockRecommendation.dto.SortBy;
import redlightBack.stockRecommendation.dto.SortDirection;

import java.util.List;


@Repository
@RequiredArgsConstructor
public class StockInfoQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final QStockRecommendation qStockRecommendation = QStockRecommendation.stockRecommendation;


    public List<StockRecommendation> recommend(Double minDividend, Double maxPriceRatio, SortBy sortBy, SortDirection sortDirection, int limit){
        return queryFactory.selectFrom(qStockRecommendation)
                .where(byDividend(minDividend), byPriceRatio(maxPriceRatio))
                .orderBy(orderSpecifier(sortBy, sortDirection))
                .limit(limit)
                .fetch();
    }

    // 	배당률이 최소 기준 이상인 종목
    private BooleanExpression byDividend(Double min){
        return (min != null) ? qStockRecommendation.dividendYield.gt(min) : null;
    }

    // 기간 고점 대비 저평가된 종목
    // 예: maxRatio = 0.8 이면, 현재가가 1년 고가의 80% 이하인 종목만 반환
    private BooleanExpression byPriceRatio(Double maxRatio){
        return (maxRatio != null) ? qStockRecommendation.currentPrice.divide(qStockRecommendation.highPrice1y).lt(maxRatio) : null;
    }

    private OrderSpecifier<?> orderSpecifier (SortBy sortBy, SortDirection sortDirection){

        Order direction = Order.valueOf(sortDirection.name());

        if(sortBy.equals(SortBy.PRICEGAP)){
            return new OrderSpecifier<>(
                    direction,
                    qStockRecommendation.currentPrice.divide(qStockRecommendation.highPrice1y)
            );
        } else if(sortBy.equals(SortBy.RISK)){
            return new OrderSpecifier<>(
                    direction,
                    new CaseBuilder()
                            .when(
                                    qStockRecommendation.dividendYield.gt(4.0)
                                            .and(qStockRecommendation.currentPrice.divide(qStockRecommendation.highPrice1y).gt(0.9))
                            ).then(0)
                            .when(qStockRecommendation.dividendYield.gt(2.0)
                                    .and(qStockRecommendation.currentPrice.divide((qStockRecommendation.highPrice1y)).gt(0.85))
                            ).then(1)
                            .otherwise(2)
            );
        }else{
            return new OrderSpecifier<>(
                    direction,
                    qStockRecommendation.dividendYield
            );
        }
    }



}
