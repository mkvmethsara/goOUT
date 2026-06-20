package com.squadx.goout.Config;

import com.squadx.goout.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UserRepository userRepository;

    // 1. We teach Spring Security how to find users in your MongoDB database
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            com.squadx.goout.Entity.User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found in MongoDB"));

            // Convert your MongoDB User into a standard Spring Security User
            return User.builder()
                    .username(user.getEmail())
                    .password(user.getPassword())
                    .roles("USER")
                    .build();
        };
    }

    // 2. We set up the Authentication Provider (The engine that actually checks passwords)
    @Bean
    public AuthenticationProvider authenticationProvider() {
        // Modern Spring Security requires the UserDetailsService to be passed in the constructor
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // 3. We provide the Authentication Manager for your AuthController to use later
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        // 'throws Exception' removed as it is no longer required in modern Spring Security
        return config.getAuthenticationManager();
    }

    // 4. We moved the Password Encoder here to break the circular dependency!
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}