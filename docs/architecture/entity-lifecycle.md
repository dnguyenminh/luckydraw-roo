---
layout: diagram
title: Entity Lifecycle Management
description: State transitions, validation hooks, and event processing
diagram_url: /generated/entity-lifecycle.png
previous_diagram: /architecture/entity-validation
next_diagram: /architecture/entity-exceptions
---

[← Back to Index](../index.md)

# Entity Lifecycle Management

## State Machine

### Available States

```java
public enum EntityStatus {
    DRAFT,      // Initial state when created
    ACTIVE,     // Normal operational state
    INACTIVE,   // Temporarily disabled
    DELETED,    // Soft deleted (hidden)
    ARCHIVED    // Historical record
}
```

### State Transitions

Valid state transitions:

| From     | To                          |
|----------|----------------------------|
| DRAFT    | ACTIVE, DELETED           |
| ACTIVE   | INACTIVE, ARCHIVED, DELETED|
| INACTIVE | ACTIVE, DELETED           |
| DELETED  | None (terminal state)     |
| ARCHIVED | None (terminal state)     |

## Lifecycle Hooks

### 1. Creation Hooks

```java
@MappedSuperclass
public abstract class AbstractEntity<ID> {
    @PrePersist
    protected void onPrePersist() {
        if (status == null) {
            status = EntityStatus.DRAFT;
        }
        validateState();
    }
    
    @PostPersist
    protected void onPostPersist() {
        publishEvent(new EntityCreatedEvent(this));
    }
}
```

### 2. Update Hooks

```java
@MappedSuperclass
public abstract class AbstractAuditableEntity<ID> {
    @PreUpdate
    protected void onPreUpdate() {
        validateState();
        updateLastModified();
    }
    
    @PostUpdate
    protected void onPostUpdate() {
        publishEvent(new EntityUpdatedEvent(this));
    }
}
```

### 3. State Change Hooks

```java
@MappedSuperclass
public abstract class AbstractStatusAwareEntity<ID> {
    @PreUpdate
    protected void onStateChange(EntityStatus newStatus) {
        EntityStatus oldStatus = this.status;
        validateStateTransition(newStatus);
        updateStateMetadata(newStatus);
        publishStateChangeEvent(oldStatus, newStatus);
    }
}
```

## Validation Points

### 1. Pre-Operation Validation

```java
public interface EntityValidator<T> {
    void validateCreate(T entity);
    void validateUpdate(T entity);
    void validateDelete(T entity);
    void validateStateChange(
        T entity, 
        EntityStatus newStatus
    );
}
```

### 2. State Transition Validation

```java
public interface StateTransitionValidator {
    void validateTransition(
        StatusAware entity,
        EntityStatus currentStatus,
        EntityStatus newStatus
    );
    
    boolean isValidTransition(
        EntityStatus from,
        EntityStatus to
    );
}
```

### 3. Business Rule Validation

```java
public interface BusinessRuleValidator {
    void validateRules(Object entity);
    void validateStateRules(
        Object entity,
        EntityStatus targetState
    );
}
```

## Event Publishing

### 1. Lifecycle Events

```java
public abstract class EntityEvent<T> extends ApplicationEvent {
    private final T entity;
    private final EventType type;
    private final LocalDateTime timestamp;
    
    protected EntityEvent(
        T entity,
        EventType type
    ) {
        super(entity);
        this.entity = entity;
        this.type = type;
        this.timestamp = LocalDateTime.now();
    }
}

public class EntityCreatedEvent<T> extends EntityEvent<T> {
    public EntityCreatedEvent(T entity) {
        super(entity, EventType.CREATED);
    }
}

public class EntityStateChangedEvent<T> extends EntityEvent<T> {
    private final EntityStatus oldStatus;
    private final EntityStatus newStatus;
    
    public EntityStateChangedEvent(
        T entity,
        EntityStatus oldStatus,
        EntityStatus newStatus
    ) {
        super(entity, EventType.STATE_CHANGED);
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }
}
```

### 2. Event Handling

```java
@Component
public class EntityEventHandler {
    @EventListener
    public void handleCreated(EntityCreatedEvent<?> event) {
        // Handle creation
        processCreation(event.getEntity());
    }
    
    @EventListener
    public void handleStateChanged(
        EntityStateChangedEvent<?> event
    ) {
        // Handle state change
        processStateChange(
            event.getEntity(),
            event.getOldStatus(),
            event.getNewStatus()
        );
    }
    
    @EventListener
    public void handleDeleted(EntityDeletedEvent<?> event) {
        // Handle deletion
        processDeletion(event.getEntity());
    }
}
```

## Best Practices

### 1. State Management

- Define clear states
- Validate transitions
- Handle edge cases
- Maintain history

### 2. Validation

- Check early
- Fail fast
- Be consistent
- Log failures

### 3. Event Handling

- Decouple logic
- Handle async
- Ensure idempotency
- Manage failures

### 4. Region-Province Status Management

#### Region-Province Status Behavior

1. Region Manual Control
   - Region can be manually activated or deactivated at any time
   - Manual deactivation of Region cascades to EventLocations
   - Manual deactivation persists regardless of Province changes

2. Region Automatic Deactivation
   - Only applies when Region is currently active
   - Triggers when all Provinces become inactive
   - No automatic reactivation of Region

3. Province Independence
   - Province can change status regardless of Region status
   - Province status changes don't affect inactive Regions
   - Province notifies Region of every status change

4. Status Flow
   - Province status changes → Region evaluation
   - Region evaluates only if currently active
   - Region stays active if any Province is active

```java
// Example: Province and Region status interaction
public class Province extends AbstractStatusAwareEntity {
    @Override
    public void setStatus(CommonStatus newStatus) {
        super.setStatus(newStatus);
        // Notify region to evaluate its status
        if (region != null) {
            region.updateStatusBasedOnProvinces();
        }
    }
}

public class Region extends AbstractStatusAwareEntity {
    void updateStatusBasedOnProvinces() {
        // Do nothing if region is already inactive (manual deactivation)
        if (!getStatus().isActive()) {
            return;
        }

        // Only deactivate if all provinces are inactive
        boolean allProvincesInactive = provinces.stream()
            .allMatch(p -> !p.getStatus().isActive());
            
        if (allProvincesInactive) {
            setStatus(CommonStatus.INACTIVE);
        }
        // Region stays active if any province is active
    }
}
```

```java
// Example of Region-Province status interaction
@Entity
public class Region extends AbstractStatusAwareEntity {
    @OneToMany(mappedBy = "region")
    private Set<Province> provinces;

    void updateStatusBasedOnProvinces() {
        boolean hasActiveProvince = provinces.stream()
            .anyMatch(p -> p.getStatus().isActive());
            
        if (hasActiveProvince && !getStatus().isActive()) {
            setStatus(CommonStatus.ACTIVE);
        } else if (!hasActiveProvince && getStatus().isActive()) {
            setStatus(CommonStatus.INACTIVE);
        }
    }
}

@Entity
public class Province extends AbstractStatusAwareEntity {
    @ManyToOne
    private Region region;

    @Override
    public void setStatus(CommonStatus newStatus) {
        super.setStatus(newStatus);
        // Notify region of status change
        if (region != null) {
            region.updateStatusBasedOnProvinces();
        }
    }
}
```

### 5. Region-Province Implementation

```java
@Entity
public class Region extends AbstractStatusAwareEntity {
    @OneToMany(mappedBy = "region")
    private Set<Province> provinces;

    @Override
    public void setStatus(CommonStatus newStatus) {
        boolean wasActive = getStatus().isActive();
        super.setStatus(newStatus);
        
        // Cascade deactivation to event locations
        if (wasActive && !newStatus.isActive()) {
            eventLocations.forEach(el ->
                el.setStatus(CommonStatus.INACTIVE));
        }
    }

    // Package-private method for Province status notifications
    void updateStatusBasedOnProvinces() {
        boolean hasActiveProvince = provinces.stream()
            .anyMatch(p -> p.getStatus().isActive());
            
        if (hasActiveProvince && !getStatus().isActive()) {
            setStatus(CommonStatus.ACTIVE);
        } else if (!hasActiveProvince && getStatus().isActive()) {
            setStatus(CommonStatus.INACTIVE);
        }
    }
}

@Entity
public class Province extends AbstractStatusAwareEntity {
    @ManyToOne
    private Region region;

    @Override
    public void setStatus(CommonStatus newStatus) {
        super.setStatus(newStatus);
        if (region != null) {
            region.updateStatusBasedOnProvinces();
        }
    }
}
```

### 6. Transaction Safety

- Use boundaries
- Handle rollback
- Clean resources
- Maintain consistency

## Implementation Guide

### 1. Entity Definition

```java
@Entity
public class CustomEntity 
    extends AbstractStatusAwareEntity<Long> {
    
    @Override
    protected void validateState() {
        super.validateState();
        // Custom validation
    }
    
    @Override
    protected boolean canTransitionTo(EntityStatus newStatus) {
        // Define valid transitions
        return isValidTransition(getStatus(), newStatus);
    }
}
```

### 2. Service Layer

```java
@Service
@Transactional
public class EntityService {
    public void changeState(
        Long id,
        EntityStatus newStatus
    ) {
        CustomEntity entity = findById(id);
        EntityStatus oldStatus = entity.getStatus();
        
        // Validate transition
        validator.validateStateChange(
            entity, 
            newStatus
        );
        
        // Update state
        entity.setStatus(newStatus);
        
        // Save changes
        repository.save(entity);
        
        // Publish event
        publisher.publishStateChanged(
            entity,
            oldStatus,
            newStatus
        );
    }
}
```

### 3. Event Handling

```java
@Component
public class EntityEventProcessor {
    @Async
    @EventListener
    public void processStateChange(
        EntityStateChangedEvent<?> event
    ) {
        // Process async
        handleStateChange(event);
        
        // Update related entities
        updateRelated(event);
        
        // Send notifications
        notifyStateChange(event);
    }
}
