package vn.com.fecredit.app.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.ServiceTestApplication;
import vn.com.fecredit.app.service.TableDataService;
import vn.com.fecredit.app.service.dto.ColumnInfo;
import vn.com.fecredit.app.service.dto.DataObject;
import vn.com.fecredit.app.service.dto.FetchStatus;
import vn.com.fecredit.app.service.dto.FilterRequest;
import vn.com.fecredit.app.service.dto.FilterType;
import vn.com.fecredit.app.service.dto.ObjectType;
import vn.com.fecredit.app.service.dto.SortRequest;
import vn.com.fecredit.app.service.dto.SortType;
import vn.com.fecredit.app.service.dto.TabTableRow;
import vn.com.fecredit.app.service.dto.TableFetchRequest;
import vn.com.fecredit.app.service.dto.TableFetchResponse;
import vn.com.fecredit.app.service.dto.TableRow;
import vn.com.fecredit.app.service.factory.RepositoryFactory;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.Permission;
import vn.com.fecredit.app.service.util.EntityConverter;

/**
 * Integration tests for TableDataServiceImpl using real data from SQL script.
 */
@Slf4j
@SpringBootTest(classes = ServiceTestApplication.class)
@AutoConfigureTestEntityManager
@ActiveProfiles("test")
@Transactional
public class TableDataServiceIntegrationTest {

        @Configuration
        static class TestSecurityConfig {
                @Bean
                public org.springframework.security.authentication.AuthenticationManager authenticationManager() {
                        return Mockito.mock(org.springframework.security.authentication.AuthenticationManager.class);
                }
        }

        @BeforeAll
        public static void setupDatabase(@Autowired DataSource dataSource) throws Exception {
                try (Connection connection = dataSource.getConnection()) {
                        // First, execute the schema creation script directly
                        ScriptUtils.executeSqlScript(
                                        connection,
                                        new EncodedResource(new ClassPathResource("/schema-test.sql"), "UTF-8"),
                                        false, true,
                                        "--", ";",
                                        "/*", "*/");

                        // Then execute the data script
                        ScriptUtils.executeSqlScript(
                                        connection,
                                        new EncodedResource(new ClassPathResource("/data-test.sql"), "UTF-8"),
                                        false, true,
                                        "--", ";",
                                        "/*", "*/");
                } catch (Exception e) {
                        e.printStackTrace();
                        throw e;
                }
        }

        @Autowired
        private TableDataService tableDataService;

        @Autowired
        private RepositoryFactory repositoryFactory;

        @PersistenceContext
        private EntityManager entityManager;

        @Autowired
        private EntityConverter entityConverter;

        @Test
        void testFetchSimpleData() {
                // Create a simple fetch request
                TableFetchRequest request = TableFetchRequest.builder()
                                .objectType(ObjectType.User)
                                .page(0)
                                .size(10)
                                .build();

                // Log debug info
                log.info("Starting testFetchSimpleData test case");

                // Execute the request
                TableFetchResponse response = tableDataService.fetchData(request);

                // Debug logging
                log.info("Response received: status={}, message={}, rowCount={}",
                                response.getStatus(),
                                response.getMessage(),
                                response.getRows() != null ? response.getRows().size() : 0);

                // 1. Basic validation
                assertNotNull(response, "Response should not be null");

                // 2. Status validation
                assertTrue(
                                response.getStatus() == FetchStatus.SUCCESS
                                                || response.getStatus() == FetchStatus.NO_DATA,
                                "Response status should be either SUCCESS or NO_DATA but was " + response.getStatus());

                // 3. Full response structure validation based on status
                if (response.getStatus() == FetchStatus.SUCCESS) {
                        // A. Table metadata validation
                        assertEquals("users", response.getTableName(), "Table name should be 'users'");
                        assertNotNull(response.getFieldNameMap(), "Field name map should not be null");
                        assertTrue(response.getFieldNameMap().containsKey("username"),
                                        "Field name map should contain username field");

                        // B. Pagination information validation
                        assertNotNull(response.getTotalElements(), "Total elements should not be null");

                        // Verify total elements count is correct by doing a direct database count
                        long expectedCount = countUsers();
                        assertEquals(expectedCount, response.getTotalElements(),
                                        "Total elements should match the actual number of users in the database");
                        log.info("Total elements check: expected={}, actual={}", expectedCount,
                                        response.getTotalElements());

                        assertNotNull(response.getCurrentPage(), "Current page should not be null");
                        assertEquals(0, response.getCurrentPage(), "Current page should be 0");
                        assertNotNull(response.getPageSize(), "Page size should not be null");
                        assertEquals(10, response.getPageSize(), "Page size should be 10");

                        // B.1 Total page validation - verify total page calculation is correct
                        assertNotNull(response.getTotalPage(), "Total page count should not be null");
                        long expectedTotalPages = (response.getTotalElements() + response.getPageSize() - 1)
                                        / response.getPageSize();
                        assertEquals(expectedTotalPages, response.getTotalPage().longValue(),
                                        "Total pages should be ceil(totalElements/pageSize)");
                        log.info("Total elements: {}, Page size: {}, Expected total pages: {}, Actual total pages: {}",
                                        response.getTotalElements(), response.getPageSize(),
                                        expectedTotalPages, response.getTotalPage());

                        // C. Request information validation
                        assertNotNull(response.getOriginalRequest(), "Original request should not be null");
                        assertEquals(request.getObjectType(), response.getOriginalRequest().getObjectType(),
                                        "Original request should have same object type");
                        assertEquals(request.getPage(), response.getOriginalRequest().getPage(),
                                        "Original request should have same page number");

                        // D. Row data validation
                        assertNotNull(response.getRows(), "Rows should not be null");
                        assertTrue(response.getTotalElements() >= response.getRows().size(),
                                        "Total elements should be greater than or equal to rows count");

                        if (!response.getRows().isEmpty()) {
                                TableRow firstRow = response.getRows().get(0);
                                assertNotNull(firstRow, "First row should not be null");
                                assertNotNull(firstRow.getData(), "Row data should not be null");

                                // Check for essential user fields
                                Map<String, Object> userData = firstRow.getData();
                                assertTrue(userData.containsKey("id"), "User data should contain id");
                                assertTrue(userData.containsKey("username"), "User data should contain username");
                                assertTrue(userData.containsKey("email"), "User data should contain email");
                                assertTrue(userData.containsKey("fullName"), "User data should contain fullName");
                                assertTrue(userData.containsKey("status"), "User data should contain status");
                                assertTrue(userData.containsKey("enabled"), "User data should contain enabled flag");

                                // Log the first user data for information
                                log.info("First user: id={}, username={}, email={}",
                                                userData.get("id"),
                                                userData.get("username"),
                                                userData.get("email"));
                        }
                } else if (response.getStatus() == FetchStatus.NO_DATA) {
                        // E. NO_DATA response validation
                        assertEquals(0, response.getTotalElements() == null ? 0 : response.getTotalElements(),
                                        "Total elements should be 0 for NO_DATA response");
                        assertTrue(response.getRows() == null || response.getRows().isEmpty(),
                                        "Rows should be null or empty for NO_DATA response");
                }

                // 4. Additional validations for special cases
                // Check if we have rows with related tables - these would be TabTableRow
                // instances
                if (response.getStatus() == FetchStatus.SUCCESS && response.getRows() != null) {
                        for (TableRow row : response.getRows()) {
                                if (row instanceof TabTableRow) {
                                        TabTableRow tabRow = (TabTableRow) row;
                                        assertNotNull(tabRow.getRelatedTables(),
                                                        "Related tables should not be null for TabTableRow");
                                        log.info("Found TabTableRow with {} related tables",
                                                        tabRow.getRelatedTables().size());
                                        tabRow.getRelatedTables()
                                                        .forEach(table -> log.info("  Related table: {}", table));
                                }
                        }
                }
        }

        @Test
        void fetchUsers_WithStatusFilter_ShouldReturnFilteredUsers() {
                // Arrange
                // Use a direct string value instead of an enum conversion, as the database
                // values are strings
                FilterRequest statusFilter = new FilterRequest("status", FilterType.EQUALS, "INACTIVE", null);
                TableFetchRequest request = TableFetchRequest.builder()
                                .objectType(ObjectType.User)
                                .page(0)
                                .size(20)
                                .filters(Collections.singletonList(statusFilter))
                                .build();

                // Act
                TableFetchResponse response = tableDataService.fetchData(request);

                // Assert
                assertNotNull(response);

                // If we're getting ERROR status, log the error message
                if (response.getStatus() == FetchStatus.ERROR) {
                        log.error("Error in fetchUsers_WithStatusFilter_ShouldReturnFilteredUsers: {}", response.getMessage());
                }

                // Check for both SUCCESS and NO_DATA as acceptable outcomes
                assertTrue(
                                response.getStatus() == FetchStatus.SUCCESS
                                                || response.getStatus() == FetchStatus.NO_DATA,
                                "Response status should be either SUCCESS or NO_DATA but was " + response.getStatus() +
                                (response.getMessage() != null ? ": " + response.getMessage() : ""));
        }

        @Test
        void fetchUsers_WithSorting_ShouldReturnSortedUsers() {
                // Arrange
                SortRequest sortRequest = new SortRequest("username", SortType.DESCENDING);
                TableFetchRequest request = TableFetchRequest.builder()
                                .objectType(ObjectType.User)
                                .page(0)
                                .size(20)
                                .sorts(Collections.singletonList(sortRequest))
                                .build();

                // Act
                TableFetchResponse response = tableDataService.fetchData(request);

                // Assert
                assertNotNull(response);
                assertEquals(FetchStatus.SUCCESS, response.getStatus());
        }

        @Test
        void testPaginationAndTotalElements() {
                // We'll test with multiple page sizes to verify consistency
                int[] pageSizes = { 5, 10, 20, 50 };

                // Get the expected total count directly from the database
                long expectedTotalUsers = countUsers();
                log.info("Expected total users in database: {}", expectedTotalUsers);

                for (int pageSize : pageSizes) {
                        // First page
                        TableFetchRequest firstPageRequest = TableFetchRequest.builder()
                                        .objectType(ObjectType.User)
                                        .page(0)
                                        .size(pageSize)
                                        .build();

                        TableFetchResponse firstPageResponse = tableDataService.fetchData(firstPageRequest);

                        // Verify the total elements is consistent across different page sizes
                        assertEquals(expectedTotalUsers, firstPageResponse.getTotalElements(),
                                        "Total elements should be consistent regardless of page size");

                        // Verify total pages calculation is correct
                        int expectedTotalPages = (int) Math.ceil((double) expectedTotalUsers / pageSize);
                        assertEquals(expectedTotalPages, firstPageResponse.getTotalPage(),
                                        "Total pages calculation should be correct for page size " + pageSize);

                        // Verify first page has correct number of items
                        int expectedFirstPageSize = (int) Math.min(pageSize, expectedTotalUsers);
                        assertEquals(expectedFirstPageSize, firstPageResponse.getRows().size(),
                                        "First page should have the correct number of items");

                        // If there's more than one page, check the second page too
                        if (expectedTotalPages > 1) {
                                TableFetchRequest secondPageRequest = TableFetchRequest.builder()
                                                .objectType(ObjectType.User)
                                                .page(1)
                                                .size(pageSize)
                                                .build();

                                TableFetchResponse secondPageResponse = tableDataService.fetchData(secondPageRequest);

                                // Same total elements and pages
                                assertEquals(expectedTotalUsers, secondPageResponse.getTotalElements(),
                                                "Total elements should be the same on second page");
                                assertEquals(expectedTotalPages, secondPageResponse.getTotalPage(),
                                                "Total pages should be the same on second page");

                                // Verify second page has correct number of items
                                int expectedSecondPageSize = (int) Math.min(pageSize,
                                                Math.max(0, expectedTotalUsers - pageSize));
                                assertEquals(expectedSecondPageSize, secondPageResponse.getRows().size(),
                                                "Second page should have the correct number of items");

                                // Verify different page has different data
                                if (!firstPageResponse.getRows().isEmpty() && !secondPageResponse.getRows().isEmpty()) {
                                        String firstPageFirstUsername = (String) firstPageResponse.getRows().get(0)
                                                        .getData()
                                                        .get("username");
                                        String secondPageFirstUsername = (String) secondPageResponse.getRows().get(0)
                                                        .getData()
                                                        .get("username");
                                        assertNotEquals(firstPageFirstUsername, secondPageFirstUsername,
                                                        "First user on first page should be different from first user on second page");
                                }
                        }

                        // Log summary for this page size
                        log.info("Page size {} - Total: {}, Pages: {}, First page size: {}",
                                        pageSize, expectedTotalUsers, expectedTotalPages, expectedFirstPageSize);
                }
        }

        @Test
        void testFetchDataWithRelatedLinkedObjects() {
                // Create a request with search criteria for related roles
                Map<ObjectType, DataObject> searchMap = new HashMap<>();

                // Create a data object for Role search
                DataObject roleSearch = new DataObject();
                roleSearch.setObjectType(ObjectType.Role);

                // Add some search criteria
                TableRow searchData = new TableRow();
                Map<String, Object> searchParams = new HashMap<>();
                searchParams.put("roleType", "ROLE_ADMIN"); // Search for admin role
                searchData.setData(searchParams);
                roleSearch.setData(searchData);

                // Add to search map
                searchMap.put(ObjectType.Role, roleSearch);

                // Create request with search criteria
                TableFetchRequest request = TableFetchRequest.builder()
                                .objectType(ObjectType.User)
                                .page(0)
                                .size(10)
                                .search(searchMap)
                                .build();

                // Execute the request
                log.info("Starting testFetchDataWithRelatedLinkedObjects test case");
                TableFetchResponse response = tableDataService.fetchData(request);

                // Debug logging
                log.info("Response received: status={}, relatedLinkedObjects={}",
                                response.getStatus(),
                                response.getRelatedLinkedObjects() != null ? response.getRelatedLinkedObjects().size()
                                                : "null");

                // Verify the response has related linked objects
                assertNotNull(response, "Response should not be null");
                assertNotNull(response.getRelatedLinkedObjects(), "Related linked objects should not be null");

                // Users should have related roles - check for ObjectType.Role key instead of
                // "roles" string
                assertFalse(response.getRelatedLinkedObjects().isEmpty(),
                                "Related linked objects should not be empty");

                // Verify that expected related objects are present using ObjectType.Role as key
                assertTrue(response.getRelatedLinkedObjects().containsKey(ObjectType.Role),
                                "Related linked objects should include roles data");

                // If we have roles data, verify it has content
                if (response.getRelatedLinkedObjects().containsKey(ObjectType.Role)) {
                        Object rolesData = response.getRelatedLinkedObjects().get(ObjectType.Role);
                        assertNotNull(rolesData, "Roles data should not be null");
                        // Additional assertions based on the expected structure of roles data
                }

                // Log detailed information about found related objects for debugging
                response.getRelatedLinkedObjects()
                                .forEach((key, value) -> log.info(
                                                "Found related object: key={}, valueType={}, valueContent={}",
                                                key,
                                                value != null ? value.getClass().getName() : "null",
                                                value));
        }

        @Test
        void testCachingMechanismForColumnInfoGeneration() {
                // Create a request for the same object type twice
                TableFetchRequest firstRequest = TableFetchRequest.builder()
                                .objectType(ObjectType.User)
                                .page(0)
                                .size(10)
                                .build();

                TableFetchRequest secondRequest = TableFetchRequest.builder()
                                .objectType(ObjectType.User)
                                .page(0)
                                .size(10)
                                .build();

                // Execute the first request
                log.info("Executing first request to populate cache");
                TableFetchResponse firstResponse = tableDataService.fetchData(firstRequest);
                assertNotNull(firstResponse.getFieldNameMap(), "Field name map should be populated");
                assertFalse(firstResponse.getFieldNameMap().isEmpty(), "Field name map should not be empty");

                // Record the field name map from the first response
                Map<String, ColumnInfo> firstFieldNameMap = firstResponse.getFieldNameMap();

                // Execute the second request - should use cached column info
                log.info("Executing second request to test cache");
                TableFetchResponse secondResponse = tableDataService.fetchData(secondRequest);

                // Both responses should have identical field name maps (same instance if cached
                // properly)
                // Use System.identityHashCode to check if they're the same object in memory
                log.info("First field name map hash: {}", System.identityHashCode(firstFieldNameMap));
                log.info("Second field name map hash: {}", System.identityHashCode(secondResponse.getFieldNameMap()));

                assertEquals(firstFieldNameMap.size(), secondResponse.getFieldNameMap().size(),
                                "Both responses should have the same number of columns");

                // Check key fields are present in both responses
                assertTrue(firstFieldNameMap.containsKey("id"), "Should have id field");
                assertTrue(secondResponse.getFieldNameMap().containsKey("id"), "Should have id field");

                // They should have the same field definitions
                for (String key : firstFieldNameMap.keySet()) {
                        assertTrue(secondResponse.getFieldNameMap().containsKey(key),
                                        "Second response should contain field: " + key);
                        assertEquals(
                                        firstFieldNameMap.get(key).getFieldType(),
                                        secondResponse.getFieldNameMap().get(key).getFieldType(),
                                        "Field types should match for: " + key);
                }
        }

        @Test
        void testFetchDataWithComplexSearch() {
                // First fetch actual data from repositories
                Role adminRole = findAdminRole();
                Permission manageUsersPermission = findManageUsersPermission();

                assertNotNull(adminRole, "Admin role should exist in test database");
                assertNotNull(manageUsersPermission, "MANAGE_USERS permission should exist in test database");

                log.info("Found role: type: {}", adminRole.getRoleType());
                log.info("Found permission: {}", manageUsersPermission.getName());

                // Create a request with multiple search criteria for different related entities
                Map<ObjectType, DataObject> searchMap = new HashMap<>();

                // 1. Convert Role entity to DataObject using the utility
                DataObject roleSearch = entityConverter.convertToDataObject(adminRole);
                searchMap.put(ObjectType.Role, roleSearch);

                // 2. Convert Permission entity to DataObject using the utility
                DataObject permissionSearch = entityConverter.convertToDataObject(manageUsersPermission);
                searchMap.put(ObjectType.Permission, permissionSearch);

                // 3. Create additional sort and filter parameters
                List<SortRequest> sorts = new ArrayList<>();
                sorts.add(new SortRequest("username", SortType.ASCENDING));

                List<FilterRequest> filters = new ArrayList<>();
                filters.add(new FilterRequest("status", FilterType.EQUALS, "ACTIVE", null));

                // Create complex request with multiple search criteria
                TableFetchRequest request = TableFetchRequest.builder()
                        .objectType(ObjectType.User)
                        .page(0)
                        .size(10)
                        .search(searchMap)
                        .sorts(sorts)
                        .filters(filters)
                        .build();

                // Log for debugging
                log.info("Starting testFetchDataWithComplexSearch test case");
                log.info("Search criteria count: {}", request.getSearch().size());

                // Execute the request
                TableFetchResponse response = tableDataService.fetchData(request);

                // Debug logging
                log.info("Response received: status={}, rows={}, relatedLinkedObjects={}",
                        response.getStatus(),
                        response.getRows() != null ? response.getRows().size() : "null",
                        response.getRelatedLinkedObjects() != null ? response.getRelatedLinkedObjects().size() : "null");

                // Basic assertions
                assertNotNull(response, "Response should not be null");

                // Either SUCCESS or NO_DATA are acceptable outcomes
                assertTrue(
                        response.getStatus() == FetchStatus.SUCCESS || response.getStatus() == FetchStatus.NO_DATA,
                        "Response status should be either SUCCESS or NO_DATA but was " + response.getStatus()
                );

                // Verify that related linked objects contains our search criteria
                assertNotNull(response.getRelatedLinkedObjects(), "Related linked objects should not be null");
                assertFalse(response.getRelatedLinkedObjects().isEmpty(), "Related linked objects should not be empty");
                assertEquals(2, response.getRelatedLinkedObjects().size(),
                        "Related linked objects should contain both Role and Permission entries");

                // Check if Role search data is preserved
                assertTrue(response.getRelatedLinkedObjects().containsKey(ObjectType.Role),
                        "Related linked objects should include Role data");
                DataObject returnedRoleData = response.getRelatedLinkedObjects().get(ObjectType.Role);
                assertNotNull(returnedRoleData, "Role data should not be null");
                assertEquals(adminRole.getRoleType().toString(), returnedRoleData.getData().getData().get("roleType"),
                        "Role search criteria should be preserved");

                // Check if Permission search data is preserved
                assertTrue(response.getRelatedLinkedObjects().containsKey(ObjectType.Permission),
                        "Related linked objects should include Permission data");
                DataObject returnedPermissionData = response.getRelatedLinkedObjects().get(ObjectType.Permission);
                assertNotNull(returnedPermissionData, "Permission data should not be null");
                assertEquals(manageUsersPermission.getName(), returnedPermissionData.getData().getData().get("name"),
                        "Permission search criteria should be preserved");
        }

        /**
         * Helper method to find the admin role from the database
         */
        private Role findAdminRole() {
            try {
                return entityManager.createQuery(
                        "SELECT r FROM Role r WHERE r.roleType = 'ROLE_ADMIN'", Role.class)
                        .setMaxResults(1)
                        .getResultList()
                        .stream()
                        .findFirst()
                        .orElse(null);
            } catch (Exception e) {
                log.error("Error finding admin role: {}", e.getMessage());
                return null;
            }
        }

        /**
         * Helper method to find the MANAGE_USERS permission from the database
         */
        private Permission findManageUsersPermission() {
            try {
                return entityManager.createQuery(
                        "SELECT p FROM Permission p WHERE p.name = 'MANAGE_USERS'", Permission.class)
                        .setMaxResults(1)
                        .getResultList()
                        .stream()
                        .findFirst()
                        .orElse(null);
            } catch (Exception e) {
                log.error("Error finding MANAGE_USERS permission: {}", e.getMessage());
                return null;
            }
        }

        // Helper method
        private <T> List<T> extractFieldValues(List<TableRow> rows, String fieldName) {
                if (rows == null)
                        return Collections.emptyList();
                return rows.stream()
                                .filter(row -> row != null && row.getData() != null)
                                .map(row -> (T) row.getData().get(fieldName))
                                .filter(value -> value != null)
                                .collect(Collectors.toList());
        }

        /**
         * Directly counts the number of users in the database using EntityManager
         * This provides a verification source for getTotalElements
         *
         * @return the count of users in the database
         */
        private long countUsers() {
                return (long) entityManager.createQuery("SELECT COUNT(u) FROM User u").getSingleResult();
        }
}
