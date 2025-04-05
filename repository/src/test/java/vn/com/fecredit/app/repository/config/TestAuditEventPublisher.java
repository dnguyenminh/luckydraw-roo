package vn.com.fecredit.app.repository.config;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * Test implementation of ApplicationEventPublisher that doesn't throw exceptions
 * when audit events are published during tests.
 */
@Component // Remove @Primary annotation to avoid conflicts
@Slf4j
public class TestAuditEventPublisher implements ApplicationEventPublisher {

    @Override
    public void publishEvent(@NonNull Object event) {
        try {
            // Log but don't actually publish to avoid exceptions during tests
            log.debug("Test publishing event: {}", event.getClass().getSimpleName());
        } catch (Exception e) {
            log.warn("Error in test event publisher: {}", e.getMessage());
        }
    }
}
