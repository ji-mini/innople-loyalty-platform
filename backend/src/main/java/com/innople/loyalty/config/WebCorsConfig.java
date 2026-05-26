package com.innople.loyalty.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
public class WebCorsConfig implements WebMvcConfigurer {

    private final ApiAuditLogInterceptor apiAuditLogInterceptor;
    private final String allowedOrigins;
    private final boolean allowCredentials;

    public WebCorsConfig(
            ApiAuditLogInterceptor apiAuditLogInterceptor,
            @Value("${app.cors.allowed-origins:http://localhost:8081,http://127.0.0.1:8081,http://localhost:8090}") String allowedOrigins,
            @Value("${app.cors.allow-credentials:false}") boolean allowCredentials
    ) {
        this.apiAuditLogInterceptor = apiAuditLogInterceptor;
        this.allowedOrigins = allowedOrigins;
        this.allowCredentials = allowCredentials;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiAuditLogInterceptor)
                .addPathPatterns("/api/v1/**")
                .excludePathPatterns("/api/v1/public/**", "/error");
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of(
                "Content-Type",
                "Authorization",
                "X-Tenant-Id",
                "x-tenant-id",
                "X-Client-Device",
                "X-Client-OS"
        ));
        config.setExposedHeaders(List.of("X-Tenant-Id", "X-Admin-User-Id"));
        config.setAllowCredentials(allowCredentials);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}

