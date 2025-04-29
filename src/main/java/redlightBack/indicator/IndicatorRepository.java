package redlightBack.indicator;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface IndicatorRepository extends JpaRepository<Indicator, Long> {

    List<Indicator> findAllByNameIn(Collection<String> names);
}
