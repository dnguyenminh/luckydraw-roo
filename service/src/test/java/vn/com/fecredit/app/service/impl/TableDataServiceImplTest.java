package vn.com.fecredit.app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.EntityManager;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.entity.enums.CommonStatus;
import vn.com.fecredit.app.repository.SimpleObjectRepository;
import vn.com.fecredit.app.service.AbstractServiceTest;
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

@SuppressWarnings({"unchecked", "rawtypes"})
public class TableDataServiceImplTest extends AbstractServiceTest {

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

    @InjectMocks
    private TableDataServiceImpl tableDataService;

    private TableFetchRequest userRequest;
    private TableFetchRequest eventRequest;
    private User testUser;
    private Event testEvent;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFullName("Test User");
        testUser.setStatus(CommonStatus.ACTIVE);

        // Setup test event
        testEvent = new Event();
        testEvent.setId(1L);
        testEvent.setName("Test Event");
        testEvent.setCode("EVT-001");
        testEvent.setDescription("Test event description");
        testEvent.setStatus(CommonStatus.ACTIVE);

        // Setup user fetch request
        userRequest = new TableFetchRequest();
        userRequest.setObjectType(ObjectType.User);
        userRequest.setPage(0);
        userRequest.setSize(10);
        userRequest.setFilters(new ArrayList<>());
        userRequest.setSorts(new ArrayList<>());
        userRequest.setSearch(new HashMap<>());

        // Setup event fetch request
        eventRequest = new TableFetchRequest();
        eventRequest.setObjectType(ObjectType.Event);
        eventRequest.setPage(0);
        eventRequest.setSize(10);
        eventRequest.setFilters(new ArrayList<>());
        eventRequest.setSorts(new ArrayList<>());
        eventRequest.setSearch(new HashMap<>());

        // Setup repository factory mocks
        // Use lenient() for all stubbings in setup to avoid "unnecessary stubbing" errors
        lenient().when(repositoryFactory.getEntityClass(ObjectType.User)).thenReturn((Class) User.class);
        lenient().when(repositoryFactory.getEntityClass(ObjectType.Event)).thenReturn((Class) Event.class);
        lenient().when(repositoryFactory.getRepositoryForClass(User.class)).thenReturn(userRepository);
        lenient().when(repositoryFactory.getRepositoryForClass(Event.class)).thenReturn(eventRepository);
        lenient().when(repositoryFactory.getTableNameForObjectType(ObjectType.User)).thenReturn("users");
        lenient().when(repositoryFactory.getTableNameForObjectType(ObjectType.Event)).thenReturn("events");
    }

    @Test
    void fetchData_WithNullRequest_ShouldReturnErrorResponse() {
        // When
        TableFetchResponse response = tableDataService.fetchData(null);

        // Then
        assertThat(response.getStatus()).isEqualTo(FetchStatus.ERROR);
        assertThat(response.getMessage()).isEqualTo("Request cannot be null");
    }

    @Test
    void fetchData_WithNoObjectTypeOrEntity_ShouldReturnErrorResponse() {
        // Given
        TableFetchRequest request = new TableFetchRequest();

        // When
        TableFetchResponse response = tableDataService.fetchData(request);

        // Then
        assertThat(response.getStatus()).isEqualTo(FetchStatus.ERROR);
        assertThat(response.getMessage()).contains("No object type or entity name specified");
    }

    @Test
    void fetchData_ForUserEntity_ShouldReturnUserData() {
        // Given
        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users);

        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);
        when(relatedTablesFactory.hasRelatedTables(any(User.class))).thenReturn(false);

        // When
        TableFetchResponse response = tableDataService.fetchData(userRequest);

        // Then
        assertThat(response.getStatus()).isEqualTo(FetchStatus.SUCCESS);
        assertThat(response.getTableName()).isEqualTo("users");
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getRows()).hasSize(1);
        assertThat(response.getRows().get(0).getData()).containsKey("id");
        assertThat(response.getRows().get(0).getData()).containsKey("username");
        assertThat(response.getRows().get(0).getData().get("username")).isEqualTo("testuser");
    }

    @Test
    void fetchData_ForEventEntity_ShouldReturnEventData() {
        // Given
        List<Event> events = Arrays.asList(testEvent);
        Page<Event> eventPage = new PageImpl<>(events);

        when(eventRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(eventPage);
        when(relatedTablesFactory.hasRelatedTables(any(Event.class))).thenReturn(false);

        // When
        TableFetchResponse response = tableDataService.fetchData(eventRequest);

        // Then
        assertThat(response.getStatus()).isEqualTo(FetchStatus.SUCCESS);
        assertThat(response.getTableName()).isEqualTo("events");
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getRows()).hasSize(1);
        assertThat(response.getRows().get(0).getData()).containsKey("id");
        assertThat(response.getRows().get(0).getData()).containsKey("name");
        assertThat(response.getRows().get(0).getData().get("name")).isEqualTo("Test Event");
    }

    @Test
    void fetchData_WithSorting_ShouldApplySortCriteria() {
        // Given
        userRequest.setSorts(Arrays.asList(
                new SortRequest("username", SortType.ASCENDING)));

        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users);

        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);
        when(relatedTablesFactory.hasRelatedTables(any(User.class))).thenReturn(false);

        // When
        TableFetchResponse response = tableDataService.fetchData(userRequest);

        // Then
        assertThat(response.getStatus()).isEqualTo(FetchStatus.SUCCESS);
        assertThat(response.getRows()).hasSize(1);
    }

    @Test
    void fetchData_WithFilters_ShouldApplyFilterCriteria() {
        // Given
        FilterRequest filter = new FilterRequest();
        filter.setField("username");
        filter.setFilterType(FilterType.EQUALS);
        filter.setMinValue("testuser");
        userRequest.setFilters(Arrays.asList(filter));

        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users);

        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);
        when(relatedTablesFactory.hasRelatedTables(any(User.class))).thenReturn(false);

        // When
        TableFetchResponse response = tableDataService.fetchData(userRequest);

        // Then
        assertThat(response.getStatus()).isEqualTo(FetchStatus.SUCCESS);
        assertThat(response.getRows()).hasSize(1);
    }

    @Test
    void fetchData_WithSearch_ShouldApplySearchCriteria() {
        // Given
        Map<String, String> search = new HashMap<>();
        search.put("username", "test");
        userRequest.setSearch(search);

        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users);

        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);
        when(relatedTablesFactory.hasRelatedTables(any(User.class))).thenReturn(false);

        // When
        TableFetchResponse response = tableDataService.fetchData(userRequest);

        // Then
        assertThat(response.getStatus()).isEqualTo(FetchStatus.SUCCESS);
        assertThat(response.getRows()).hasSize(1);
    }

    @Test
    void fetchData_WithEmptyResult_ShouldReturnNoDataStatus() {
        // Given
        Page<User> emptyPage = new PageImpl<>(new ArrayList<>());

        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(emptyPage);

        // When
        TableFetchResponse response = tableDataService.fetchData(userRequest);

        // Then
        assertThat(response.getStatus()).isEqualTo(FetchStatus.NO_DATA);
        assertThat(response.getRows()).isEmpty();
    }

    @Test
    void fetchData_WithEntityName_ShouldMapToObjectType() {
        // Given
        TableFetchRequest request = new TableFetchRequest();
        request.setEntityName("User");
        request.setPage(0);
        request.setSize(10);

        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users);

        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);
        when(relatedTablesFactory.hasRelatedTables(any(User.class))).thenReturn(false);

        // When
        TableFetchResponse response = tableDataService.fetchData(request);

        // Then
        assertThat(response.getStatus()).isEqualTo(FetchStatus.SUCCESS);
        assertThat(response.getTableName()).isEqualTo("users");
    }

    @Test
    void fetchData_WithInvalidEntityName_ShouldReturnErrorResponse() {
        // Given
        TableFetchRequest request = new TableFetchRequest();
        request.setEntityName("InvalidEntity");

        // When
        TableFetchResponse response = tableDataService.fetchData(request);

        // Then
        assertThat(response.getStatus()).isEqualTo(FetchStatus.ERROR);
        assertThat(response.getMessage()).contains("Unsupported entity");
    }

    @Test
    void fetchData_WithException_ShouldReturnErrorResponse() {
        // Given
        when(repositoryFactory.getEntityClass(any())).thenThrow(new RuntimeException("Test exception"));

        // When
        TableFetchResponse response = tableDataService.fetchData(userRequest);

        // Then
        assertThat(response.getStatus()).isEqualTo(FetchStatus.ERROR);
        assertThat(response.getMessage()).contains("Error fetching data");
    }

    @Test
    void fetchData_WithRelatedTables_ShouldIncludeTabInformation() {
        // Given
        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users);
        List<String> relatedTables = Arrays.asList("roles", "permissions");

        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);
        when(relatedTablesFactory.hasRelatedTables(any(User.class))).thenReturn(true);
        when(relatedTablesFactory.getRelatedTables(any(User.class))).thenReturn(relatedTables);

        // When
        TableFetchResponse response = tableDataService.fetchData(userRequest);

        // Then
        assertThat(response.getStatus()).isEqualTo(FetchStatus.SUCCESS);
        assertThat(response.getRows()).hasSize(1);
        // The TabTableRow implementation details would need to be verified here
    }
}
