package com.quannhabaninh.config;

import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

// @Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        // Explicitly allow frontend origins
        config.addAllowedOrigin("http://localhost:5173"); // Vite dev server
        config.addAllowedOrigin("http://localhost:3000"); // React dev server
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "X-Requested-With", "Cache-Control", "Origin"));
        config.addAllowedMethod("*");
        // Expose headers for JWT
        config.addExposedHeader("Authorization");
        config.addExposedHeader("Content-Type");
        config.setMaxAge(3600L); // Cache preflight response for 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
