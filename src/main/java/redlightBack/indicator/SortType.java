package redlightBack.indicator;

import com.querydsl.core.types.OrderSpecifier;

public enum SortType {
    LATEST {
        @Override
        public OrderSpecifier<?> getOrder(QFavoriteIndicator fav) {
            return fav.createdAt.desc();
        }
    },
    OLDEST {
        @Override
        public OrderSpecifier<?> getOrder(QFavoriteIndicator fav) {
            return fav.createdAt.asc();
        }
    };

    public abstract OrderSpecifier<?> getOrder(QFavoriteIndicator fav);
}
