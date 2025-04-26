package redlightBack.config;

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
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // (1) CORS 활성화
                .cors(Customizer -> {})

                // (2) CSRF 끄기
                .csrf(AbstractHttpConfigurer::disable)

                // (3) 기본 인증/Form 로그인 끄기
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)

                // (4) 인가 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/members/me").authenticated()
                        .anyRequest().permitAll()
                )

                // (5) JWT Resource Server 설정
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                );

        return http.build();
    }
}
