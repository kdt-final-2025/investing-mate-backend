package redlightBack.indicator;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import redlightBack.common.BaseEntity;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class FavoriteIndicator extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Indicator indicator;

    private String userId;

    public FavoriteIndicator(Indicator indicator, String userId) {
        this.indicator = indicator;
        this.userId = userId;
    }
}
