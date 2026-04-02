package com.joyson.ai_life_tracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // ❌ Disable CSRF (for React frontend)
            .csrf(csrf -> csrf.disable())

            // 🔥 Allow API access
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/**").permitAll() // ✅ simpler & safer
                .anyRequest().authenticated()
            )

            // ❌ Disable default login popup (IMPORTANT)
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable());

        return http.build();
    }
}