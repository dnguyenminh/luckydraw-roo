package vn.com.fecredit.app.repository.base;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import vn.com.fecredit.app.repository.config.TestConfig;
import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.UUID;

@DataJpaTest
@Import(TestConfig.class)
@ActiveProfiles("test")
public abstract class BaseRepositoryTest {

    @Autowired
    protected EntityManager entityManager;

    protected final LocalDateTime now = LocalDateTime.now();
    protected final String testUser = "test-user";

    @BeforeEach
    void setUp() {
        // Clear persistence context to avoid caching issues
        entityManager.clear();
    }

    protected void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    protected UUID randomUUID() {
        return UUID.randomUUID();
    }

    protected String randomString(int length) {
        return UUID.randomUUID().toString().substring(0, length);
    }

    protected LocalDateTime futureDateTime(long plusMinutes) {
        return now.plusMinutes(plusMinutes);
    }

    protected LocalDateTime pastDateTime(long minusMinutes) {
        return now.minusMinutes(minusMinutes);
    }

    /**
     * Saves an entity and returns a fresh instance from the database
     * @param entity Entity to save
     * @param <T> Entity type
     * @return Fresh instance from database
     */
    @SuppressWarnings("unchecked")
    protected <T> T saveAndReload(T entity) {
        entityManager.persist(entity);
        flushAndClear();
        return (T) entityManager.find(entity.getClass(), 
            entityManager.getEntityManagerFactory()
                .getPersistenceUnitUtil()
                .getIdentifier(entity));
    }

    /**
     * Saves multiple entities and clears the persistence context
     * @param entities Entities to save
     */
    protected void saveAll(Object... entities) {
        for (Object entity : entities) {
            entityManager.persist(entity);
        }
        flushAndClear();
    }
}