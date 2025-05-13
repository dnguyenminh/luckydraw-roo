package vn.com.fecredit.app.controller.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;

/**
 * Security configuration for tests to avoid dependency on the real security module.
 */
@Configuration
@EnableWebSecurity
@ActiveProfiles("test")
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeHttpRequests(authz -> 
                authz.anyRequest().permitAll()
            )
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Primary
    public UserDetailsService userDetailsService() {
        var userDetailsManager = new InMemoryUserDetailsManager();
        userDetailsManager.createUser(
            new User(
                "admin",
                passwordEncoder().encode("password"),
                Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN"))
            )
        );
        userDetailsManager.createUser(
            new User(
                "user",
                passwordEncoder().encode("password"),
                Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"))
            )
        );
        return userDetailsManager;
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authManagerBuilder.userDetailsService(userDetailsService()).passwordEncoder(passwordEncoder());
        return authManagerBuilder.build();
    }
    
    @Bean
    public MockJwtTokenProvider mockJwtTokenProvider() {
        return new MockJwtTokenProvider();
    }
}
