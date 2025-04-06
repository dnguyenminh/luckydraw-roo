package vn.com.fecredit.app.controller.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import vn.com.fecredit.app.security.MockJwtTokenProvider;
import vn.com.fecredit.app.service.AuditLogService;
import vn.com.fecredit.app.service.EventLocationService;
import vn.com.fecredit.app.service.EventService;
import vn.com.fecredit.app.service.ParticipantService;
import vn.com.fecredit.app.service.RegionService;
import vn.com.fecredit.app.service.RewardService;
import vn.com.fecredit.app.service.SpinHistoryService;
import vn.com.fecredit.app.service.TableDataService;
import vn.com.fecredit.app.service.UserService;
import vn.com.fecredit.app.service.factory.RelatedTablesFactory;
import vn.com.fecredit.app.service.factory.RepositoryFactory;

/**
 * Test configuration for controller tests.
 * Provides mock beans for services needed by controllers.
 */
@TestConfiguration
public class ControllerTestConfig {

    @Bean
    @Primary
    public TableDataService tableDataService() {
        return Mockito.mock(TableDataService.class);
    }
    
    @Bean
    @Primary
    public RepositoryFactory repositoryFactory() {
        return Mockito.mock(RepositoryFactory.class);
    }
    
    @Bean
    @Primary
    public RelatedTablesFactory relatedTablesFactory() {
        return Mockito.mock(RelatedTablesFactory.class);
    }
    
    @Bean
    @Primary
    public AuthenticationManager authenticationManager() {
        return Mockito.mock(AuthenticationManager.class);
    }
    
    @Bean
    @Primary
    public MockJwtTokenProvider jwtTokenProvider() {
        return new MockJwtTokenProvider();
    }
    
    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    @Primary
    public UserService userService() {
        return Mockito.mock(UserService.class);
    }
    
    @Bean
    @Primary
    public AuditLogService auditLogService() {
        return Mockito.mock(AuditLogService.class);
    }
    
    @Bean
    @Primary
    public EventService eventService() {
        return Mockito.mock(EventService.class);
    }
    
    @Bean
    @Primary
    public EventLocationService eventLocationService() {
        return Mockito.mock(EventLocationService.class);
    }
    
    @Bean
    @Primary
    public RegionService regionService() {
        return Mockito.mock(RegionService.class);
    }
    
    @Bean
    @Primary
    public RewardService rewardService() {
        return Mockito.mock(RewardService.class);
    }
    
    @Bean
    @Primary
    public ParticipantService participantService() {
        return Mockito.mock(ParticipantService.class);
    }
    
    @Bean
    @Primary
    public SpinHistoryService spinHistoryService() {
        return Mockito.mock(SpinHistoryService.class);
    }
}
