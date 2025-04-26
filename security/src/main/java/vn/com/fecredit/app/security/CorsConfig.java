package vn.com.fecredit.app.security;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import lombok.extern.slf4j.Slf4j;

/**
 * CORS configuration with highest precedence to ensure it overrides any other CORS settings.
 * This explicitly allows HEAD requests which are needed for the export polling mechanism.
 */
@Configuration
@Slf4j
public class CorsConfig {
    
    @Bean
    @Primary
    @Order(Ordered.HIGHEST_PRECEDENCE) // Give it highest precedence to override other CORS configs
    public CorsFilter corsFilter() {
        log.info("Initializing CORS filter with explicit HEAD request support");
        
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(false);
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        
        // Explicitly set all allowed methods including HEAD
        config.setAllowedMethods(Arrays.asList(
            HttpMethod.GET.name(),
            HttpMethod.POST.name(),
            HttpMethod.PUT.name(), 
            HttpMethod.DELETE.name(),
            HttpMethod.HEAD.name(),
            HttpMethod.OPTIONS.name()
        ));
        
        // Set a long cache time for preflight requests
        config.setMaxAge(3600L);
        
        log.info("CORS configuration created with allowed methods: {}", config.getAllowedMethods());
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);  // Specifically target API paths
        source.registerCorsConfiguration("/**", config);      // Also apply globally as fallback
        
        return new CorsFilter(source);
    }
}
