package com.worldcup.config;

import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.*;

/**
 * 跨域配置 - 允许前端 Vue 开发服务器 (localhost:5173) 访问后端 API
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}