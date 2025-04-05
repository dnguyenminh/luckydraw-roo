package vn.com.fecredit.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import vn.com.fecredit.app.entity.AuditLog;
import vn.com.fecredit.app.entity.enums.CommonStatus;
import vn.com.fecredit.app.repository.AuditLogRepository;
import vn.com.fecredit.app.service.impl.AuditLogServiceImpl;

class AuditLogServiceTest extends AbstractServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    private AuditLogService auditLogService;
    
    @Captor
    private ArgumentCaptor<AuditLog> auditLogCaptor;

    private AuditLog userAuditLog;
    private AuditLog eventAuditLog;
    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        // Initialize service with mock repository
        auditLogService = new AuditLogServiceImpl(auditLogRepository);
        
        userAuditLog = AuditLog.builder()
                .id(1L)
                .objectType("User")
                .objectId(1L)
                .oldValue("{\"username\":\"old_name\"}")
                .newValue("{\"username\":\"new_name\"}")
                .valueType("String")
                .propertyPath("username")
                .updateTime(now)
                .status(CommonStatus.ACTIVE)
                .build();

        eventAuditLog = AuditLog.builder()
                .id(2L)
                .objectType("Event")
                .objectId(1L)
                .oldValue("{\"name\":\"Old Event\"}")
                .newValue("{\"name\":\"New Event\"}")
                .valueType("String")
                .propertyPath("name")
                .updateTime(now)
                .status(CommonStatus.ACTIVE)
                .build();
    }

    @Test
    void findById_ShouldReturnAuditLog_WhenExists() {
        // given
        when(auditLogRepository.findById(1L)).thenReturn(Optional.of(userAuditLog));

        // when
        Optional<AuditLog> result = auditLogService.findById(1L);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getObjectType()).isEqualTo("User");
        verify(auditLogRepository).findById(1L);
    }

    @Test
    void findAll_ShouldReturnAllAuditLogs() {
        // given
        List<AuditLog> auditLogs = Arrays.asList(userAuditLog, eventAuditLog);
        when(auditLogRepository.findAll()).thenReturn(auditLogs);

        // when
        List<AuditLog> result = auditLogService.findAll();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("objectType")
                .containsExactly("User", "Event");
        verify(auditLogRepository).findAll();
    }

    @Test
    void findByObjectTypeAndObjectId_ShouldReturnMatchingAuditLogs() {
        // given
        List<AuditLog> userAuditLogs = Arrays.asList(userAuditLog);
        when(auditLogRepository.findByObjectTypeAndObjectId("User", 1L)).thenReturn(userAuditLogs);

        // when
        List<AuditLog> result = auditLogService.findByObjectTypeAndObjectId("User", 1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPropertyPath()).isEqualTo("username");
        verify(auditLogRepository).findByObjectTypeAndObjectId("User", 1L);
    }

    @Test
    void findByObjectTypeAndObjectIdAndPropertyPath_ShouldReturnMatchingAuditLog() {
        // given
        List<AuditLog> matchingLogs = Arrays.asList(userAuditLog);
        when(auditLogRepository.findByObjectTypeAndObjectIdAndPropertyPath("User", 1L, "username"))
            .thenReturn(matchingLogs);

        // when
        List<AuditLog> result = auditLogService.findByObjectTypeAndObjectIdAndPropertyPath(
                "User", 1L, "username");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOldValue()).contains("old_name");
        verify(auditLogRepository).findByObjectTypeAndObjectIdAndPropertyPath("User", 1L, "username");
    }

    @Test
    void findByUpdateTimeBetween_ShouldReturnAuditLogsInTimeRange() {
        // given
        LocalDateTime start = now.minusHours(1);
        LocalDateTime end = now.plusHours(1);
        List<AuditLog> matchingLogs = Arrays.asList(userAuditLog, eventAuditLog);
        when(auditLogRepository.findByUpdateTimeBetween(start, end)).thenReturn(matchingLogs);

        // when
        List<AuditLog> result = auditLogService.findByUpdateTimeBetween(start, end);

        // then
        assertThat(result).hasSize(2);
        verify(auditLogRepository).findByUpdateTimeBetween(start, end);
    }

    @Test
    void findByUsername_ShouldReturnAuditLogsByUser() {
        // given
        List<AuditLog> userLogs = Arrays.asList(userAuditLog);
        when(auditLogRepository.findByUsername("admin")).thenReturn(userLogs);

        // when
        List<AuditLog> result = auditLogService.findByUsername("admin");

        // then
        assertThat(result).hasSize(1);
        verify(auditLogRepository).findByUsername("admin");
    }

    @Test
    void save_ShouldReturnSavedAuditLog() {
        // given
        AuditLog toSave = userAuditLog.toBuilder().build();
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(toSave);

        // when
        AuditLog saved = auditLogService.save(toSave);

        // then
        assertThat(saved).isNotNull();
        assertThat(saved.getObjectType()).isEqualTo("User");
        verify(auditLogRepository).save(toSave);
    }

    @Test
    void createAuditEntry_ShouldCreateValidAuditLog() {
        // given
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog saved = invocation.getArgument(0);
            saved.setId(1L); // Simulate ID generation
            return saved;
        });

        // when
        AuditLog result = auditLogService.createAuditEntry(
                "Event", 
                1L, 
                "{\"name\":\"Old Event\"}", 
                "{\"name\":\"New Event\"}", 
                "String", 
                "name", 
                "admin");

        // then
        verify(auditLogRepository).save(auditLogCaptor.capture());
        AuditLog captured = auditLogCaptor.getValue();
        
        assertThat(result).isNotNull();
        assertThat(captured.getObjectType()).isEqualTo("Event");
        assertThat(captured.getObjectId()).isEqualTo(1L);
        assertThat(captured.getOldValue()).isEqualTo("{\"name\":\"Old Event\"}");
        assertThat(captured.getNewValue()).isEqualTo("{\"name\":\"New Event\"}");
        assertThat(captured.getValueType()).isEqualTo("String");
        assertThat(captured.getPropertyPath()).isEqualTo("name");
        assertThat(captured.getCreatedBy()).isEqualTo("admin");
        assertThat(captured.getUpdatedBy()).isEqualTo("admin");
        assertThat(captured.getStatus()).isEqualTo(CommonStatus.ACTIVE);
    }
}
