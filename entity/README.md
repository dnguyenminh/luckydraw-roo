# Entity Framework Documentation

## Overview

This document outlines the entity class hierarchy used in the LuckyDraw application. The entity framework provides a structured approach to data persistence with built-in auditing, status management, and different ID generation strategies.

## Entity Class Hierarchy

The entity framework follows a layered inheritance pattern:

```
                  AbstractAuditEntity
                         ↑
                  AbstractStatusAwareEntity
                         ↑
                  AbstractPersistableEntity
                 ↗                      ↖
AbstractSimplePersistableEntity    AbstractComplexPersistableEntity
```

## Core Classes

### AbstractAuditEntity

The base class for all entities, providing auditing capabilities:

- **Tracks creation metadata**: `createdBy` and `createdAt`
- **Tracks modification metadata**: `updatedBy` and `updatedAt`
- Uses Spring Data's `@CreatedBy`, `@CreatedDate`, `@LastModifiedBy`, and `@LastModifiedDate` annotations
- Integrates with `AuditingEntityListener` for automatic auditing

### AbstractStatusAwareEntity

Extends auditing with status management:

- Adds a `status` field using the `CommonStatus` enum (ACTIVE, INACTIVE, etc.)
- Provides convenience methods: `activate()`, `deactivate()`, `isActive()`
- Implements the `StatusAware` interface
- Includes temporary attributes for runtime use (not persisted)

### AbstractPersistableEntity

Adds version-based optimistic locking:

- Provides a `version` field annotated with `@Version`
- Implements the `PersistableEntity<T>` interface
- Acts as a common base for different ID generation strategies

### ID Generation Strategy Variants

#### AbstractSimplePersistableEntity

For entities with simple auto-incremented primary keys:

- Uses `@GeneratedValue(strategy = GenerationType.IDENTITY)`
- Suitable for most standard entities with numeric IDs

#### AbstractComplexPersistableEntity

For entities with composite or complex primary keys:

- Uses `@EmbeddedId` annotation
- Supports composite keys defined as embeddable classes

## Usage Guidelines

- For standard entities with auto-incremented IDs:
  ```java
  public class Customer extends AbstractSimplePersistableEntity<Long> {
      // Entity-specific fields
  }
  ```

- For entities with composite keys:
  ```java
  public class OrderItem extends AbstractComplexPersistableEntity<OrderItemId> {
      // Entity-specific fields
  }
  ```

## Benefits

1. **Consistent Auditing**: All entities automatically track creation and modification metadata
2. **Status Management**: Built-in active/inactive status handling
3. **Optimistic Locking**: Prevents concurrent modification conflicts
4. **Flexible ID Strategies**: Support for both simple and complex primary keys
5. **Type Safety**: Generic typing ensures ID type consistency

## Design Considerations

- All entity classes use Lombok annotations for reducing boilerplate
- `@SuperBuilder` pattern enables fluid builder syntax that works with inheritance
- `@MappedSuperclass` ensures that fields are inherited in the database schema
- Abstract base classes prevent direct instantiation
