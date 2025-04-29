package redlightBack.indicator;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import redlightBack.common.BaseEntity;

import java.time.LocalDate;

@NoArgsConstructor
@Getter
@Entity
public class Indicator extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 예: WB_WDI_SP_POP_TOTL

    private String name;        // 예: Total Population

    public Indicator(String name, LocalDate nextReleaseDate) {
        this.name = name;
    }
}