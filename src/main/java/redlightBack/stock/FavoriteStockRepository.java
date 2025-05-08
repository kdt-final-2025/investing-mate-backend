package redlightBack.stock;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FavoriteStockRepository extends JpaRepository<FavoriteStock, Long> {

    boolean existsByStock_IdAndUserId(Long stockId, String userId);

    void deleteByStock_IdAndUserId(Long stockId, String userId);
}
