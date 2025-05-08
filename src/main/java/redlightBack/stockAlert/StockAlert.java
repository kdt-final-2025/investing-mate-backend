package redlightBack.stockAlert;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class StockAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;

    private double targetPrice;

    private String symbol;

    private boolean above;

    private boolean triggered;

    public StockAlert(String userId, double targetPrice, String symbol, boolean above) {
        this.userId = userId;
        this.targetPrice = targetPrice;
        this.symbol = symbol;
        this.above = above;
    }

    public void markTriggered() {
        this.triggered = true;
    }
}
