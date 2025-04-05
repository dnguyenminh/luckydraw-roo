package vn.com.fecredit.app.repository.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

/**
 * Configures test lifecycle hooks to ensure database is properly set up/torn down
 */
@Configuration
@AutoConfigureTestEntityManager
@Slf4j
public class TestLifecycleConfig {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Listener that ensures database tables are properly prepared before each test
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public TestLifecycleListener testLifecycleListener() {
        return new TestLifecycleListener();
    }

    /**
     * Listener class that handles database setup/cleanup for tests
     */
    public static class TestLifecycleListener extends AbstractTestExecutionListener {
        
        @Override
        public int getOrder() {
            return DependencyInjectionTestExecutionListener.HIGHEST_PRECEDENCE - 10;
        }
        
        @Override
        public void beforeTestClass(@NonNull TestContext testContext) throws Exception {
            // Make sure test environment is correctly set
            System.setProperty("spring.profiles.active", "test");
        }
        
        @Override
        public void beforeTestMethod(@NonNull TestContext testContext) throws Exception {
            // Nothing special needed here as @Transactional and @Rollback handle most cases
        }
        
        @Override
        public void afterTestMethod(@NonNull TestContext testContext) throws Exception {
            // Nothing special needed here as @Transactional and @Rollback handle most cases
        }
    }

    /**
     * Transaction manager delegate that ensures transaction boundaries are respected
     */
    @Bean
    @Primary
    @DependsOn("transactionManager")
    public org.springframework.transaction.support.TransactionTemplate safeTransactionTemplate(
            org.springframework.transaction.PlatformTransactionManager transactionManager) {
        org.springframework.transaction.support.TransactionTemplate template = 
            new org.springframework.transaction.support.TransactionTemplate(transactionManager);
        template.setTimeout(60); // 60 second timeout
        return template;
    }

    /**
     * Utility to reset database state when needed
     */
    @Transactional
    public void resetDatabase() {
        try {
            log.debug("Resetting database state");
            entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();
            
            // Truncate standard tables
            String[] tables = {
                "user_roles", "blacklisted_tokens", "role_permissions", 
                "spin_histories", "participant_events", "audit_logs"
            };
            
            for (String table : tables) {
                try {
                    entityManager.createNativeQuery("TRUNCATE TABLE " + table).executeUpdate();
                } catch (Exception e) {
                    // Ignore - table might not exist yet
                }
            }
            
            entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
            entityManager.flush();
        } catch (Exception e) {
            log.warn("Error resetting database: {}", e.getMessage());
        }
    }
}
