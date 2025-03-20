---
layout: diagram
title: Entity Validation Framework
description: Multi-layered validation system with constraint handling and error processing
diagram_url: /generated/entity-validation.png
previous_diagram: /architecture/entity-classes
next_diagram: /architecture/entity-exceptions
---

[← Back to Index](../index.md)

# Entity Validation Framework

## Validation Architecture

The validation framework is built as a multi-layered system:

### 1. Bean Validation Layer

Basic constraint validation using Jakarta Bean Validation:

```java
@Entity
public class User extends AbstractStatusAwareEntity<Long> {
    @NotNull
    @Size(min = 3, max = 50)
    private String username;
    
    @Email
    @NotBlank
    private String email;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$")
    private String phone;
    
    @Past
    private LocalDate birthDate;
}
```

### 2. Business Rule Layer

Domain-specific validation rules:

```java
@Component
public class BusinessRuleValidator {
    @Autowired
    private RuleEngine ruleEngine;
    
    public <T> void validate(T entity) {
        List<ValidationRule<T>> rules = 
            ruleEngine.getRulesFor(entity.getClass());
            
        for (ValidationRule<T> rule : rules) {
            if (!rule.test(entity)) {
                throw new BusinessRuleViolation(
                    rule.getMessage(),
                    rule.getCode()
                );
            }
        }
    }
}
```

### 3. State Transition Layer

Status change validation:

```java
@Component
public class StateTransitionValidator {
    public void validateTransition(
        StatusAware entity,
        EntityStatus newStatus
    ) {
        EntityStatus currentStatus = entity.getStatus();
        
        // Check if transition is allowed
        if (!entity.canTransitionTo(newStatus)) {
            throw new IllegalStateTransitionException(
                entity,
                currentStatus,
                newStatus
            );
        }
        
        // Validate transition-specific rules
        validateTransitionRules(
            entity, 
            currentStatus,
            newStatus
        );
    }
}
```

### 4. Cross-Entity Validation

Validation across multiple entities:

```java
@Component
public class CrossEntityValidator {
    @Autowired
    private EntityRelationshipService relations;
    
    public void validateRelationships(Object entity) {
        // Get related entities
        Set<Object> related = 
            relations.getRelatedEntities(entity);
            
        // Validate relationships
        for (Object relatedEntity : related) {
            validateRelationship(
                entity,
                relatedEntity
            );
        }
    }
}
```

## Custom Constraints

### 1. Custom Validator

```java
@Constraint(validatedBy = CustomValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomConstraint {
    String message() default "Invalid value";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

public class CustomValidator 
    implements ConstraintValidator<CustomConstraint, String> {
    
    @Override
    public boolean isValid(
        String value,
        ConstraintValidatorContext context
    ) {
        // Custom validation logic
        return validateCustomLogic(value);
    }
}
```

### 2. Business Rule

```java
public class BusinessRule<T> implements ValidationRule<T> {
    private final String name;
    private final Predicate<T> condition;
    private final String message;
    private final ErrorCode code;
    
    @Override
    public boolean test(T entity) {
        return condition.test(entity);
    }
}
```

### 3. Composite Validation

```java
public class CompositeValidator<T> implements EntityValidator<T> {
    private final List<EntityValidator<T>> validators;
    
    @Override
    public void validate(T entity) {
        ValidationResult result = new ValidationResult();
        
        for (EntityValidator<T> validator : validators) {
            try {
                validator.validate(entity);
            } catch (ValidationException e) {
                result.addErrors(e.getViolations());
            }
        }
        
        if (!result.isValid()) {
            throw new CompositeValidationException(result);
        }
    }
}
```

## Best Practices

### 1. Layered Validation

- Start with bean validation
- Add business rules
- Include state validation
- Check relationships

### 2. Error Handling

- Use specific exceptions
- Include error context
- Provide clear messages
- Support error codes

### 3. Performance

- Validate early
- Cache rules
- Batch validations
- Use async where possible

### 4. Maintenance

- Document rules
- Test thoroughly
- Monitor performance
- Review regularly
