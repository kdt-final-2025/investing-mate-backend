package redlightBack.stock;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
public class FavoriteStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Stock stock;

    private String userId;

    protected FavoriteStock() {
    }

    public FavoriteStock(Stock stock, String userId) {
        this.stock = stock;
        this.userId = userId;
    }
}
