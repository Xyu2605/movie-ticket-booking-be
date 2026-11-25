package com.ticket_booking.config;

import org.springframework.context.annotation.Configuration;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

@Configuration
public class MinioConfig {

    @Value("${app.minio.url}")
    private String url;

    @Value("${app.minio.public-url}")
    private String minioPublicUrl;

    @Value("${app.minio.access-key}")
    private String accessKey;
    
    @Value("${app.minio.secret-key}")
    private String secretKey;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .build();
    }
}
