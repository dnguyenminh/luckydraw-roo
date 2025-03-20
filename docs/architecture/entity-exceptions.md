---
layout: diagram
title: Entity Exception Framework
description: Exception handling, error responses, and error recovery processes
diagram_url: /generated/entity-exceptions.png
previous_diagram: /architecture/entity-validation
next_diagram: /architecture/entity-sequence
---

[← Back to Index](../index.md)

# Entity Exception Framework

## Exception Hierarchy

### Base Exception

```java
public abstract class EntityException extends RuntimeException {
    private final String entityType;
    private final Object entityId;
    private final String operation;
    private final ErrorCode code;
    
    protected EntityException(
        String entityType,
        Object entityId,
        String operation,
        ErrorCode code,
        String message
    ) {
        super(message);
        this.entityType = entityType;
        this.entityId = entityId;
        this.operation = operation;
        this.code = code;
    }
    
    public abstract HttpStatus getHttpStatus();
    public abstract ErrorResponse toResponse();
}
```

### Specialized Exceptions

```java
public class EntityNotFoundException extends EntityException {
    public EntityNotFoundException(String entityType, Object id) {
        super(
            entityType,
            id,
            "FIND",
            ErrorCode.ENTITY_NOT_FOUND,
            String.format("Entity %s with id %s not found", entityType, id)
        );
    }
    
    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}

public class IllegalEntityStateException extends EntityException {
    private final EntityStatus currentState;
    private final EntityStatus targetState;
    
    public IllegalEntityStateException(
        String entityType,
        Object entityId,
        EntityStatus current,
        EntityStatus target
    ) {
        super(
            entityType,
            entityId,
            "STATE_CHANGE",
            ErrorCode.ILLEGAL_STATE,
            String.format("Cannot transition from %s to %s", current, target)
        );
        this.currentState = current;
        this.targetState = target;
    }
    
    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.CONFLICT;
    }
}
```

## Error Handling

### Exception Handler

```java
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class EntityExceptionHandler {
    
    @ExceptionHandler(EntityException.class)
    public ResponseEntity<ErrorResponse> handleEntityException(
        EntityException ex
    ) {
        ErrorResponse response = ex.toResponse();
        return new ResponseEntity<>(
            response,
            ex.getHttpStatus()
        );
    }
    
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(ValidationException ex) {
        return ErrorResponse.builder()
            .code(ErrorCode.VALIDATION_FAILED)
            .message("Validation failed")
            .details(ex.getViolations())
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    @ExceptionHandler(TransactionException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleTransaction(TransactionException ex) {
        return ErrorResponse.builder()
            .code(ErrorCode.TRANSACTION_FAILED)
            .message(ex.getMessage())
            .operation(ex.getOperation())
            .timestamp(LocalDateTime.now())
            .build();
    }
}
```

### Error Response

```java
@Data
@Builder
public class ErrorResponse {
    private String code;
    private String message;
    private String entityType;
    private Object entityId;
    private String operation;
    private LocalDateTime timestamp;
    private Map<String, Object> details;
    
    public static ErrorResponse from(EntityException ex) {
        return builder()
            .code(ex.getCode().toString())
            .message(ex.getMessage())
            .entityType(ex.getEntityType())
            .entityId(ex.getEntityId())
            .operation(ex.getOperation())
            .timestamp(LocalDateTime.now())
            .build();
    }
}
```

## Error Recovery

### Transaction Management

```java
@Aspect
@Component
public class TransactionAspect {
    
    @Around("@annotation(Transactional)")
    public Object manageTransaction(ProceedingJoinPoint point)
        throws Throwable {
        try {
            return point.proceed();
        } catch (EntityException ex) {
            // Log error
            // Roll back transaction
            // Clean up resources
            throw ex;
        }
    }
}
```

### Recovery Process

```java
@Component
public class ErrorRecoveryManager {
    @Autowired
    private TransactionTemplate txTemplate;
    
    public void recoverFromError(EntityException ex) {
        txTemplate.execute(status -> {
            try {
                // Attempt recovery
                handleRecovery(ex);
                return true;
            } catch (Exception e) {
                status.setRollbackOnly();
                return false;
            }
        });
    }
    
    private void handleRecovery(EntityException ex) {
        switch (ex.getCode()) {
            case ENTITY_NOT_FOUND -> handleNotFound(ex);
            case ILLEGAL_STATE -> handleIllegalState(ex);
            case VALIDATION_FAILED -> handleValidation(ex);
            default -> handleGenericError(ex);
        }
    }
}
```

## Best Practices

### 1. Exception Design

- Use hierarchy
- Include context
- Clear messages
- Error codes

### 2. Error Handling

- Centralize handling
- Proper mapping
- Clean responses
- Recovery paths

### 3. Transaction Safety

- Proper boundaries
- Clean rollback
- Resource cleanup
- State consistency

### 4. Logging & Monitoring

- Log details
- Track frequency
- Monitor patterns
- Alert on thresholds

## Implementation Guide

### 1. Creating Exceptions

1. Extend base class:
   ```java
   public class CustomException extends EntityException {
       // Implementation
   }
   ```

2. Add context:
   ```java
   super(
       entityType,
       entityId,
       operation,
       errorCode,
       message
   );
   ```

3. Define status:
   ```java
   @Override
   public HttpStatus getHttpStatus() {
       return HttpStatus.BAD_REQUEST;
   }
   ```

### 2. Handling Errors

1. Add handler:
   ```java
   @ExceptionHandler(CustomException.class)
   public ResponseEntity<?> handleCustom(CustomException ex) {
       // Handle exception
   }
   ```

2. Map response:
   ```java
   ErrorResponse.builder()
       .code(ex.getCode())
       .message(ex.getMessage())
       .build();
   ```

3. Set status:
   ```java
   new ResponseEntity<>(
       response,
       ex.getHttpStatus()
   );
   ```

### 3. Recovery Process

1. Define strategy:
   ```java
   @Component
   public class CustomRecoveryStrategy {
       // Recovery logic
   }
   ```

2. Handle cleanup:
   ```java
   public void cleanup(EntityException ex) {
       // Cleanup resources
   }
   ```

3. Restore state:
   ```java
   public void restore(EntityException ex) {
       // Restore valid state
   }
