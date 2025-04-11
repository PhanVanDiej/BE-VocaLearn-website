package com.TestFlashCard.FlashCard.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.TestFlashCard.FlashCard.security.JwtTokenFilter;

@Configuration
@EnableWebSecurity
public class securityConfig {

    private final JwtTokenFilter jwtTokenFilter;

    public securityConfig(JwtTokenFilter jwtTokenFilter) {
        this.jwtTokenFilter = jwtTokenFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
<<<<<<< Updated upstream
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/user/login").permitAll() // Cho phép truy cập /login mà không cần xác thực
                        .requestMatchers("/api/user/register").permitAll()
                        .anyRequest().authenticated() // Các request khác cần xác thực
                )
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
=======
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers("/api/user/login").permitAll()
                    .requestMatchers("/api/user").permitAll()
                    .requestMatchers("/api/user/register").permitAll()
                    .requestMatchers("/api/user/create").permitAll()
                    .requestMatchers("/api/user/update").hasRole("ADMIN")
                    .anyRequest().authenticated()
            )
            .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);
    
    return http.build();
>>>>>>> Stashed changes
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}