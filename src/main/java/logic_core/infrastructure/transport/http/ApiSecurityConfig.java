package logic_core.infrastructure.transport.http;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Minimal security wiring for the HTTP transport.
 *
 * <p>Authentication for the API is handled application-layer (the request payload
 * carries {@code sessionToken}, validated per request by the existing auth use
 * cases), so the servlet layer only needs to let the JSON transport through.
 * CSRF is disabled because the transport is stateless JSON with no cookie-based
 * session. All other (non-API) servlet paths keep the default authenticated posture.
 */
@Configuration
@EnableWebSecurity
public class ApiSecurityConfig
{
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception
    {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api").permitAll()
                        .anyRequest().authenticated());

        return http.build();
    }
}