// config/WebMvcConfig.java
package com.cakestore.cakestore.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry reg) {
        reg.addInterceptor(new RecentlyViewedInterceptor());
    }
}
