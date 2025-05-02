package redlightBack.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.OctetSequenceKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.*;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Configuration
public class SecurityConfig {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Bean
    public JwtDecoder jwtDecoder() {
        // 대칭 키 생성
        SecretKey key = new OctetSequenceKey.Builder(jwtSecret.getBytes(StandardCharsets.UTF_8))
                .algorithm(JWSAlgorithm.HS256)
                .build()
                .toSecretKey();
        // HS256 검증기 생성
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).build();
        return decoder;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1) MVC 에서 정의한 WebConfig 의 CORS 매핑을 스프링 시큐리티도 허용하도록 활성화
                .cors(Customizer.withDefaults())

                // 2) 필요 없으면 CSRF 끄기
                .csrf(AbstractHttpConfigurer::disable)

                // 3) 기본 폼 로그인/베이직 로그인 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)

                // 4) 엔드포인트별 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/members/me").authenticated()
                        .anyRequest().permitAll()
                )

                // 5) JWT Resource Server 모드
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                );

        return http.build();
    }
}
