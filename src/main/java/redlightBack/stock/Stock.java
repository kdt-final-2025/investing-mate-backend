package redlightBack.stock;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import redlightBack.common.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Stock extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String symbol;

    private BigDecimal marketCap;

    private String exchange;

    public Stock(String symbol, String name, BigDecimal marketCap, String exchange) {
        this.symbol = symbol;
        this.name = name;
        this.marketCap = marketCap;
        this.exchange = exchange;
    }

    public void update(String name, BigDecimal cap, String exchange) {
        this.name = name;
        this.marketCap = cap;
        this.exchange = exchange;
    }
}
