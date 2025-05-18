package vn.com.fecredit.app.entity.config;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.lang.NonNull;

import vn.com.fecredit.app.entity.listener.EntityAuditListener;

/**
 * Minimal entity listener configuration for tests
 */
@Configuration
public class EntityListenerConfig {

    /**
     * Creates a single event publisher for testing that doesn't actually publish events
     */
    @Bean
    @Primary
    public ApplicationEventPublisher testEventPublisher() {
        return new ApplicationEventPublisher() {
            @Override
            public void publishEvent(@NonNull Object event) {
                // Do nothing in tests to avoid dependency on real event handling
            }
        };
    }

    /**
     * Creates a test-friendly audit listener that doesn't depend on external services
     */
    @Bean
    public EntityAuditListener entityAuditListener(ApplicationEventPublisher publisher) {
        EntityAuditListener listener = new EntityAuditListener();
        // Inject the publisher if the setter method exists
        try {
            listener.getClass().getMethod("setEventPublisher", ApplicationEventPublisher.class)
                   .invoke(listener, publisher);
        } catch (Exception e) {
            // Method doesn't exist or invocation failed - that's fine for tests
        }
        return listener;
    }

    /**
     * Creates a simple event multicaster for synchronous event handling in tests
     */
    @Bean(name = AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME)
    public ApplicationEventMulticaster applicationEventMulticaster() {
        return new SimpleApplicationEventMulticaster();
    }
}
