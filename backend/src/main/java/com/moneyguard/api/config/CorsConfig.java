package com.moneyguard.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter(AppProperties props) {
        CorsConfiguration config = new CorsConfiguration();
        List<String> origins = List.of(props.getCors().getAllowedOrigins().split(","));
        if (origins.stream().anyMatch(o -> o.trim().equals("*"))) {
            config.setAllowedOriginPatterns(List.of("*"));
        } else {
            config.setAllowedOrigins(origins.stream().map(String::trim).filter(s -> !s.isBlank()).toList());
        }
        config.setAllowedMethods(Arrays.asList("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization","Content-Type","Idempotency-Key","X-Device-Id"));
        config.setExposedHeaders(Arrays.asList("X-Request-Id", "X-SecurePay-Mode"));
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
