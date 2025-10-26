package com.cakestore.cakestore.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class CloudinaryConfig {


    @Bean
    public Cloudinary cloudinary() {
        // Lấy thông tin cấu hình từ Environment hoặc application.properties
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "YOUR_CLOUD_NAME", 
                "api_key", "YOUR_API_KEY",       
                "api_secret", "YOUR_API_SECRET", 
                "secure", true
        ));
    }
}