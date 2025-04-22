# Entity Architecture Documentation

## Overview

The entity framework in the Lucky Draw application provides a robust foundation for domain entities with features like auditing, status management, optimistic locking, and consistent ID generation. This document explains the entity hierarchy and how to use base entity classes.

## Entity Class Hierarchy

The entity classes form a structured hierarchy to provide increasing levels of functionality:

```
                   AbstractAuditEntity
                          ↑
                   AbstractStatusAwareEntity
                          ↑
                   AbstractPersistableEntity
                  ↗                      ↖
 AbstractSimplePersistableEntity    AbstractComplexPersistableEntity
```

### Core Classes and Interfaces

1. **AbstractAuditEntity**
   - Base class for all entities
   - Provides audit fields (created/updated by/at)
   - Uses Spring Data JPA's auditing support

2. **AbstractStatusAwareEntity**
   - Adds status tracking (ACTIVE, INACTIVE, etc.)
   - Implements `StatusAware` interface
   - Provides status lifecycle methods (activate/deactivate)

3. **AbstractPersistableEntity**
   - Adds optimistic locking via version field
   - Implements `PersistableEntity` interface

4. **AbstractSimplePersistableEntity**
   - Uses simple auto-incremented ID (@GenerationType.IDENTITY)
   - For entities with straightforward primary keys

5. **AbstractComplexPersistableEntity**
   - Uses embedded composite primary keys (@EmbeddedId)
   - For entities requiring complex/composite keys

## Key Interfaces

1. **PersistableEntity**
   - Contract for entity persistence operations
   - Defines ID management and version handling

2. **StatusAware**
   - Contract for entities with status tracking
   - Defines status management methods

## Usage Guidelines

### When to Use Which Base Class

- For most entities, extend `AbstractSimplePersistableEntity<Long>` with Long IDs
- For entities with composite keys, extend `AbstractComplexPersistableEntity<YourEmbeddableIdClass>`
- Only extend `AbstractStatusAwareEntity` or `AbstractAuditEntity` directly if you have special requirements

### Example Usage

```java
@Entity
@Table(name = "products")
@Getter @Setter
@NoArgsConstructor
@SuperBuilder
public class Product extends AbstractSimplePersistableEntity<Long> {
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "price")
    private BigDecimal price;
    
    // Additional fields and methods...
}
```

### ID Types

The entity framework supports various ID types via the generic type parameter:

- `AbstractSimplePersistableEntity<Long>` - Long ID (most common)
- `AbstractSimplePersistableEntity<Integer>` - Integer ID
- `AbstractSimplePersistableEntity<String>` - String ID
- `AbstractComplexPersistableEntity<CustomId>` - Custom embeddable ID

## Status Management

Entities extending `AbstractStatusAwareEntity` have built-in status management:

```java
Product product = new Product();
product.activate();   // Sets status to ACTIVE
product.isActive();   // Returns true
product.deactivate(); // Sets status to INACTIVE
```

The `CommonStatus` enum provides standard statuses:
- `ACTIVE` - Entity is active and available
- `INACTIVE` - Entity is inactive but still exists
- `PENDING` - Entity is pending activation/approval
- `ARCHIVED` - Entity is archived (historical)
- `DELETED` - Entity has been soft-deleted

## Auditing Configuration

The entity framework integrates with Spring Data JPA's auditing:

1. Enable auditing in your configuration:
   ```java
   @Configuration
   @EnableJpaAuditing
   public class JpaConfig {
       @Bean
       public AuditorAware<String> auditorProvider() {
           // Return current user from security context
       }
   }
   ```

2. Audit fields are automatically populated:
   - `createdBy` - User who created the entity
   - `createdAt` - Timestamp when created
   - `updatedBy` - User who last updated the entity
   - `updatedAt` - Timestamp when last updated

## Best Practices

1. **Always use the appropriate base class** for your entity's needs
2. **Don't override audit fields' behavior** unless absolutely necessary
3. **Use the status methods** (activate/deactivate) instead of setting status directly
4. **Remember that version fields** are managed by JPA for optimistic locking

## Common Pitfalls

1. **Not setting up auditing properly** - Make sure to configure `@EnableJpaAuditing`
2. **Manually setting IDs** - Let the database generate IDs with identity strategy
3. **Inappropriate use of @EmbeddedId** - Only use complex keys when necessary
4. **Not handling optimistic locking exceptions** - Add proper error handling for concurrent modifications
