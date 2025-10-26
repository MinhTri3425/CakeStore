package com.cakestore.cakestore.config;

import com.cloudinary.Cloudinary;
import org.springframework.boot.context.properties.EnableConfigurationProperties; // <<< import thêm
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@EnableConfigurationProperties(CloudinaryProperties.class) // <<< thêm dòng này
public class CloudinaryConfig {
    @Bean
    public Cloudinary cloudinary(CloudinaryProperties p) {
        return new Cloudinary(Map.of(
                "cloud_name", p.getCloudName(),
                "api_key", p.getApiKey(),
                "api_secret", p.getApiSecret(),
                "secure", "true"));
    }
}
