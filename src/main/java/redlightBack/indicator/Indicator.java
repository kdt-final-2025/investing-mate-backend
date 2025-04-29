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
    private Long id;

    private String name;

    public Indicator(String name) {
        this.name = name;
    }
}