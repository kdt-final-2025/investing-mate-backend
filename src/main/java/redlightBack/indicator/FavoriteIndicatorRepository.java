package redlightBack.indicator;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FavoriteIndicatorRepository extends JpaRepository<FavoriteIndicator, Long> {

    Optional<FavoriteIndicator> findByUserIdAndIndicator_Id(String userId, Long indicatorId);

    @Query("SELECT fi.indicator.id FROM FavoriteIndicator fi WHERE fi.userId = :userId")
    List<Long> findIndicatorIdsByUserId(@Param("userId") String userId);
}
