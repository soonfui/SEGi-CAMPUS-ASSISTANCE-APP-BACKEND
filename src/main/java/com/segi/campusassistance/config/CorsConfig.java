package com.segi.campusassistance.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    
    @Value("${cors.allowed-origins}")
    private String allowedOrigins;
    
    @Value("${cors.allowed-methods}")
    private String allowedMethods;
    
    @Value("${cors.allowed-headers}")
    private String allowedHeaders;
    
    @Value("${cors.allow-credentials}")
    private boolean allowCredentials;
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 当 allowCredentials 为 true 时，不能使用 "*" 作为 allowedHeaders
        String[] headers;
        if (allowedHeaders.equals("*") && allowCredentials) {
            headers = new String[]{
                "Authorization", "Content-Type", "X-Requested-With", 
                "Accept", "Origin", "Access-Control-Request-Method", 
                "Access-Control-Request-Headers"
            };
        } else if (allowedHeaders.equals("*")) {
            headers = new String[]{"*"};
        } else {
            headers = allowedHeaders.split(",");
        }
        
        registry.addMapping("/api/**")
                .allowedOriginPatterns(allowedOrigins.split(","))
                .allowedMethods(allowedMethods.split(","))
                .allowedHeaders(headers)
                .allowCredentials(allowCredentials)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/")
                .setCachePeriod(3600);
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 添加 Flutter localhost 端口
        String[] origins = allowedOrigins.split(",");
        java.util.List<String> originList = new java.util.ArrayList<>(Arrays.asList(origins));
        if (!originList.contains("http://localhost:5173")) {
            originList.add("http://localhost:5173");
        }
        if (!originList.contains("http://localhost:8080")) {
            originList.add("http://localhost:8080");
        }
        configuration.setAllowedOriginPatterns(originList);
        configuration.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
        // 当 allowCredentials 为 true 时，不能使用 "*" 作为 allowedHeaders
        if (allowedHeaders.equals("*") && allowCredentials) {
            configuration.setAllowedHeaders(Arrays.asList(
                "Authorization", "Content-Type", "X-Requested-With", 
                "Accept", "Origin", "Access-Control-Request-Method", 
                "Access-Control-Request-Headers"
            ));
        } else {
        configuration.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
        }
        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
