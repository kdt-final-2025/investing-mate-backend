package redlightBack.stockRecommendation;

import jakarta.persistence.*;
import lombok.*;
import redlightBack.stockRecommendation.dto.Tag;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class StockRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticker", nullable = false, unique = true)
    private String ticker;  //종목코드

    private String name;  //종목이름

    private Double currentPrice;  //현재가

    @Column(name = "high_price_6m")
    private Double highPrice6m;

    @Column(name = "high_price_1y")
    private Double highPrice1y;

    @Column(name = "high_price_2y")
    private Double highPrice2y;

    @Column(name = "high_price_5y")
    private Double highPrice5y;

    private Double dividendYield;  //연배당수익률

    private Double currentToHighRatio;  //저평가율

    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel;  //위험도

    private LocalDateTime updatedAt;

    //"고배당 + 저평가" 태그 생성
    public String generateReason(){
        List<String> reasons = new ArrayList<>();
        if(dividendYield != null && dividendYield > 0.04){
            reasons.add(Tag.고배당.toString());
        }
        if(currentToHighRatio < 0.85){
            reasons.add(Tag.저평가.toString());
        }
        return String.join("+", reasons);
    }

    //챗봇에 넘길 문장 생성
    public String generateDetail(){
        List<String> details = new ArrayList<>();

        if(dividendYield != null){
            String formattedYield = String.format("배당률 %.2f%%", dividendYield);
            details.add(formattedYield);
        }
        if(currentToHighRatio < 1.0){
            double dropPercent = (1.0 - currentToHighRatio) * 100;
            String formattedDrop = String.format("고점 대비 -%.0f%%", dropPercent);
            details.add(formattedDrop);
        }
        return String.join(", ", details);
    }
}
