package vn.com.fecredit.app.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ConfigurationTest {

    @Test
    void testConfigurationProperties() {
        Configuration config = Configuration.builder()
                                            .key("testKey")
                                            .value("testValue")
                                            .description("Test config")
                                            .build();

        assertEquals("testKey", config.getKey());
        assertEquals("testValue", config.getValue());
        assertEquals("Test config", config.getDescription());
    }
}