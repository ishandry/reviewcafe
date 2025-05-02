package lnu.ishchuk.reviewcafe;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@TestConfiguration
@EnableMethodSecurity
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );
        return http.build();
    }

    @Primary
    @Bean
    public JwtDecoder jwtDecoder() {
        return token -> {
            Map<String, Object> realmAccess = Map.of("roles", List.of("role-user"));
            Map<String, Object> claims = new HashMap<>();
            claims.put("realm_access", realmAccess);
            claims.put("preferred_username", "testuser");
            return Jwt.withTokenValue(token)
                    .header("alg", "none")
                    .claims(c -> c.putAll(claims))
                    .build();
        };
    }
}
