package redlightBack.stockAlert;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface StockAlertRepository extends JpaRepository<StockAlert, Long> {

    @Query("select a from StockAlert a where a.triggered = false")
    List<StockAlert> findAllActive();

    List<StockAlert> findBySymbolAndTriggeredFalse(String symbol);

    List<StockAlert> findAllByUserId(String userId);

    @Transactional
    @Modifying
    void deleteByUserIdAndSymbolAndTargetPrice(String userId, String symbol, double targetPrice);

    boolean existsByUserIdAndSymbolAndTargetPrice(String userId, String symbol, double targetPrice);
}