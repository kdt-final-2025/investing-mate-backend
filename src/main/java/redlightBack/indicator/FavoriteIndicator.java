package redlightBack.indicator;

import jakarta.persistence.*;
import lombok.Getter;
import redlightBack.common.BaseEntity;

@Getter
@Entity
public class FavoriteIndicator extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Indicator indicator;

    private String userId;

    protected FavoriteIndicator() {
    }

    public FavoriteIndicator(Indicator indicator, String userId) {
        this.indicator = indicator;
        this.userId = userId;
    }
}
