package vn.com.fecredit.app.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.EntityManager;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.entity.enums.CommonStatus;
import vn.com.fecredit.app.repository.RoleRepository;
import vn.com.fecredit.app.repository.SimpleObjectRepository;
import vn.com.fecredit.app.service.dto.FetchStatus;
import vn.com.fecredit.app.service.dto.FilterRequest;
import vn.com.fecredit.app.service.dto.FilterType;
import vn.com.fecredit.app.service.dto.ObjectType;
import vn.com.fecredit.app.service.dto.SortRequest;
import vn.com.fecredit.app.service.dto.SortType;
import vn.com.fecredit.app.service.dto.TableFetchRequest;
import vn.com.fecredit.app.service.dto.TableFetchResponse;
import vn.com.fecredit.app.service.factory.RelatedTablesFactory;
import vn.com.fecredit.app.service.factory.RepositoryFactory;
import vn.com.fecredit.app.service.validator.TableFetchRequestValidator;

@ExtendWith(MockitoExtension.class)
public class TableDataServiceImplTest {

        @Mock
        private EntityManager entityManager;

        @Mock
        private RepositoryFactory repositoryFactory;

        @Mock
        private RelatedTablesFactory relatedTablesFactory;

        @Mock
        private SimpleObjectRepository<User> userRepository;

        @Mock
        private SimpleObjectRepository<Event> eventRepository;

        @Mock
        private RoleRepository roleRepository;

        @Mock
        private TableFetchRequestValidator validator;

        @InjectMocks
        private TableDataServiceImpl tableDataService;

        @Captor
        private ArgumentCaptor<Specification<User>> userSpecCaptor;

        @Captor
        private ArgumentCaptor<Specification<Role>> roleSpecCaptor;

        @Captor
        private ArgumentCaptor<Pageable> pageableCaptor;

        private User testUser;
        private Event testEvent;
        private List<User> testUsers;
        private List<Event> testEvents;

        @BeforeEach
        void setUp() {
                // Create test user data
                testUser = new User();
                testUser.setId(1L);
                testUser.setUsername("testuser");
                testUser.setEmail("test@example.com");
                testUser.setFullName("Test User");
                testUser.setStatus(CommonStatus.ACTIVE);
                testUsers = Arrays.asList(testUser);

                // Create test event data
                testEvent = new Event();
                testEvent.setId(1L);
                testEvent.setName("Test Event");
                testEvent.setCode("EVT-001");
                testEvent.setDescription("Test event description");
                testEvent.setStatus(CommonStatus.ACTIVE);
                testEvents = Arrays.asList(testEvent);
        }

        // ======== Basic Functionality Tests ========

        @Test
        void fetchData_WithNullRequest_ShouldReturnErrorResponse() {
                // Act
                TableFetchResponse response = tableDataService.fetchData(null);

                // Assert - EXACT message from implementation
                assertEquals(FetchStatus.ERROR, response.getStatus());
                assertEquals("Request cannot be null", response.getMessage());
        }

        @Test
        void fetchData_WithNoObjectTypeOrEntity_ShouldReturnErrorResponse() {
                // Arrange
                TableFetchRequest request = new TableFetchRequest();

                // Act
                TableFetchResponse response = tableDataService.fetchData(request);

                // Assert - EXACT message from implementation
                assertEquals(FetchStatus.ERROR, response.getStatus());
                assertEquals("No object type or entity name specified", response.getMessage());
        }

        @Test
        void fetchData_ForUserEntity_ShouldReturnUserData() {
                // Arrange
                TableFetchRequest request = TableFetchRequest.builder()
                                .objectType(ObjectType.User)
                                .page(0)
                                .size(10)
                                .build();

                Page<User> userPage = new PageImpl<>(testUsers);

                // Setup repository factory mocks
                when(repositoryFactory.getEntityClass(ObjectType.User)).thenReturn((Class) User.class);
                when(repositoryFactory.getRepositoryForClass(User.class)).thenReturn(userRepository);
                when(repositoryFactory.getTableNameForObjectType(ObjectType.User)).thenReturn("users");

                // Setup repository mocks
                when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);

                // Act
                TableFetchResponse response = tableDataService.fetchData(request);

                // Assert
                assertEquals(FetchStatus.SUCCESS, response.getStatus());
                assertEquals("users", response.getTableName());
                assertEquals(1, response.getTotalElements());
                assertEquals(1, response.getRows().size());
                assertEquals("testuser", response.getRows().get(0).getData().get("username"));

                // Verify interactions
                verify(repositoryFactory, atLeastOnce()).getEntityClass(ObjectType.User);
                verify(repositoryFactory).getRepositoryForClass(User.class);
                verify(repositoryFactory).getTableNameForObjectType(ObjectType.User);
                verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        void fetchData_ForEventEntity_ShouldReturnEventData() {
                // Arrange
                TableFetchRequest request = TableFetchRequest.builder()
                                .objectType(ObjectType.Event)
                                .page(0)
                                .size(10)
                                .build();

                Page<Event> eventPage = new PageImpl<>(testEvents);

                // Setup repository factory mocks
                when(repositoryFactory.getEntityClass(ObjectType.Event)).thenReturn((Class) Event.class);
                when(repositoryFactory.getRepositoryForClass(Event.class)).thenReturn(eventRepository);
                when(repositoryFactory.getTableNameForObjectType(ObjectType.Event)).thenReturn("events");

                // Setup repository mocks
                when(eventRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(eventPage);
                when(relatedTablesFactory.hasRelatedTables(any(Event.class))).thenReturn(false);

                // Act
                TableFetchResponse response = tableDataService.fetchData(request);

                // Assert
                assertEquals(FetchStatus.SUCCESS, response.getStatus());
                assertEquals("events", response.getTableName());
                assertEquals(1, response.getTotalElements());
                assertEquals(1, response.getRows().size());
                assertEquals("Test Event", response.getRows().get(0).getData().get("name"));
        }

        @Test
        void fetchData_WithEntityName_ShouldConvertToObjectType() {
                // Arrange
                TableFetchRequest request = TableFetchRequest.builder()
                                .entityName("User")
                                .page(0)
                                .size(10)
                                .build();

                Page<User> userPage = new PageImpl<>(testUsers);

                // Setup repository factory mocks
                when(repositoryFactory.getEntityClass(ObjectType.User)).thenReturn((Class) User.class);
                when(repositoryFactory.getRepositoryForClass(User.class)).thenReturn(userRepository);
                when(repositoryFactory.getTableNameForObjectType(ObjectType.User)).thenReturn("users");

                // Setup repository mocks
                when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);
                when(relatedTablesFactory.hasRelatedTables(any(User.class))).thenReturn(false);

                // Act
                TableFetchResponse response = tableDataService.fetchData(request);

                // Assert
                assertEquals(FetchStatus.SUCCESS, response.getStatus());
                assertEquals("users", response.getTableName());
        }

        @Test
        void fetchData_WithInvalidEntityName_ShouldReturnErrorResponse() {
                // Arrange
                TableFetchRequest request = TableFetchRequest.builder()
                                .entityName("InvalidEntity")
                                .page(0)
                                .size(10)
                                .build();

                // Act
                TableFetchResponse response = tableDataService.fetchData(request);

                // Assert - EXACT message from implementation
                assertEquals(FetchStatus.ERROR, response.getStatus());
                assertEquals("Unsupported entity: InvalidEntity", response.getMessage());
        }

        // ======== Filtering and Sorting Tests ========

        @Test
        void fetchData_WithFiltering_ShouldApplyFilterSpecification() {
                // Arrange
                FilterRequest filter = new FilterRequest("username", FilterType.EQUALS, "testuser");

                TableFetchRequest request = TableFetchRequest.builder()
                                .objectType(ObjectType.User)
                                .page(0)
                                .size(10)
                                .filters(Arrays.asList(filter))
                                .build();

                Page<User> userPage = new PageImpl<>(testUsers);

                // Setup mocks
                when(repositoryFactory.getEntityClass(ObjectType.User)).thenReturn((Class) User.class);
                when(repositoryFactory.getRepositoryForClass(User.class)).thenReturn(userRepository);
                when(repositoryFactory.getTableNameForObjectType(ObjectType.User)).thenReturn("users");
                when(userRepository.findAll(userSpecCaptor.capture(), any(Pageable.class))).thenReturn(userPage);
                when(relatedTablesFactory.hasRelatedTables(any(User.class))).thenReturn(false);

                // Act
                TableFetchResponse response = tableDataService.fetchData(request);

                // Assert
                assertEquals(FetchStatus.SUCCESS, response.getStatus());
                assertEquals(1, response.getRows().size());

                // We can't directly test the specification logic since it's a functional
                // interface,
                // but we can verify it was passed to the repository
                assertNotNull(userSpecCaptor.getValue());
        }

        @Test
        void fetchData_WithSorting_ShouldApplySortCriteria() {
                // Arrange
                SortRequest sort = new SortRequest("username", SortType.ASCENDING);

                // TODO: Complete this test
        }

        // ======== Special Cases Tests ========

        @Test
        void fetchData_WithEmptyResult_ShouldReturnNoDataStatus() {
                // Arrange
                TableFetchRequest request = TableFetchRequest.builder()
                                .objectType(ObjectType.User)
                                .page(0)
                                .size(10)
                                .build();

                Page<User> emptyPage = new PageImpl<>(Collections.emptyList());

                // Setup mocks
                when(repositoryFactory.getEntityClass(ObjectType.User)).thenReturn((Class) User.class);
                when(repositoryFactory.getRepositoryForClass(User.class)).thenReturn(userRepository);
                when(repositoryFactory.getTableNameForObjectType(ObjectType.User)).thenReturn("users");
                when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(emptyPage);

                // Act
                TableFetchResponse response = tableDataService.fetchData(request);

                // Assert
                assertEquals(FetchStatus.NO_DATA, response.getStatus());
                assertEquals(0, response.getTotalElements());
                assertTrue(response.getRows().isEmpty());
        }

        @Test
        void fetchData_WithRepositoryException_ShouldReturnErrorResponse() {
                // Arrange
                TableFetchRequest request = TableFetchRequest.builder()
                                .objectType(ObjectType.User)
                                .page(0)
                                .size(10)
                                .build();

                // Setup mocks
                when(repositoryFactory.getEntityClass(ObjectType.User)).thenReturn((Class) User.class);
                when(repositoryFactory.getRepositoryForClass(User.class)).thenReturn(userRepository);
                when(repositoryFactory.getTableNameForObjectType(ObjectType.User)).thenReturn("users");
                when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                                .thenThrow(new RuntimeException("Test exception"));

                // Act
                TableFetchResponse response = tableDataService.fetchData(request);

                // Assert - EXACT message from implementation
                assertEquals(FetchStatus.ERROR, response.getStatus());
                assertEquals("Error executing query: Test exception", response.getMessage());
        }

        @Test
        void fetchData_WithEntityClassNotFoundException_ShouldReturnErrorResponse() {
                // Arrange
                TableFetchRequest request = TableFetchRequest.builder()
                                .objectType(ObjectType.User)
                                .page(0)
                                .size(10)
                                .build();

                // Setup mocks
                when(repositoryFactory.getEntityClass(ObjectType.User))
                                .thenThrow(new IllegalArgumentException("Entity class not found"));

                // Act
                TableFetchResponse response = tableDataService.fetchData(request);

                // Assert - EXACT message from implementation
                assertEquals(FetchStatus.ERROR, response.getStatus());
                assertEquals("Error getting entity class for object type: User", response.getMessage());
        }

        @Test
        void fetchData_WithEntityClassNull_ShouldReturnErrorResponse() {
                // Arrange
                TableFetchRequest request = TableFetchRequest.builder()
                                .objectType(ObjectType.User)
                                .page(0)
                                .size(10)
                                .build();

                // Setup mocks
                when(repositoryFactory.getEntityClass(ObjectType.User)).thenReturn(null);

                // Act
                TableFetchResponse response = tableDataService.fetchData(request);

                // Assert
                assertEquals(FetchStatus.ERROR, response.getStatus());
                assertEquals("Entity class not found for object type: User", response.getMessage());
        }

        @Test
        void fetchData_WithRepositoryNotFoundException_ShouldReturnErrorResponse() {
                // Arrange
                TableFetchRequest request = TableFetchRequest.builder()
                                .objectType(ObjectType.User)
                                .page(0)
                                .size(10)
                                .build();

                // Setup mocks
                when(repositoryFactory.getEntityClass(ObjectType.User)).thenReturn((Class) User.class);
                when(repositoryFactory.getRepositoryForClass(User.class))
                                .thenThrow(new IllegalArgumentException("Repository not found"));

                // Act
                TableFetchResponse response = tableDataService.fetchData(request);

                // Assert
                assertEquals(FetchStatus.ERROR, response.getStatus());
                assertEquals("Error getting repository for entity class: vn.com.fecredit.app.entity.User",
                                response.getMessage());
        }

        @Test
        void fetchData_WithRepositoryNull_ShouldReturnErrorResponse() {
                // Arrange
                TableFetchRequest request = TableFetchRequest.builder()
                                .objectType(ObjectType.User)
                                .page(0)
                                .size(10)
                                .build();

                // Setup mocks
                when(repositoryFactory.getEntityClass(ObjectType.User)).thenReturn((Class) User.class);
                when(repositoryFactory.getRepositoryForClass(User.class)).thenReturn(null);

                // Act
                TableFetchResponse response = tableDataService.fetchData(request);

                // Assert
                assertEquals(FetchStatus.ERROR, response.getStatus());
                assertEquals("Repository not found for entity class: vn.com.fecredit.app.entity.User",
                                response.getMessage());
        }


}
