package redlightBack.stock;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Entity
public class FavoriteStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Stock stock;

    private String userId;

    public FavoriteStock(Stock stock, String userId) {
        this.stock = stock;
        this.userId = userId;
    }
}
