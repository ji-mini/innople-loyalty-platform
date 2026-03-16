package com.innople.loyalty.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
public class WebCorsConfig implements WebMvcConfigurer {

    private final List<String> allowedOrigins;
    private final boolean allowCredentials;

    public WebCorsConfig(
            @Value("${app.cors.allowed-origins:" +
                    "http://localhost:3200,http://127.0.0.1:3200," +
                    "http://localhost:5173,http://127.0.0.1:5173," +
                    "http://localhost:8090,http://127.0.0.1:8090," +
                    "http://52.79.229.236:8090" +
                    "}") String allowedOrigins,
            @Value("${app.cors.allow-credentials:false}") boolean allowCredentials
    ) {
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        this.allowCredentials = allowCredentials;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins.toArray(String[]::new))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "Authorization", "X-Tenant-Id")
                .exposedHeaders("X-Tenant-Id")
                .maxAge(3600)
                .allowCredentials(allowCredentials);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(false);
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Content-Type", "Authorization", "X-Tenant-Id"));
        config.setExposedHeaders(List.of("X-Tenant-Id"));
        config.setAllowCredentials(allowCredentials);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}

