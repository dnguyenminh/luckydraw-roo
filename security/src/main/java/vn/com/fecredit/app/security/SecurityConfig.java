package vn.com.fecredit.app.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@Order(1)
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // API endpoints that don't require auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                
                // H2 console access
                .requestMatchers("/h2-console/**").permitAll()
                
                // Static resources
                .requestMatchers("/", "/index.html", "/*.js", "/*.css", "/*.ico", "/assets/**").permitAll()
                
                // Frontend routes - add specific routes
                .requestMatchers("/rewards", "/rewards/**").permitAll()
                .requestMatchers("/dashboard", "/profile", "/settings", "/login", "/register").permitAll()
                .requestMatchers("/about", "/help", "/events", "/participants", "/admin").permitAll()
                
                // Frontend sub-pages
                .requestMatchers("/pages/**", "/views/**").permitAll()
                
                // Any path that doesn't end with a file extension is likely a frontend route
                .requestMatchers("/**").permitAll()
                
                // Protected API endpoints require authentication (uncomment when auth is fully set up)
                // .requestMatchers("/api/**").authenticated()
                
                // During development, allow all requests for easier debugging
                .anyRequest().permitAll()
            );
            
        // Allow frames for H2 console
        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));

        return http.build();
    }
}
