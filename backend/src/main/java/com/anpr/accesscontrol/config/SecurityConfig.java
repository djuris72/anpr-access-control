package com.anpr.accesscontrol.config;

import com.anpr.accesscontrol.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * - /api/auth/**    -> otvoreno (tu se prijavljuje admin)
 * - /api/access/**  -> otvoreno (ovo poziva "kamera" na kapiji, ne osoba
 *                      koja se prijavljuje - nema smisla da kamera ima login)
 * - /api/vehicles/** -> zahteva vazeci JWT token (samo ulogovan admin)
 *
 * Sesije su iskljucene (STATELESS) jer je JWT sam po sebi dovoljan dokaz
 * identiteta na svaki zahtev - server ne treba da pamti nista izmedju poziva.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // nije potrebno za stateless REST API sa JWT-om
                .cors(cors -> {}) // koristi CorsConfigurationSource bean iz CorsConfig.java
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/access/**").permitAll()
                        .requestMatchers("/api/vehicles/**").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}