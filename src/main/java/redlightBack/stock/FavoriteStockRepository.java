package redlightBack.stock;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FavoriteStockRepository extends JpaRepository<FavoriteStock, Long> {

    boolean existsByStock_IdAndUserId(Long stockId, String userId);

    void deleteByStock_IdAndUserId(Long stockId, String userId);

    @Query("select f.stock from FavoriteStock f where f.userId = :userId")
    List<Stock> findStocksByUserId(@Param("userId") String userId);
}
