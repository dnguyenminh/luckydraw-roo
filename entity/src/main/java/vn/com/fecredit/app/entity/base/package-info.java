/**
 * Base entity framework for the Lucky Draw application.
 * <p>
 * This package contains the core abstract entity classes that form the foundation
 * of the application's persistence model. The entity hierarchy provides:
 * </p>
 * <ul>
 *   <li>Consistent auditing (creation/modification tracking)</li>
 *   <li>Status management (active/inactive entities)</li>
 *   <li>Optimistic locking with version fields</li>
 *   <li>Support for both simple and complex primary key strategies</li>
 * </ul>
 * <p>
 * The main inheritance hierarchy is structured as follows:
 * </p>
 * <pre>
 *                   AbstractAuditEntity
 *                          ↑
 *                   AbstractStatusAwareEntity
 *                          ↑
 *                   AbstractPersistableEntity
 *                  ↗                      ↖
 * AbstractSimplePersistableEntity    AbstractComplexPersistableEntity
 * </pre>
 * <p>
 * Application-specific entities should extend either {@link vn.com.fecredit.app.entity.base.AbstractSimplePersistableEntity}
 * or {@link vn.com.fecredit.app.entity.base.AbstractComplexPersistableEntity} based on their
 * primary key requirements.
 * </p>
 */
package vn.com.fecredit.app.entity.base;
