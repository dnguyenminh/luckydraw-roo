package vn.com.fecredit.app.service.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.HashSet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import vn.com.fecredit.app.ServiceTestApplication;
import vn.com.fecredit.app.entity.Permission;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.entity.Province;
import vn.com.fecredit.app.entity.enums.CommonStatus;
import vn.com.fecredit.app.entity.enums.RoleType;
import vn.com.fecredit.app.service.dto.DataObject;
import vn.com.fecredit.app.service.dto.ObjectType;

@SpringBootTest(classes = ServiceTestApplication.class)
@ActiveProfiles("test")
public class EntityConverterTest {

    @Autowired
    private EntityConverter entityConverter;

    @Test
    void testConvertRoleToDataObject() {
        // Create a test role entity
        Role role = Role.builder()
                .id(100L)
                .roleType(RoleType.ROLE_ADMIN)
                .description("Admin role for testing")
                .displayOrder(1)
                .permissions(new HashSet<>())
                .users(new HashSet<>())
                .status(CommonStatus.ACTIVE)
                .build();

        // Convert to DataObject
        DataObject dataObject = entityConverter.convertToDataObject(role);

        // Verify conversion
        assertNotNull(dataObject, "DataObject should not be null");
        assertEquals(ObjectType.Role, dataObject.getObjectType(), "Object type should be Role");
        assertNotNull(dataObject.getData(), "TableRow should not be null");
        assertNotNull(dataObject.getData().getData(), "Data map should not be null");

        // Verify data content
        assertEquals(100L, dataObject.getData().getData().get("id"), "ID should match");
        assertEquals("ROLE_ADMIN", dataObject.getData().getData().get("roleType"), "RoleType should match");
        assertEquals("ACTIVE", dataObject.getData().getData().get("status"), "Status should match");
        assertEquals("Admin role for testing", dataObject.getData().getData().get("description"),
                "Description should match");
        assertEquals(1, dataObject.getData().getData().get("displayOrder"), "Display order should match");
    }

    @Test
    void testConvertPermissionToDataObject() {
        // Create a test permission entity
        Permission permission = Permission.builder()
                .id(200L)
                .name("TEST_PERMISSION")
                .description("Test permission description")
                .roles(new HashSet<>())
                .status(CommonStatus.ACTIVE)
                .build();

        // Convert to DataObject
        DataObject dataObject = entityConverter.convertToDataObject(permission);

        // Verify conversion
        assertNotNull(dataObject, "DataObject should not be null");
        assertEquals(ObjectType.Permission, dataObject.getObjectType(), "Object type should be Permission");
        assertNotNull(dataObject.getData(), "TableRow should not be null");
        assertNotNull(dataObject.getData().getData(), "Data map should not be null");

        // Verify data content
        assertEquals(200L, dataObject.getData().getData().get("id"), "ID should match");
        assertEquals("TEST_PERMISSION", dataObject.getData().getData().get("name"), "Name should match");
        assertEquals("Test permission description", dataObject.getData().getData().get("description"),
                "Description should match");
        assertEquals("ACTIVE", dataObject.getData().getData().get("status"), "Status should match");
    }

    @Test
    void testConvertUserWithRelatedObjects() {
        // Create a test role
        Role role = Role.builder()
                .id(101L)
                .roleType(RoleType.ROLE_ADMIN)
                .status(CommonStatus.ACTIVE)
                .build();

        // Create a test user entity with related objects
        User user = User.builder()
                .id(300L)
                .username("testuser")
                .password("$2a$10$encrypted")
                .email("test@example.com")
                .fullName("Test User")
                .enabled(true)
                .role(RoleType.ROLE_ADMIN)
                .status(CommonStatus.ACTIVE)
                .roles(new HashSet<>())
                .build();

        // Add role to user's roles collection
        // Set the roles collection first to avoid NPE
        user.setRoles(new HashSet<>());
        user.getRoles().add(role);

        // Convert to DataObject
        DataObject dataObject = entityConverter.convertToDataObject(user);

        // Verify conversion
        assertNotNull(dataObject, "DataObject should not be null");
        assertEquals(ObjectType.User, dataObject.getObjectType(), "Object type should be User");
        assertNotNull(dataObject.getData(), "TableRow should not be null");
        assertNotNull(dataObject.getData().getData(), "Data map should not be null");

        // Verify data content
        assertEquals(300L, dataObject.getData().getData().get("id"), "ID should match");
        assertEquals("testuser", dataObject.getData().getData().get("username"), "Username should match");
        assertEquals("test@example.com", dataObject.getData().getData().get("email"), "Email should match");
        assertEquals("Test User", dataObject.getData().getData().get("fullName"), "Full name should match");
        assertEquals(true, dataObject.getData().getData().get("enabled"), "Enabled flag should match");
        assertEquals("ACTIVE", dataObject.getData().getData().get("status"), "Status should match");
        assertEquals("ROLE_ADMIN", dataObject.getData().getData().get("role"), "Role should match");
    }

    @Test
    void testConvertEntityWithDates() {
        // Create an entity with date fields
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime laterDate = now.plusDays(1);

        Event event = Event.builder()
                .id(400L)
                .name("Test Event")
                .code("TEST-EVENT")
                .description("Test event with dates")
                .startTime(now)
                .endTime(laterDate)
                .status(CommonStatus.ACTIVE)
                .build();

        // Convert to DataObject
        DataObject dataObject = entityConverter.convertToDataObject(event);

        // Verify conversion
        assertNotNull(dataObject, "DataObject should not be null");
        assertEquals(ObjectType.Event, dataObject.getObjectType(), "Object type should be Event");
        assertNotNull(dataObject.getData(), "TableRow should not be null");

        // Verify basic data fields
        assertEquals(400L, dataObject.getData().getData().get("id"), "ID should match");
        assertEquals("Test Event", dataObject.getData().getData().get("name"), "Name should match");
        assertEquals("TEST-EVENT", dataObject.getData().getData().get("code"), "Code should match");
        assertEquals("Test event with dates", dataObject.getData().getData().get("description"),
                "Description should match");
        assertEquals("ACTIVE", dataObject.getData().getData().get("status"), "Status should match");

        // Verify date fields are present
        assertNotNull(dataObject.getData().getData().get("startTime"), "Start time field should be included");
        assertNotNull(dataObject.getData().getData().get("endTime"), "End time field should be included");
    }

    @Test
    void testConvertEntityWithNestedStructure() {
        // Create a region
        Region region = Region.builder()
                .provinces(new HashSet<>())
                .eventLocations(new HashSet<>())
                .id(500L)
                .name("Test Region")
                .code("TR")
                .provinces(new HashSet<>())
                .eventLocations(new HashSet<>())
                .description("Test region description")
                .status(CommonStatus.ACTIVE)
                .provinces(new HashSet<>())
                .eventLocations(new HashSet<>())
                .build();

        // Initialize collections to prevent NullPointerException
        region.setProvinces(new HashSet<>());
        region.setEventLocations(new HashSet<>());

        // Create a province that references the region
        Province province = Province.builder()
                .id(600L)
                .name("Test Province")
                .code("TP")
                .description("Test province description")
                .region(region)
                .status(CommonStatus.ACTIVE)
                .participants(new HashSet<>())
                .build();

        // Initialize participants collection
        province.setParticipants(new HashSet<>());

        // Update bidirectional relationship
        region.getProvinces().add(province);

        // Convert province to DataObject
        DataObject dataObject = entityConverter.convertToDataObject(province);

        // Verify conversion
        assertNotNull(dataObject, "DataObject should not be null");
        assertEquals(ObjectType.Province, dataObject.getObjectType(), "Object type should be Province");

        // Verify data content
        assertEquals(600L, dataObject.getData().getData().get("id"), "Province ID should match");
        assertEquals("Test Province", dataObject.getData().getData().get("name"), "Province name should match");
        assertEquals("TP", dataObject.getData().getData().get("code"), "Province code should match");
        assertEquals("Test province description", dataObject.getData().getData().get("description"),
                "Province description should match");
        assertEquals("ACTIVE", dataObject.getData().getData().get("status"), "Status should match");

        // Verify nested region was converted properly - checking for regionId and
        // regionName fields
        assertEquals(500L, dataObject.getData().getData().get("regionId"), "Region ID should be extracted");
        assertEquals("Test Region", dataObject.getData().getData().get("regionName"),
                "Region name should be extracted");
    }

    @Test
    void testConvertWithExplicitObjectType() {
        // Create a custom object that doesn't match any predefined entity
        CustomTestEntity customEntity = new CustomTestEntity();
        customEntity.setId(700L);
        customEntity.setName("Custom Entity");
        customEntity.setValue(42);
        customEntity.setDescription("Test custom entity");
        customEntity.setActive(true);

        // Convert using explicit ObjectType
        DataObject dataObject = entityConverter.convertToDataObject(customEntity, ObjectType.Configuration);

        // Verify conversion with explicit type
        assertNotNull(dataObject, "DataObject should not be null");
        assertEquals(ObjectType.Configuration, dataObject.getObjectType(), "Object type should be Configuration");

        // Verify data content
        assertEquals(700L, dataObject.getData().getData().get("id"), "ID should match");
        assertEquals("Custom Entity", dataObject.getData().getData().get("name"), "Name should match");
        assertEquals(42, dataObject.getData().getData().get("value"), "Value should match");
        assertEquals("Test custom entity", dataObject.getData().getData().get("description"),
                "Description should match");
        assertEquals(true, dataObject.getData().getData().get("active"), "Active flag should match");
    }

    // Custom test entity class
    static class CustomTestEntity {
        private Long id;
        private String name;
        private int value;
        private String description;
        private boolean active;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }
}
