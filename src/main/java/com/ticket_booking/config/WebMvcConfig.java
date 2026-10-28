package com.ticket_booking.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.ticket_booking.utils.ratelimit.RateLimitInterceptor;

public class WebMvcConfig implements WebMvcConfigurer{
    
    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(rateLimitInterceptor).addPathPatterns("/api/**");
    }
}
