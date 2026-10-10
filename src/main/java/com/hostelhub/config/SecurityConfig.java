package com.hostelhub.config;

import com.hostelhub.security.CustomAccessDeniedHandler;
import com.hostelhub.security.JwtAuthEntryPoint;
import com.hostelhub.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthEntryPoint unauthorizedHandler;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthEntryPoint unauthorizedHandler,
                          CustomAccessDeniedHandler accessDeniedHandler,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.unauthorizedHandler = unauthorizedHandler;
        this.accessDeniedHandler = accessDeniedHandler;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(unauthorizedHandler)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/health").permitAll()
                        
                        // Complaint Role-based endpoints
                        .requestMatchers(HttpMethod.PATCH, "/api/complaints/*/assign/*").hasAnyRole("WARDEN", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/complaints/*/priority").hasAnyRole("WARDEN", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/complaints/*/status").hasAnyRole("WARDEN", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/complaints/**").hasAnyRole("WARDEN", "ADMIN")
                        .requestMatchers("/api/complaints/**").hasAnyRole("STUDENT", "WARDEN", "ADMIN")

                        // Group & Membership management endpoints
                        .requestMatchers(HttpMethod.POST, "/api/groups/*/members/*").hasAnyRole("WARDEN", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/groups/*/members/*").hasAnyRole("WARDEN", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/groups/**").hasAnyRole("WARDEN", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/groups/**").hasAnyRole("WARDEN", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/groups/**").hasAnyRole("WARDEN", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/groups/**").hasAnyRole("STUDENT", "WARDEN", "ADMIN")
                        
                        // User Group endpoints
                        .requestMatchers("/api/users/**").hasAnyRole("STUDENT", "WARDEN", "ADMIN")
                        
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
