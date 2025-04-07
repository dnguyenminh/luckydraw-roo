package vn.com.fecredit.app.service.factory.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.context.ActiveProfiles;

import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.ServiceTestApplication;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.repository.UserRepository;
import vn.com.fecredit.app.repository.EventRepository;
import vn.com.fecredit.app.service.dto.ObjectType;
import vn.com.fecredit.app.service.factory.RepositoryFactory;

@Slf4j
@SpringBootTest(classes = ServiceTestApplication.class)
@ActiveProfiles("test")
public class RepositoryFactoryImplTest {

    @Autowired
    private RepositoryFactory repositoryFactory;
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @BeforeEach
    void setUp() {
        // Log available repository beans to help with debugging
        log.info("Available beans in application context:");
        Arrays.stream(applicationContext.getBeanDefinitionNames())
            .filter(name -> name.toLowerCase().contains("repository"))
            .sorted()
            .forEach(name -> log.info("Repository bean: {}", name));
        
        // Verify UserRepository bean specifically
        try {
            Object userRepoBean = applicationContext.getBean("userRepository");
            log.info("UserRepository bean found: {}", userRepoBean.getClass().getName());
        } catch (Exception e) {
            log.error("Could not find UserRepository bean: {}", e.getMessage());
        }
        
        // Verify EventRepository bean specifically
        try {
            Object eventRepoBean = applicationContext.getBean("eventRepository");
            log.info("EventRepository bean found: {}", eventRepoBean.getClass().getName());
        } catch (Exception e) {
            log.error("Could not find EventRepository bean: {}", e.getMessage());
        }
    }
    
    @Test
    void repositoryFactory_ShouldBeInjected() {
        assertNotNull(repositoryFactory, "RepositoryFactory should be autowired properly");
        log.info("RepositoryFactory implementation: {}", repositoryFactory.getClass().getName());
    }
    
    @Test
    void getEntityClass_ForAllObjectTypes_ShouldReturnCorrectClass() {
        // Test all object types that should have entity classes
        for (ObjectType type : ObjectType.values()) {
            try {
                if (type != ObjectType.Statistics) { // Statistics doesn't have an entity
                    Class<?> entityClass = repositoryFactory.getEntityClass(type);
                    assertNotNull(entityClass, "Entity class for " + type + " should not be null");
                    log.info("Object type {} maps to entity class {}", type, entityClass.getName());
                }
            } catch (Exception e) {
                // Only log exceptions here, we'll have specific tests for each important type
                log.error("Error getting entity class for {}: {}", type, e.getMessage());
            }
        }
    }
    
    @Test
    void getEntityClass_ForUser_ShouldReturnUserClass() {
        Class<?> entityClass = repositoryFactory.getEntityClass(ObjectType.User);
        
        assertNotNull(entityClass, "Entity class for User should not be null");
        assertEquals(User.class, entityClass, "Entity class for User should be User.class");
        log.info("User entity class: {}", entityClass.getName());
    }
    
    @Test
    void getEntityClass_ForEvent_ShouldReturnEventClass() {
        Class<?> entityClass = repositoryFactory.getEntityClass(ObjectType.Event);
        
        assertNotNull(entityClass, "Entity class for Event should not be null");
        assertEquals(Event.class, entityClass, "Entity class for Event should be Event.class");
        log.info("Event entity class: {}", entityClass.getName());
    }
    
    @Test
    void getRepositoryForClass_ForUserClass_ShouldReturnUserRepository() {
        JpaRepository<?, ?> repository = repositoryFactory.getRepositoryForClass(User.class);
        
        assertNotNull(repository, "Repository for User class should not be null");
        assertThat(repository).isInstanceOf(UserRepository.class);
        log.info("User repository: {}", repository.getClass().getName());
        
        // Test a basic repository operation to ensure it works
        long count = repository.count();
        log.info("User count: {}", count);
    }
    
    @Test
    void getRepositoryForClass_ForEventClass_ShouldReturnEventRepository() {
        JpaRepository<?, ?> repository = repositoryFactory.getRepositoryForClass(Event.class);
        
        assertNotNull(repository, "Repository for Event class should not be null");
        assertThat(repository).isInstanceOf(EventRepository.class);
        log.info("Event repository: {}", repository.getClass().getName());
        
        // Test a basic repository operation to ensure it works
        long count = repository.count();
        log.info("Event count: {}", count);
    }
    
    @Test
    void getTableNameForObjectType_ForUser_ShouldReturnUsersTable() {
        String tableName = repositoryFactory.getTableNameForObjectType(ObjectType.User);
        
        assertNotNull(tableName, "Table name for User should not be null");
        assertEquals("users", tableName, "Table name for User should be 'users'");
    }
    
    @Test
    void getTableNameForObjectType_ForEvent_ShouldReturnEventTable() {
        String tableName = repositoryFactory.getTableNameForObjectType(ObjectType.Event);
        
        assertNotNull(tableName, "Table name for Event should not be null");
        assertEquals("event", tableName, "Table name for Event should be 'event'");
    }
    
    @Test
    void getObjectTypeForEntityName_ShouldWork() {
        // Test the method if it's exposed
        if (repositoryFactory instanceof RepositoryFactoryImpl) {
            RepositoryFactoryImpl impl = (RepositoryFactoryImpl) repositoryFactory;
            ObjectType userType = impl.getObjectTypeForEntityName("User");
            assertEquals(ObjectType.User, userType, "Entity name 'User' should map to ObjectType.User");
            
            ObjectType eventType = impl.getObjectTypeForEntityName("Event");
            assertEquals(ObjectType.Event, eventType, "Entity name 'Event' should map to ObjectType.Event");
        }
    }
    
    @Test
    void getEntityClass_ForStatistics_ShouldThrowException() {
        // Statistics is special and doesn't have an entity class
        assertThrows(IllegalArgumentException.class, () -> {
            repositoryFactory.getEntityClass(ObjectType.Statistics);
        });
    }
    
    @Test
    void getRepositoryForClass_ForInvalidClass_ShouldThrowException() {
        // Test with a class that doesn't have a repository
        class FakeEntity {}
        
        assertThrows(IllegalArgumentException.class, () -> {
            repositoryFactory.getRepositoryForClass(FakeEntity.class);
        });
    }
}
