package vn.com.fecredit.app.repository.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PreRemove;
import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

/**
 * Test implementation of EntityAuditListener that doesn't depend on event publishing
 */
@Component
@Slf4j
public class TestEntityAuditListener {

    // private ApplicationEventPublisher eventPublisher;

    /**
     * Setter method to allow for dependency injection
     * We use setter injection here to avoid circular dependencies during context startup
     */
    @Autowired(required = false) // Make this optional to avoid startup issues
    public void setEventPublisher(ApplicationEventPublisher eventPublisher) {
        // this.eventPublisher = eventPublisher;
        
        // Try to update the real listener if available
        try {
            Class<?> listenerClass = Class.forName("vn.com.fecredit.app.entity.listener.EntityAuditListener");
            Object instance = listenerClass.getDeclaredConstructor().newInstance();
            
            java.lang.reflect.Method setterMethod = listenerClass.getDeclaredMethod("setEventPublisher", ApplicationEventPublisher.class);
            setterMethod.setAccessible(true);
            setterMethod.invoke(instance, eventPublisher);
            
            log.info("Updated EntityAuditListener eventPublisher for tests");
        } catch (Exception e) {
            log.debug("Could not update EntityAuditListener: {}", e.getMessage());
        }
    }

    @PostPersist
    public void postPersist(Object entity) {
        if (entity instanceof AbstractStatusAwareEntity) {
            log.debug("Test audit listener: Entity created: {}", entity.getClass().getSimpleName());
        }
    }

    @PostUpdate
    public void postUpdate(Object entity) {
        if (entity instanceof AbstractStatusAwareEntity) {
            log.debug("Test audit listener: Entity updated: {}", entity.getClass().getSimpleName());
        }
    }

    @PreRemove
    public void preRemove(Object entity) {
        if (entity instanceof AbstractStatusAwareEntity) {
            log.debug("Test audit listener: Entity deleted: {}", entity.getClass().getSimpleName());
        }
    }
}
