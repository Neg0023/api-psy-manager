package com.psymanager.config;

import com.psymanager.security.AdminAuthoritiesConverter;
import com.psymanager.security.GoogleIdTokenValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security como OAuth2 Resource Server validando o ID Token do Google.
 *
 * <p>Fluxo de validação:
 * <ol>
 *   <li>Assinatura + issuer + expiração: decoder padrão (JWKS do Google).</li>
 *   <li>Audiência + e-mail verificado: {@link GoogleIdTokenValidator} (401 se falhar).</li>
 *   <li>E-mail == ADMIN_EMAIL: {@link AdminAuthoritiesConverter} -> ROLE_ADMIN (403 se não for).</li>
 * </ol>
 */
@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            JwtAuthenticationConverter jwtAuthenticationConverter,
                                            CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Callback do OAuth do Google: navegação do navegador, sem Bearer.
                        // A segurança é garantida validando o e-mail da conta no handleCallback.
                        .requestMatchers("/api/integrations/google/callback").permitAll()
                        .anyRequest().hasRole("ADMIN"))
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));
        return http.build();
    }

    /**
     * Decoder do ID Token: valida assinatura/issuer/expiração e ainda audiência
     * e e-mail verificado (via {@link GoogleIdTokenValidator}).
     */
    @Bean
    JwtDecoder jwtDecoder(AppProperties props,
                          @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri) {
        NimbusJwtDecoder decoder = (NimbusJwtDecoder) JwtDecoders.fromIssuerLocation(issuerUri);
        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(issuerUri),
                new GoogleIdTokenValidator(props.google().clientId()));
        decoder.setJwtValidator(validator);
        return decoder;
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter(AppProperties props) {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new AdminAuthoritiesConverter(props.adminEmail()));
        return converter;
    }

    /**
     * Fonte única de configuração de CORS, injetada no filter chain do Spring Security.
     * Origens vêm de {@code app.cors.allowed-origins} (env {@code APP_CORS_ALLOWED_ORIGINS}).
     * Sem curinga em origens porque a SPA envia credenciais (header {@code Authorization}).
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource(AppProperties props) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(props.cors().allowedOrigins());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
