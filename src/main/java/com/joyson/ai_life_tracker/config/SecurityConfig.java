package com.joyson.ai_life_tracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class SecurityConfig {

    // 🔐 Password encoder (keep this)
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 🔥 Security configuration
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // ❌ Disable CSRF (needed for frontend requests)
            .csrf(csrf -> csrf.disable())

            // 🔥 Allow ALL requests (important for now)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/health").permitAll()   // health check
                .requestMatchers("/api/**").permitAll()   // all APIs
                .anyRequest().permitAll()                 // 🔥 FIXED HERE
            )

            // ❌ Disable login popup
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable());

        return http.build();
    }
}
