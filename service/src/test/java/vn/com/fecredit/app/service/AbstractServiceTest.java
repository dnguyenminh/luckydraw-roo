package vn.com.fecredit.app.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Base class for service tests that handles common extension setup.
 * This allows for consistent test configuration across all service tests.
 */
@ExtendWith({MockitoExtension.class, SpringExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT) // Set lenient mode for all Mockito tests
@ActiveProfiles("test")
public abstract class AbstractServiceTest {
    // Common test utility methods can be added here
}
