package com.segi.campusassistance.config;

import com.segi.campusassistance.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Autowired
    private CorsConfigurationSource corsConfigurationSource;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/uploads/**").permitAll()
                .requestMatchers("/api/notifications/**").permitAll()
                .requestMatchers("/api/marketplace/items").permitAll() // GET 请求允许未登录访问
                .requestMatchers("/api/marketplace/items/search").permitAll() // 搜索允许未登录访问
                .requestMatchers("/api/marketplace/items/{itemId}").permitAll() // GET 详情允许未登录访问
                .requestMatchers("/items").permitAll() // GET 请求允许未登录访问（Lost and Found）
                .requestMatchers("/items/{id}").permitAll() // GET 详情允许未登录访问
                .requestMatchers("/api/items").permitAll() // GET 请求允许未登录访问（Item）
                .requestMatchers("/api/items/{id}").permitAll() // GET 详情允许未登录访问
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}

















