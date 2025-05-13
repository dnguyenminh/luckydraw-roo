package vn.com.fecredit.app.repository;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

/**
 * Base class for repository tests
 * Sets up a common test environment with the H2 in-memory database
 */
@Slf4j
@DataJpaTest
@ActiveProfiles("test")
@ComponentScan(basePackages = {"vn.com.fecredit.app.repository"})
@Transactional
public abstract class AbstractRepositoryTest {
    // Common test utilities and helpers can be added here
    @Autowired
    public DatabaseCleanupUtil databaseCleanupUtil;

    @BeforeEach
    void setUp() {
        databaseCleanupUtil.cleanAllTables();
    }
}
