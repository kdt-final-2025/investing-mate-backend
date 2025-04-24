package redlightBack.indicator;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
public class FavoriteIndicator {

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
