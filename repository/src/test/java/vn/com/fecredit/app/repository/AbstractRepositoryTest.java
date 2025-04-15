package vn.com.fecredit.app.repository;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Base class for repository tests
 * Sets up a common test environment with the H2 in-memory database
 */
@Slf4j
@DataJpaTest
@ActiveProfiles("test")
@Import(DatabaseCleanupUtil.class)
@ComponentScan(basePackages = {"vn.com.fecredit.app.repository"})
@Transactional
public abstract class AbstractRepositoryTest {
    // Common test utilities and helpers can be added here
}
