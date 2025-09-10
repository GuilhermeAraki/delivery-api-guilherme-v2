package com.deliverytech.delivery.security;

import com.auth0.spring.security.api.JwtWebSecurityConfigurer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${auth0.audience}")
    private String audience;

    @Value("${auth0.domain}")
    private String domain;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        JwtWebSecurityConfigurer
            .forDomain(domain)
            .withAudience(audience)
            .configure(http)
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                // Clientes
                .requestMatchers(HttpMethod.GET, "/api/clientes/**").hasAuthority("SCOPE_read:clientes")
                .requestMatchers(HttpMethod.POST, "/api/clientes").hasAuthority("SCOPE_write:clientes")
                .requestMatchers(HttpMethod.PUT, "/api/clientes/**").hasAuthority("SCOPE_write:clientes")
                .requestMatchers(HttpMethod.DELETE, "/api/clientes/**").hasAuthority("SCOPE_delete:clientes")
                // Restaurantes
                .requestMatchers(HttpMethod.GET, "/api/restaurantes/**").hasAuthority("SCOPE_read:restaurantes")
                .requestMatchers(HttpMethod.POST, "/api/restaurantes").hasAuthority("SCOPE_write:restaurantes")
                .requestMatchers(HttpMethod.PUT, "/api/restaurantes/**").hasAuthority("SCOPE_write:restaurantes")
                .requestMatchers(HttpMethod.DELETE, "/api/restaurantes/**").hasAuthority("SCOPE_delete:restaurantes")
                // Produtos
                .requestMatchers(HttpMethod.GET, "/api/produtos/**").hasAuthority("SCOPE_read:produtos")
                .requestMatchers(HttpMethod.POST, "/api/produtos").hasAuthority("SCOPE_write:produtos")
                .requestMatchers(HttpMethod.PUT, "/api/produtos/**").hasAuthority("SCOPE_write:produtos")
                .requestMatchers(HttpMethod.DELETE, "/api/produtos/**").hasAuthority("SCOPE_delete:produtos")
                // Pedidos
                .requestMatchers(HttpMethod.GET, "/api/pedidos/**").hasAuthority("SCOPE_read:pedidos")
                .requestMatchers(HttpMethod.POST, "/api/pedidos").hasAuthority("SCOPE_write:pedidos")
                .requestMatchers(HttpMethod.PUT, "/api/pedidos/**").hasAuthority("SCOPE_write:pedidos")
                .requestMatchers(HttpMethod.DELETE, "/api/pedidos/**").hasAuthority("SCOPE_delete:pedidos")
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions().sameOrigin());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;  
    }   
}