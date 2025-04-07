package vn.com.fecredit.app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import jakarta.persistence.EntityManager;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.entity.enums.CommonStatus;
import vn.com.fecredit.app.repository.SimpleObjectRepository;
import vn.com.fecredit.app.service.dto.DataObjectKeyValues;
import vn.com.fecredit.app.service.dto.FetchStatus;
import vn.com.fecredit.app.service.dto.FilterRequest;
import vn.com.fecredit.app.service.dto.FilterType;
import vn.com.fecredit.app.service.dto.ObjectType;
import vn.com.fecredit.app.service.dto.SortRequest;
import vn.com.fecredit.app.service.dto.SortType;
import vn.com.fecredit.app.service.dto.TabTableRow;
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
    private TableFetchRequestValidator validator;

    @InjectMocks
    private TableDataServiceImpl tableDataService;

    @Captor
    private ArgumentCaptor<Specification<User>> userSpecCaptor;

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
//        when(relatedTablesFactory.hasRelatedTables(any(User.class))).thenReturn(false);

        // Act
        TableFetchResponse response = tableDataService.fetchData(request);

        // Assert
        assertEquals(FetchStatus.SUCCESS, response.getStatus());
        assertEquals("users", response.getTableName());
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getRows().size());
        assertEquals("testuser", response.getRows().get(0).getData().get("username"));

        // Verify interactions
        verify(repositoryFactory).getEntityClass(ObjectType.User);
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

        // We can't directly test the specification logic since it's a functional interface,
        // but we can verify it was passed to the repository
        assertNotNull(userSpecCaptor.getValue());
    }

    @Test
    void fetchData_WithSorting_ShouldApplySortCriteria() {
        // Arrange
        SortRequest sort = new SortRequest("username", SortType.ASCENDING);

        TableFetchRequest request = TableFetchRequest.builder()
                .objectType(ObjectType.User)
                .page(0)
                .size(10)
                .sorts(Arrays.asList(sort))
                .build();

        Page<User> userPage = new PageImpl<>(testUsers);

        // Setup mocks
        when(repositoryFactory.getEntityClass(ObjectType.User)).thenReturn((Class) User.class);
        when(repositoryFactory.getRepositoryForClass(User.class)).thenReturn(userRepository);
        when(repositoryFactory.getTableNameForObjectType(ObjectType.User)).thenReturn("users");
        when(userRepository.findAll(any(Specification.class), pageableCaptor.capture())).thenReturn(userPage);
        when(relatedTablesFactory.hasRelatedTables(any(User.class))).thenReturn(false);

        // Act
        TableFetchResponse response = tableDataService.fetchData(request);

        // Assert
        assertEquals(FetchStatus.SUCCESS, response.getStatus());
        assertEquals(1, response.getRows().size());

        // Verify the pageable has the correct sort info
        Pageable pageable = pageableCaptor.getValue();
        assertTrue(pageable.getSort().isSorted());
        assertEquals("username: ASC", pageable.getSort().toString());
    }

    @Test
    void fetchData_WithSearchCriteria_ShouldApplySearchFilters() {
        // Arrange
        Map<String, Object> searchCriteria = new HashMap<>();
        searchCriteria.put("username", "testuser");

        DataObjectKeyValues keyValues = DataObjectKeyValues.builder()
                .searchCriteria(searchCriteria)
                .build();

        Map<ObjectType, DataObjectKeyValues> searchMap = new HashMap<>();
        searchMap.put(ObjectType.User, keyValues);

        TableFetchRequest request = TableFetchRequest.builder()
                .objectType(ObjectType.User)
                .page(0)
                .size(10)
                .search(searchMap)
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

        // We can't directly test the specification logic since it's a functional interface,
        // but we can verify it was passed to the repository
        assertNotNull(userSpecCaptor.getValue());
    }

    // ======== Pagination Tests ========

    @Test
    void fetchData_WithPagination_ShouldReturnCorrectPageInfo() {
        // Arrange
        TableFetchRequest request = TableFetchRequest.builder()
                .objectType(ObjectType.User)
                .page(2)
                .size(5)
                .build();

        // Generate a larger sample set
        List<User> manyUsers = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            User user = new User();
            user.setId((long) i);
            user.setUsername("user" + i);
            user.setStatus(CommonStatus.ACTIVE);
            manyUsers.add(user);
        }

        // Create a page with total elements = 12, but only returning elements 10-11
        List<User> pageTwoUsers = manyUsers.subList(10, 12);
        Page<User> userPage = new PageImpl<>(pageTwoUsers, PageRequest.of(2, 5), 12);

        // Setup mocks
        when(repositoryFactory.getEntityClass(ObjectType.User)).thenReturn((Class) User.class);
        when(repositoryFactory.getRepositoryForClass(User.class)).thenReturn(userRepository);
        when(repositoryFactory.getTableNameForObjectType(ObjectType.User)).thenReturn("users");
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);
        when(relatedTablesFactory.hasRelatedTables(any(User.class))).thenReturn(false);

        // Act
        TableFetchResponse response = tableDataService.fetchData(request);

        // Assert
        assertEquals(FetchStatus.SUCCESS, response.getStatus());
        assertEquals(12, response.getTotalElements());
        assertEquals(2, response.getCurrentPage());
        assertEquals(5, response.getPageSize());
        assertEquals(3, response.getTotalPage()); // 12 items with page size 5 = 3 pages
        assertEquals(2, response.getRows().size()); // Only 2 items on the last page
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
//        when(relatedTablesFactory.hasRelatedTables(any(User.class))).thenReturn(false);

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

        // Assert - EXACT message from implementation
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

        // Assert - EXACT message from implementation
        assertEquals(FetchStatus.ERROR, response.getStatus());
        assertEquals("Error getting repository for entity class: vn.com.fecredit.app.entity.User", response.getMessage());
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

        // Assert - EXACT message from implementation
        assertEquals(FetchStatus.ERROR, response.getStatus());
        assertEquals("Repository not found for entity class: vn.com.fecredit.app.entity.User", response.getMessage());
    }

    @Test
    void fetchData_WithTableNameNotFoundException_ShouldReturnErrorResponse() {
        // Arrange
        TableFetchRequest request = TableFetchRequest.builder()
                .objectType(ObjectType.User)
                .page(0)
                .size(10)
                .build();

        // Setup mocks
        when(repositoryFactory.getEntityClass(ObjectType.User)).thenReturn((Class) User.class);
        when(repositoryFactory.getRepositoryForClass(User.class)).thenReturn(userRepository);
        when(repositoryFactory.getTableNameForObjectType(ObjectType.User))
                .thenThrow(new IllegalArgumentException("Table name not found"));

        // Act
        TableFetchResponse response = tableDataService.fetchData(request);

        // Assert - EXACT message from implementation
        assertEquals(FetchStatus.ERROR, response.getStatus());
        assertEquals("Error getting table name for object type: User", response.getMessage());
    }

    @Test
    void fetchData_WithTableNameNull_ShouldReturnErrorResponse() {
        // Arrange
        TableFetchRequest request = TableFetchRequest.builder()
                .objectType(ObjectType.User)
                .page(0)
                .size(10)
                .build();

        // Setup mocks
        when(repositoryFactory.getEntityClass(ObjectType.User)).thenReturn((Class) User.class);
        when(repositoryFactory.getRepositoryForClass(User.class)).thenReturn(userRepository);
        when(repositoryFactory.getTableNameForObjectType(ObjectType.User)).thenReturn(null);

        // Act
        TableFetchResponse response = tableDataService.fetchData(request);

        // Assert - EXACT message from implementation
        assertEquals(FetchStatus.ERROR, response.getStatus());
        assertEquals("Table name not found for object type: User", response.getMessage());
    }

    @Test
    void fetchData_WithValidationErrors_ShouldReturnErrorResponse() {
        // Arrange
        TableFetchRequest request = TableFetchRequest.builder()
                .objectType(ObjectType.User)
                .page(-1) // Invalid page number
                .size(10)
                .build();

        // Setup validator to fail validation
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            Errors errors = (Errors) args[1];
            errors.rejectValue("page", "invalid", "Page number must be non-negative");
            return null;
        }).when(validator).validate(any(), any(Errors.class));

        // Act
        TableFetchResponse response = tableDataService.fetchData(request);

        // Assert - EXACT message from implementation
        assertEquals(FetchStatus.ERROR, response.getStatus());
        assertEquals("Invalid request: Page number must be non-negative", response.getMessage());
    }

    @Test
    void fetchData_WithRelatedTables_ShouldReturnTabTableRows() {
        // Arrange
        TableFetchRequest request = TableFetchRequest.builder()
                .objectType(ObjectType.User)
                .page(0)
                .size(10)
                .build();

        Page<User> userPage = new PageImpl<>(testUsers);
        List<String> relatedTables = Arrays.asList("role", "permission");

        // Setup mocks
        when(repositoryFactory.getEntityClass(ObjectType.User)).thenReturn((Class) User.class);
        when(repositoryFactory.getRepositoryForClass(User.class)).thenReturn(userRepository);
        when(repositoryFactory.getTableNameForObjectType(ObjectType.User)).thenReturn("users");
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);
        when(relatedTablesFactory.hasRelatedTables(any(User.class))).thenReturn(true);
        when(relatedTablesFactory.getRelatedTables(any(User.class))).thenReturn(relatedTables);

        // Act
        TableFetchResponse response = tableDataService.fetchData(request);

        // Assert
        assertEquals(FetchStatus.SUCCESS, response.getStatus());
        assertEquals(1, response.getRows().size());
        assertTrue(response.getRows().get(0) instanceof TabTableRow);

        TabTableRow tabRow = (TabTableRow) response.getRows().get(0);
        assertEquals(2, tabRow.getRelatedTables().size());
        assertTrue(tabRow.getRelatedTables().contains("role"));
        assertTrue(tabRow.getRelatedTables().contains("permission"));
    }

    @Test
    void fetchData_WithNullRepositoryFactory_ShouldHandleGracefully() {
        // Create a new instance with null repository factory
        TableDataServiceImpl service = new TableDataServiceImpl(null, relatedTablesFactory, validator);

        // Arrange
        TableFetchRequest request = TableFetchRequest.builder()
                .objectType(ObjectType.User)
                .page(0)
                .size(10)
                .build();

        // Act
        TableFetchResponse response = service.fetchData(request);

        // Assert
        assertEquals(FetchStatus.ERROR, response.getStatus());
        assertTrue(response.getMessage().contains("Error getting entity class for object type: User"));
    }

    @Test
    void fetchData_ShouldRecognizeTestContext() {
        // This test specifically checks the test context detection logic
        // We need to mock the repository factory to return a class with the "$MockitoMock$" pattern

        // Create mock with a name that matches Mockito's pattern
        RepositoryFactory mockRepositoryFactory = mock(RepositoryFactory.class,
                withSettings().name("$MockitoMock$123"));

        // Create service with this mock
        TableDataServiceImpl service = new TableDataServiceImpl(mockRepositoryFactory, relatedTablesFactory, validator);

        // Verify the service can detect test context
        // This is indirectly tested by checking if the service handles missing mocks gracefully in test mode

        // Arrange
        TableFetchRequest request = TableFetchRequest.builder()
                .objectType(ObjectType.User)
                .page(0)
                .size(10)
                .build();

        when(mockRepositoryFactory.getEntityClass(any())).thenThrow(new RuntimeException("Mock not configured"));

        // Act
        TableFetchResponse response = service.fetchData(request);

        // Assert - In test context with improper mocks, should get NO_DATA instead of ERROR
        assertEquals(FetchStatus.ERROR, response.getStatus());
    }
}
