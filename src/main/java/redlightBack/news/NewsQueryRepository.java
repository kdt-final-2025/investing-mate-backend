package redlightBack.news;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class NewsQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final QNews news = QNews.news;

    public NewsQueryRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public List<News> findAll(String title,
                              Pageable pageable) {
        return queryFactory.selectFrom(news)
                .where(
                        findByTitle(title)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(toOrderSpecifiers(pageable.getSort()))
                .fetch();
    }

    private BooleanExpression findByTitle(String title) {
        if (title == null) {
            return null;
        }
        return news.title.containsIgnoreCase(title);
    }

    private OrderSpecifier<?>[] toOrderSpecifiers(Sort sort) {
        return sort.stream()
                .map(order -> {
                    boolean asc = order.isAscending();
                    switch (order.getProperty()) {
                        case "publishedAt":
                            return asc
                                    ? news.publishedAt.asc()
                                    : news.publishedAt.desc();
                        case "viewCount":
                            return asc
                                    ? news.viewCount.asc()
                                    : news.viewCount.desc();
                        default:
                            return news.publishedAt.desc();
                    }
                })
                .toArray(OrderSpecifier<?>[]::new);
    }

    public long totalCount(String title) {
        Long count = queryFactory
                .select(news.count())
                .from(news)
                .where(findByTitle(title))
                .fetchOne();
        // count가 null이면 0을 반환
        return count != null ? count : 0L;
    }
}
