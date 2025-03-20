---
layout: diagram
title: Entity Operation Sequences
description: Flow of entity operations through the system layers
diagram_url: /generated/entity-sequence.png
previous_diagram: /architecture/entity-exceptions
next_diagram: /architecture/index
---

[← Back to Index](../index.md)

# Entity Operation Sequences

## Operation Flow

### 1. Creation Sequence

```java
@Service
@Transactional
public class EntityService {
    @Autowired
    private EntityValidator validator;
    
    @Autowired
    private EntityRepository repository;
    
    @Autowired
    private EntityEventPublisher publisher;
    
    public Entity create(Entity entity) {
        // Pre-create validation
        validator.validate(entity);
        
        // Set initial state
        entity.setStatus(DRAFT);
        
        // Persist entity
        entity = repository.save(entity);
        
        // Publish event
        publisher.publishCreated(entity);
        
        return entity;
    }
}
```

### 2. State Change Sequence

```java
@Service
@Transactional
public class EntityStateManager {
    @Autowired
    private StateTransitionValidator validator;
    
    @Autowired
    private EntityRepository repository;
    
    @Autowired
    private EntityEventPublisher publisher;
    
    public Entity transition(
        Entity entity,
        EntityStatus newStatus
    ) {
        // Get current state
        EntityStatus oldStatus = entity.getStatus();
        
        // Validate transition
        validator.validateTransition(
            entity, oldStatus, newStatus
        );
        
        // Execute transition
        entity.setStatus(newStatus);
        entity = repository.save(entity);
        
        // Publish event
        publisher.publishStateChanged(
            entity, oldStatus, newStatus
        );
        
        return entity;
    }
}
```

## Operation Steps

### 1. Input Processing

```java
@Component
public class RequestProcessor {
    @Autowired
    private EntityMapper mapper;
    
    @Autowired
    private RequestValidator validator;
    
    public Entity processRequest(EntityDTO dto) {
        // Validate request
        validator.validate(dto);
        
        // Convert to entity
        Entity entity = mapper.toEntity(dto);
        
        // Enrich data
        enrichEntity(entity);
        
        return entity;
    }
}
```

### 2. Business Logic

```java
@Service
public class EntityOperations {
    @Autowired
    private BusinessRuleValidator validator;
    
    @Autowired
    private EntityRepository repository;
    
    @Transactional
    public Entity process(Entity entity) {
        // Validate business rules
        validator.validate(entity);
        
        // Execute business logic
        executeOperations(entity);
        
        // Update entity
        return repository.save(entity);
    }
}
```

### 3. Event Processing

```java
@Component
public class EntityEventProcessor {
    @Autowired
    private EventRepository eventRepo;
    
    @Autowired
    private NotificationService notifier;
    
    @EventListener
    public void handleEntityEvent(EntityEvent event) {
        // Log event
        eventRepo.save(event);
        
        // Process based on type
        switch (event.getType()) {
            case CREATED -> handleCreated(event);
            case UPDATED -> handleUpdated(event);
            case DELETED -> handleDeleted(event);
            case STATE_CHANGED -> handleStateChange(event);
        }
        
        // Send notifications
        notifier.notify(event);
    }
}
```

## Operation Types

### 1. CRUD Operations

```java
@RestController
@RequestMapping("/api/entities")
public class EntityController {
    @PostMapping
    public ResponseEntity<?> create(
        @Valid @RequestBody EntityDTO dto
    ) {
        return execute(() -> service.create(dto));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
        @PathVariable Long id,
        @Valid @RequestBody EntityDTO dto
    ) {
        return execute(() -> service.update(id, dto));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
        @PathVariable Long id
    ) {
        return execute(() -> service.delete(id));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> get(
        @PathVariable Long id
    ) {
        return execute(() -> service.findById(id));
    }
}
```

### 2. State Operations

```java
@RestController
@RequestMapping("/api/entities/{id}")
public class EntityStateController {
    @PostMapping("/activate")
    public ResponseEntity<?> activate(
        @PathVariable Long id
    ) {
        return execute(() -> 
            service.transition(id, ACTIVE));
    }
    
    @PostMapping("/deactivate")
    public ResponseEntity<?> deactivate(
        @PathVariable Long id
    ) {
        return execute(() -> 
            service.transition(id, INACTIVE));
    }
    
    @PostMapping("/archive")
    public ResponseEntity<?> archive(
        @PathVariable Long id
    ) {
        return execute(() -> 
            service.transition(id, ARCHIVED));
    }
}
```

### 3. Batch Operations

```java
@Service
@Transactional
public class BatchOperations {
    @Autowired
    private EntityService service;
    
    public BatchResult processBatch(
        List<EntityDTO> items
    ) {
        BatchResult result = new BatchResult();
        
        for (EntityDTO item : items) {
            try {
                Entity entity = service.process(item);
                result.addSuccess(entity);
            } catch (Exception e) {
                result.addError(item, e);
            }
        }
        
        return result;
    }
}
```

## Best Practices

### 1. Request Flow
- Validate early
- Use DTOs
- Map properly
- Handle errors

### 2. Processing Flow
- Check permissions
- Validate state
- Apply rules
- Maintain consistency

### 3. Response Flow
- Map results
- Include metadata
- Handle errors
- Format properly

### 4. Event Flow
- Publish async
- Handle failures
- Maintain order
- Track processing

## Implementation Guide

### 1. Operation Design

1. Define endpoints:
   ```java
   @PostMapping("/{id}/process")
   public ResponseEntity<?> process() {
       // Implementation
   }
   ```

2. Create service:
   ```java
   @Service
   public class OperationService {
       // Implementation
   }
   ```

3. Handle events:
   ```java
   @EventListener
   public void onEvent() {
       // Implementation
   }
   ```

### 2. Flow Control

1. Pre-processing:
   ```java
   validator.validate(input);
   ```

2. Main processing:
   ```java
   entity = service.process(entity);
   ```

3. Post-processing:
   ```java
   publisher.publish(event);
   ```

### 3. Error Handling

1. Try-catch blocks:
   ```java
   try {
       return execute();
   } catch (Exception e) {
       handle(e);
   }
   ```

2. Result wrapping:
   ```java
   return Result.of(() -> 
       service.execute()
   );
   ```

3. Error responses:
   ```java
   return ResponseEntity
       .status(status)
       .body(error);
