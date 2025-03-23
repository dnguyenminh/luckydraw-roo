package vn.com.fecredit.app.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.com.fecredit.app.entity.CommonStatus;
import vn.com.fecredit.app.entity.Configuration;
import vn.com.fecredit.app.repository.ConfigurationRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigurationServiceImplTest {

    @Mock
    private ConfigurationRepository configurationRepository;

    @InjectMocks
    private ConfigurationServiceImpl configurationService;

    private Configuration stringConfig;
    private Configuration intConfig;
    private Configuration boolConfig;
    @SuppressWarnings("unused") // Field used for test data setup
    private Configuration inactiveConfig;

    @BeforeEach
    void setUp() {
        stringConfig = Configuration.builder()
                .id(1L)
                .key("string.key")
                .value("string value")
                .description("String config")
                .status(CommonStatus.ACTIVE)
                .build();

        intConfig = Configuration.builder()
                .id(2L)
                .key("int.key")
                .value("42")
                .description("Integer config")
                .status(CommonStatus.ACTIVE)
                .build();

        boolConfig = Configuration.builder()
                .id(3L)
                .key("bool.key")
                .value("true")
                .description("Boolean config")
                .status(CommonStatus.ACTIVE)
                .build();

        inactiveConfig = Configuration.builder()
                .id(4L)
                .key("inactive.key")
                .value("inactive value")
                .description("Inactive config")
                .status(CommonStatus.INACTIVE)
                .build();
    }

    @Test
    void findByKey_ShouldReturnConfiguration_WhenExists() {
        // Given
        when(configurationRepository.findByKey("string.key")).thenReturn(Optional.of(stringConfig));

        // When
        Optional<Configuration> result = configurationService.findByKey("string.key");

        // Then
        assertTrue(result.isPresent());
        assertEquals("string value", result.get().getValue());
        verify(configurationRepository).findByKey("string.key");
    }

    @Test
    void saveOrUpdate_ShouldUpdateExistingConfiguration() {
        // Given
        when(configurationRepository.findByKey("string.key")).thenReturn(Optional.of(stringConfig));
        when(configurationRepository.save(any(Configuration.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Configuration result = configurationService.saveOrUpdate("string.key", "new value", "Updated description");

        // Then
        assertEquals("new value", result.getValue());
        assertEquals("Updated description", result.getDescription());
        verify(configurationRepository).findByKey("string.key");
        verify(configurationRepository).save(stringConfig);
    }

    @Test
    void saveOrUpdate_ShouldCreateNewConfiguration() {
        // Given
        when(configurationRepository.findByKey("new.key")).thenReturn(Optional.empty());
        when(configurationRepository.save(any(Configuration.class))).thenAnswer(invocation -> {
            Configuration config = invocation.getArgument(0);
            config.setId(5L);
            return config;
        });

        // When
        Configuration result = configurationService.saveOrUpdate("new.key", "new value", "New description");

        // Then
        assertEquals("new.key", result.getKey());
        assertEquals("new value", result.getValue());
        assertEquals("New description", result.getDescription());
        assertEquals(CommonStatus.ACTIVE, result.getStatus());
        verify(configurationRepository).findByKey("new.key");
        verify(configurationRepository).save(any(Configuration.class));
    }

    @Test
    void getValue_ShouldReturnValue_WhenConfigExists() {
        // Given
        when(configurationRepository.findByKey("string.key")).thenReturn(Optional.of(stringConfig));

        // When
        String result = configurationService.getValue("string.key");

        // Then
        assertEquals("string value", result);
        verify(configurationRepository).findByKey("string.key");
    }

    @Test
    void getValue_ShouldReturnNull_WhenConfigDoesNotExist() {
        // Given
        when(configurationRepository.findByKey("nonexistent.key")).thenReturn(Optional.empty());

        // When
        String result = configurationService.getValue("nonexistent.key");

        // Then
        assertNull(result);
        verify(configurationRepository).findByKey("nonexistent.key");
    }

    @Test
    void getValue_WithDefault_ShouldReturnValue_WhenConfigExists() {
        // Given
        when(configurationRepository.findByKey("string.key")).thenReturn(Optional.of(stringConfig));

        // When
        String result = configurationService.getValue("string.key", "default");

        // Then
        assertEquals("string value", result);
        verify(configurationRepository).findByKey("string.key");
    }

    @Test
    void getValue_WithDefault_ShouldReturnDefault_WhenConfigDoesNotExist() {
        // Given
        when(configurationRepository.findByKey("nonexistent.key")).thenReturn(Optional.empty());

        // When
        String result = configurationService.getValue("nonexistent.key", "default");

        // Then
        assertEquals("default", result);
        verify(configurationRepository).findByKey("nonexistent.key");
    }

    @Test
    void getIntValue_ShouldReturnIntValue_WhenConfigExists() {
        // Given
        when(configurationRepository.findByKey("int.key")).thenReturn(Optional.of(intConfig));

        // When
        Integer result = configurationService.getIntValue("int.key", 0);

        // Then
        assertEquals(42, result);
        verify(configurationRepository).findByKey("int.key");
    }

    @Test
    void getIntValue_ShouldReturnDefault_WhenConfigDoesNotExist() {
        // Given
        when(configurationRepository.findByKey("nonexistent.key")).thenReturn(Optional.empty());

        // When
        Integer result = configurationService.getIntValue("nonexistent.key", 123);

        // Then
        assertEquals(123, result);
        verify(configurationRepository).findByKey("nonexistent.key");
    }

    @Test
    void getIntValue_ShouldReturnDefault_WhenValueNotInteger() {
        // Given
        when(configurationRepository.findByKey("string.key")).thenReturn(Optional.of(stringConfig));

        // When
        Integer result = configurationService.getIntValue("string.key", 123);

        // Then
        assertEquals(123, result);
        verify(configurationRepository).findByKey("string.key");
    }

    @Test
    void getBooleanValue_ShouldReturnBooleanValue_WhenConfigExists() {
        // Given
        when(configurationRepository.findByKey("bool.key")).thenReturn(Optional.of(boolConfig));

        // When
        Boolean result = configurationService.getBooleanValue("bool.key", false);

        // Then
        assertTrue(result);
        verify(configurationRepository).findByKey("bool.key");
    }

    @Test
    void getBooleanValue_ShouldHandleVariousFormats() {
        // Given
        Configuration yesConfig = Configuration.builder().key("yes.key").value("yes").status(CommonStatus.ACTIVE).build();
        Configuration noConfig = Configuration.builder().key("no.key").value("no").status(CommonStatus.ACTIVE).build();
        Configuration oneConfig = Configuration.builder().key("one.key").value("1").status(CommonStatus.ACTIVE).build();
        Configuration zeroConfig = Configuration.builder().key("zero.key").value("0").status(CommonStatus.ACTIVE).build();
        
        when(configurationRepository.findByKey("yes.key")).thenReturn(Optional.of(yesConfig));
        when(configurationRepository.findByKey("no.key")).thenReturn(Optional.of(noConfig));
        when(configurationRepository.findByKey("one.key")).thenReturn(Optional.of(oneConfig));
        when(configurationRepository.findByKey("zero.key")).thenReturn(Optional.of(zeroConfig));

        // When & Then
        assertTrue(configurationService.getBooleanValue("yes.key", false));
        assertFalse(configurationService.getBooleanValue("no.key", true));
        assertTrue(configurationService.getBooleanValue("one.key", false));
        assertFalse(configurationService.getBooleanValue("zero.key", true));
        
        verify(configurationRepository).findByKey("yes.key");
        verify(configurationRepository).findByKey("no.key");
        verify(configurationRepository).findByKey("one.key");
        verify(configurationRepository).findByKey("zero.key");
    }

    @Test
    void getBooleanValue_ShouldReturnDefault_WhenConfigDoesNotExist() {
        // Given
        when(configurationRepository.findByKey("nonexistent.key")).thenReturn(Optional.empty());

        // When
        Boolean result = configurationService.getBooleanValue("nonexistent.key", true);

        // Then
        assertTrue(result);
        verify(configurationRepository).findByKey("nonexistent.key");
    }

    @Test
    void getBooleanValue_ShouldReturnDefault_WhenValueNotBoolean() {
        // Given
        when(configurationRepository.findByKey("string.key")).thenReturn(Optional.of(stringConfig));

        // When
        Boolean result = configurationService.getBooleanValue("string.key", true);

        // Then
        assertTrue(result);
        verify(configurationRepository).findByKey("string.key");
    }

    @Test
    void getAllValues_ShouldReturnMapOfValues() {
        // Given
        when(configurationRepository.findByStatus(CommonStatus.ACTIVE))
                .thenReturn(Arrays.asList(stringConfig, intConfig, boolConfig));

        // When
        Map<String, String> result = configurationService.getAllValues();

        // Then
        assertEquals(3, result.size());
        assertEquals("string value", result.get("string.key"));
        assertEquals("42", result.get("int.key"));
        assertEquals("true", result.get("bool.key"));
        verify(configurationRepository).findByStatus(CommonStatus.ACTIVE);
    }

    @Test
    void findByKeyContaining_ShouldReturnMatchingConfigs() {
        // Given
        when(configurationRepository.findByKeyContainingIgnoreCase("key"))
                .thenReturn(Arrays.asList(stringConfig, intConfig));

        // When
        List<Configuration> result = configurationService.findByKeyContaining("key");

        // Then
        assertEquals(2, result.size());
        verify(configurationRepository).findByKeyContainingIgnoreCase("key");
    }

    @Test
    void deleteByKey_ShouldDeactivateConfig() {
        // Given
        when(configurationRepository.findByKey("string.key")).thenReturn(Optional.of(stringConfig));
        when(configurationRepository.save(any(Configuration.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        configurationService.deleteByKey("string.key");

        // Then
        assertEquals(CommonStatus.INACTIVE, stringConfig.getStatus());
        verify(configurationRepository).findByKey("string.key");
        verify(configurationRepository).save(stringConfig);
    }

    @Test
    void deleteByKey_ShouldDoNothing_WhenConfigDoesNotExist() {
        // Given
        when(configurationRepository.findByKey("nonexistent.key")).thenReturn(Optional.empty());

        // When
        configurationService.deleteByKey("nonexistent.key");

        // Then
        verify(configurationRepository).findByKey("nonexistent.key");
        verify(configurationRepository, never()).save(any());
    }
}
