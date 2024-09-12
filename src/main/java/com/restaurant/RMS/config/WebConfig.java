package com.restaurant.RMS.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Api Path Mapping
                .allowedOrigins("http://localhost:3000", "https://saiman-gericht-restaurant.vercel.app") // accept cross-origin requests from http://localhost:3000 for endpoints under /api/**
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Allowed methods for the client
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}

// This is useful in a development environment where the frontend (running on localhost:3000)
// and backend (running on a different port) need to communicate with each other.