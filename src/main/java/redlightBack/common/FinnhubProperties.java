package redlightBack.common;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter @Setter
@Component
@ConfigurationProperties(prefix = "finnhub.api")
public class FinnhubProperties {
    private String baseUrl;
    private String key;
}
