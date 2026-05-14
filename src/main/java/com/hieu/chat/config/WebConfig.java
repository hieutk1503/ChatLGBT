package com.hieu.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Cho phép trình duyệt truy cập thẳng vào các file trong thư mục "uploads"
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}