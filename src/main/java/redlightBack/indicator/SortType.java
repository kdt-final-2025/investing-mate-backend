package redlightBack.indicator;


import com.querydsl.core.types.OrderSpecifier;

public enum SortType {
    LATEST {
        @Override
        public OrderSpecifier<?> getOrder(QIndicator indicator) {
            return indicator.createdAt.desc();
        }
    },
    OLDEST {
        @Override
        public OrderSpecifier<?> getOrder(QIndicator indicator) {
            return indicator.createdAt.asc();
        }
    };

    public abstract OrderSpecifier<?> getOrder(QIndicator indicator);
}
