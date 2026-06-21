package gov.procure.shared.config;

import java.util.Collection;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Stateless OAuth2 Resource Server config. Validates Keycloak JWTs (issuer configured in
 * application.yml) and maps the {@code roles} claim to Spring authorities used by {@code @PreAuthorize}
 * — note: authorities are the raw permission strings ({@code procurement:requisition:create}),
 * not {@code ROLE_}-prefixed. Method security enables fine-grained endpoint guards.
 */
@Configuration
@EnableMethodSecurity
@Profile("!mock") // replaced by MockSecurityConfig (permit-all) in the runnable mock profile
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // stateless API; browser flows use a gateway/CSRF token
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth -> oauth.jwt(jwt ->
                jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(rolesConverter());
        return converter;
    }

    @SuppressWarnings("unchecked")
    private Converter<Jwt, Collection<GrantedAuthority>> rolesConverter() {
        return jwt -> {
            Object roles = jwt.getClaims().getOrDefault("roles", List.of());
            if (roles instanceof Collection<?> c) {
                return c.stream()
                    .map(Object::toString)
                    .map(SimpleGrantedAuthority::new)
                    .map(GrantedAuthority.class::cast)
                    .toList();
            }
            return List.of();
        };
    }
}
