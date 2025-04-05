package vn.com.fecredit.app.service.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import vn.com.fecredit.app.repository.AuditLogRepository;
import vn.com.fecredit.app.service.AuditLogService;
import vn.com.fecredit.app.service.impl.AuditLogServiceImpl;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public AuditLogRepository auditLogRepository() {
        return Mockito.mock(AuditLogRepository.class);
    }
    
    @Bean
    public AuditLogService auditLogService(AuditLogRepository auditLogRepository) {
        return new AuditLogServiceImpl(auditLogRepository);
    }
}
