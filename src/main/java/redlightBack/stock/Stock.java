package redlightBack.stock;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String symbol;

    private BigDecimal marketCap;

    private LocalDateTime updatedAt;

    public Stock(String name, String symbol, BigDecimal marketCap) {
        this.name = name;
        this.symbol = symbol;
        this.marketCap = marketCap;
        this.updatedAt = LocalDateTime.now();
    }

    public void update(String name, BigDecimal cap) {
        this.name = name;
        this.marketCap = cap;
        this.updatedAt = LocalDateTime.now();
    }
}
