package vn.com.fecredit.app.repository;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import vn.com.fecredit.app.repository.config.TestConfig;

/**
 * Base test class for all repository tests.
 * Provides common configuration to ensure consistency across all repository tests.
 */
@DataJpaTest
@ContextConfiguration(classes = TestConfig.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.test.database.replace=none"
})
@Transactional
public abstract class AbstractRepositoryTest {
    // Common test utilities and helpers can be added here
}
