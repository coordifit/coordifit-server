package com.miracle.coordifit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins:}") // 콤마로 구분
    private String allowedOriginCsv;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = org.springframework.util.StringUtils.hasText(allowedOriginCsv)
                ? org.springframework.util.StringUtils.commaDelimitedListToStringArray(allowedOriginCsv)
                : new String[0]; // 비어 있으면 전부 거부

        registry.addMapping("/**")                 // 필요 시 "/api/**"로 좁히기
                .allowedOrigins(origins)           // 정확 매칭만 허용
                .allowedMethods("*")               // 필요 메서드로 좁히면 더 안전
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
