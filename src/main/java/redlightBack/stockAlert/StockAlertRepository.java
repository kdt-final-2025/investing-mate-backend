package redlightBack.stockAlert;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StockAlertRepository extends JpaRepository<StockAlert, Long> {

    @Query("select a from StockAlert a where a.triggered = false")
    List<StockAlert> findAllActive();

    List<StockAlert> findBySymbolAndTriggeredFalse(String symbol);
}