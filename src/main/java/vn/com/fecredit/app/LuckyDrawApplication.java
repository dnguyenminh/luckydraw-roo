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
             * This forwards non-API paths to the index.html to allow client-side routing
             */
            @Override
            public void addViewControllers(@NonNull ViewControllerRegistry registry) {
                // Forward root requests to index.html
                registry.addViewController("/").setViewName("forward:/index.html");
                
                // Forward single-level paths to index.html (except for paths that would match other patterns)
                registry.addViewController("/{path:[^\\.]*}").setViewName("forward:/index.html");
                
                // Forward paths with /pages/ prefix (common in SPAs)
                registry.addViewController("/pages/{*page}").setViewName("forward:/index.html");
                
                // Forward paths with /views/ prefix (common in SPAs)
                registry.addViewController("/views/{*view}").setViewName("forward:/index.html");
                
                // Simple path mappings that don't use regex or ** patterns
                registry.addViewController("/dashboard").setViewName("forward:/index.html");
                registry.addViewController("/profile").setViewName("forward:/index.html");
                registry.addViewController("/settings").setViewName("forward:/index.html");
                registry.addViewController("/login").setViewName("forward:/index.html");
                registry.addViewController("/register").setViewName("forward:/index.html");
                registry.addViewController("/about").setViewName("forward:/index.html");
                registry.addViewController("/help").setViewName("forward:/index.html");
                
                // NO CATCH-ALL PATTERNS WITH ** - they're causing the errors
            }
        };
    }
}