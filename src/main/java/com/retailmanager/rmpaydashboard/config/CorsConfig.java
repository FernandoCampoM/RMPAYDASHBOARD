package com.retailmanager.rmpaydashboard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
/**
 * @author: Juan Fernando Campo Mosquera juancamm@unicauca.edu.co
 * Ésta clase realiza la configuración CORS para la aplicación
 */
@Configuration
public class CorsConfig {
    /**
     * Configuración de los dominios permitidos para consumir el servicio
     * @return
     */
    @Bean
    public WebMvcConfigurer corsConfigurer(){
        return new WebMvcConfigurer() {
            /**
             * Adds CORS mappings to the provided CorsRegistry.
             *
             * @param  registry   the CorsRegistry to add the mappings to
             */
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                // method implementation
                registry.addMapping("/login**")
                .allowedOrigins("http://localhost:4200","https://rmpay.retailmanagerpr.com")
                .allowedMethods("*")
                .exposedHeaders("*");
                
                registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:4200","https://rmpay.retailmanagerpr.com")
                .allowedMethods("*");
            }
        };
    }
}
