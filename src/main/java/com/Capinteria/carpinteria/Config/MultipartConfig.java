package com.Capinteria.carpinteria.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

/** Resolver de multipart para la carga de imágenes. (CORS se configura en SecurityConfig.) */
@Configuration
public class MultipartConfig {

    @Bean
    public StandardServletMultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }
}
