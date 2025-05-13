package vn.com.fecredit.app.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.extern.slf4j.Slf4j;
import org.springframework.test.context.jdbc.Sql;
import vn.com.fecredit.app.entity.AuditLog;

@Slf4j
// @DataJpaTest
// @ActiveProfiles("test")
// @AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = {"classpath:data-h2.sql"})
class AuditLogRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Test
    void testFindByObjectType() {
        // The test data is already loaded from data-h2.sql
        // We can verify that there's one 'User' record in the test data
        List<AuditLog> userLogs = auditLogRepository.findByObjectType("User");
        log.info("Found {} logs with objectType=User", userLogs.size());
        assertThat(userLogs).hasSize(1);

        // We can verify that there's one 'Event' record in the test data
        List<AuditLog> eventLogs = auditLogRepository.findByObjectType("Event");
        log.info("Found {} logs with objectType=Event", eventLogs.size());
        assertThat(eventLogs).hasSize(1);

        // We can verify that there's one 'Reward' record in the test data
        List<AuditLog> rewardLogs = auditLogRepository.findByObjectType("Reward");
        log.info("Found {} logs with objectType=Reward", rewardLogs.size());
        assertThat(rewardLogs).hasSize(1);
    }
}
