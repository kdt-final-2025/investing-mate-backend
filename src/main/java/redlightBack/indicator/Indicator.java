package redlightBack.indicator;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import redlightBack.common.BaseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Indicator extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private String korName;

    private String country;

    private LocalDateTime date;

    private double actual;

    private double previous;

    private double estimate;

    @Enumerated(EnumType.STRING)
    private Impact impact;

    public Indicator(String name, String korName, String country, LocalDateTime date, double actual, double previous, double estimate, Impact impact) {
        this.name = name;
        this.korName = korName;
        this.country = country;
        this.date = date;
        this.actual = actual;
        this.previous = previous;
        this.estimate = estimate;
        this.impact = impact;
    }
}