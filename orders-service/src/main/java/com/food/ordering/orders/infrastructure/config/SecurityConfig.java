package com.food.ordering.orders.infrastructure.config;

import com.food.ordering.common.security.JwtAuthConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/orders").hasAnyRole("CUSTOMER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/orders/**").hasAnyRole("CUSTOMER", "RESTAURANT_OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/orders/*/status").hasAnyRole("RESTAURANT_OWNER", "ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter())));

        return http.build();
    }

    @Bean
    public JwtAuthConverter jwtAuthConverter() {
        return new JwtAuthConverter("orders-service");
    }
}
