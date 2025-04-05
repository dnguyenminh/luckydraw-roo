package vn.com.fecredit.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;

/**
 * Main Spring Boot Application class for LuckyDraw.
 * This serves both the REST API and the compiled frontend assets.
 */
@SpringBootApplication
@EnableJpaAuditing
public class LuckyDrawApplication {

    public static void main(String[] args) {
        SpringApplication.run(LuckyDrawApplication.class, args);
    }
    
    /**
     * Web MVC configuration to handle static resources, CORS, and SPA routing.
     */
    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            /**
             * Configure CORS settings to allow frontend development server to access API
             */
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("http://localhost:3000", "http://localhost:8080")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
            
            /**
             * Configure static resource handling for the compiled frontend
             */
            @Override
            public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
                // Serve static resources from the frontend/dist folder
                registry.addResourceHandler("/**")
                        .addResourceLocations("classpath:/static/")
                        .resourceChain(true);
            }
            
            /**
             * Configure routing to support SPA navigation
             * This forwards non-API, non-resource paths to the index.html
             */
            @Override
            public void addViewControllers(@NonNull ViewControllerRegistry registry) {
                // Forward requests to index.html for SPA routing
                registry.addViewController("/").setViewName("forward:/index.html");
                registry.addViewController("/{x:[\\w\\-]+}").setViewName("forward:/index.html");
                registry.addViewController("/{x:^(?!api$).*$}/**/{y:[\\w\\-]+}").setViewName("forward:/index.html");
            }
        };
    }
}