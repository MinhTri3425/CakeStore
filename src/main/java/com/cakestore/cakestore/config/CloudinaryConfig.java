package com.cakestore.cakestore.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value; // Import mới
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class CloudinaryConfig {

    // Inject giá trị từ application.properties
    @Value("${cloudinary.cloud-name}") 
    private String cloudName;

    @Value("${cloudinary.api-key}") 
    private String apiKey;

    @Value("${cloudinary.api-secret}") 
    private String apiSecret;


    @Bean
    public Cloudinary cloudinary() {
        // Sử dụng giá trị được Inject thay vì hardcode
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName, 
                "api_key", apiKey,       
                "api_secret", apiSecret, 
                "secure", true
        ));
    }
}