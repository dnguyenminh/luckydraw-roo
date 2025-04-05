package vn.com.fecredit.app.entity.listener;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PreRemove;
import lombok.extern.slf4j.Slf4j;

/**
 * Mock implementation of entity audit listener for tests
 * This replaces the real entity listener to avoid issues with Spring context
 */
@Slf4j
public class MockEntityAuditListener {
    
    @PostPersist
    public void postPersist(Object entity) {
        log.debug("Mock audit: Entity created: {}", entity.getClass().getSimpleName());
    }
    
    @PostUpdate
    public void postUpdate(Object entity) {
        log.debug("Mock audit: Entity updated: {}", entity.getClass().getSimpleName());
    }
    
    @PreRemove
    public void preRemove(Object entity) {
        log.debug("Mock audit: Entity deleted: {}", entity.getClass().getSimpleName());
    }
}
