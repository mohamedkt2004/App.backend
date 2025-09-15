// src/main/java/com/example/wirelength/config/CorsConfig.java
package com.example.wirelength.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // Permettre les credentials
        config.setAllowCredentials(true);
        
        // Permettre toutes les origines (pour le développement)
        config.addAllowedOriginPattern("*");
        
        // Permettre tous les headers
        config.addAllowedHeader("*");
        
        // Permettre toutes les méthodes HTTP
        config.addAllowedMethod("*");
        
        source.registerCorsConfiguration("/api/**", config);
        return new CorsFilter(source);
    }
}