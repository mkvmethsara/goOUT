package com.squadx.goout.Config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;



    // 2. We add the new Security Vault rules
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // Disable CSRF (We don't need it for stateless JWT APIs)
                .csrf(AbstractHttpConfigurer::disable)

                // Enable CORS so React can talk to us without getting blocked by Security
                .cors(Customizer.withDefaults())

                // Set up our Public vs Private routes
                .authorizeHttpRequests(auth -> auth
                        // Allow anyone to register or log in without a token
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // EVERY other endpoint requires a valid JWT Token
                        .anyRequest().authenticated()
                )

                // Tell Spring Security NOT to use memory sessions. We use strict JWT tokens per request.
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Put our custom Bouncer BEFORE the standard Spring Security password filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}