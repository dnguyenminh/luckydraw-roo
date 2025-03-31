package vn.com.fecredit.app.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import vn.com.fecredit.app.entity.AuditLog;
import vn.com.fecredit.app.entity.CommonStatus;
import vn.com.fecredit.app.entity.enums.ActionType;
import vn.com.fecredit.app.repository.config.TestConfig;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// @DataJpaTest
@DataJpaTest
@ContextConfiguration(classes = TestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuditLogRepositoryTest {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Test
    void testPersistAndRetrieve() {
        AuditLog log = new AuditLog();
        log.setUsername("testUser");
        log.setActionType(ActionType.CREATE);
        log.setTimestamp(LocalDateTime.now());
        log.setStatus(CommonStatus.ACTIVE);
        log.setCreatedAt(LocalDateTime.now()); // Add this line
        log.setCreatedBy("testUser"); // Add this line
        log.setUpdatedAt(LocalDateTime.now()); // Add this line
        log.setUpdatedBy("testUser"); // Add this line
        auditLogRepository.save(log);

        List<AuditLog> found = auditLogRepository.findByUsername("testUser");
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getCreatedAt()).isNotNull();
        assertThat(found.get(0).getCreatedBy()).isEqualTo("testUser");
        assertThat(found.get(0).getUpdatedAt()).isNotNull();
        assertThat(found.get(0).getUpdatedBy()).isEqualTo("testUser");
    }
}