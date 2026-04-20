package com.joyson.ai_life_tracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // ─────────────────────────────────────────────
    // 🔐 BCrypt password encoder
    // Cost factor 10 (default) → ~80–120ms per hash
    // Do NOT increase cost — it will slow login further
    // ─────────────────────────────────────────────
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    // ─────────────────────────────────────────────
    // 🔥 Security filter chain
    //
    // Key performance decisions:
    //   - STATELESS session: no session creation overhead per request
    //   - All /api/** permitted: no auth filter overhead on API routes
    //   - formLogin + httpBasic disabled: removes default filter chains
    //     that add latency to every request
    // ─────────────────────────────────────────────
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // ❌ CSRF disabled — React sends JSON, not form POSTs with CSRF tokens
            .csrf(csrf -> csrf.disable())

            // ✅ STATELESS — eliminates session creation/lookup per request (~10–30ms saved)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // ✅ Permit all API routes — no token/session check overhead
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/**").permitAll()
                .requestMatchers("/health", "/actuator/health").permitAll()
                .anyRequest().permitAll()
            )

            // ❌ Disable default login page — removes UsernamePasswordAuthenticationFilter
            .formLogin(form -> form.disable())

            // ❌ Disable HTTP Basic — removes BasicAuthenticationFilter
            .httpBasic(basic -> basic.disable());

        return http.build();
    }
}
