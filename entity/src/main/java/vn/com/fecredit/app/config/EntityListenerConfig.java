package vn.com.fecredit.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;

import vn.com.fecredit.app.entity.listener.EntityAuditListener;

/**
 * Configuration for entity listeners including auditing
 */
@Configuration
@EnableSpringConfigured
public class EntityListenerConfig {

    /**
     * Create the entity audit listener as a Spring bean
     * @return EntityAuditListener instance
     */
    @Bean
    public EntityAuditListener entityAuditListener() {
        return new EntityAuditListener();
    }
}
