package redlightBack.stockRecommendation;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
@RequiredArgsConstructor
public class StockInfoQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final QStockRecommendation qStockRecommendation = QStockRecommendation.stockRecommendation;


    public List<StockRecommendation> recommend(Double minDividend,
                                               Double maxPriceRatio,
                                               RiskLevel riskLevel,
                                               int limit){
        return queryFactory.selectFrom(qStockRecommendation)
                .where(byDividend(minDividend),
                        byPriceRatio(maxPriceRatio),
                        byRiskLevel(riskLevel))
                .orderBy(getOrderBy(riskLevel))
                .limit(limit)
                .fetch();
    }

    // 	배당률이 최소 기준 이상인 종목
    private BooleanExpression byDividend(Double min){
        return (min != null) ? qStockRecommendation.dividendYield.goe(min) : null;
    }

    // 기간 고점 대비 저평가된 종목
    // 예: maxRatio = 0.8 이면, 현재가가 1년 고가의 80% 이하인 종목만 반환
    private BooleanExpression byPriceRatio(Double maxRatio){
        return (maxRatio != null) ? qStockRecommendation.currentToHighRatio.loe(maxRatio) : null;
    }

    //위험도 조건
    private BooleanExpression byRiskLevel(RiskLevel level){
        return (level != null) ? qStockRecommendation.riskLevel.eq(level) : null;
    }

    private OrderSpecifier<?>[] getOrderBy (RiskLevel level){

        if(level == RiskLevel.LOW)
            return new OrderSpecifier<?>[]{
                    qStockRecommendation.riskLevel.asc(),
                    qStockRecommendation.dividendYield.asc()
            };

        else return new OrderSpecifier<?>[]{
                qStockRecommendation.dividendYield.desc()
        };
    }

}
