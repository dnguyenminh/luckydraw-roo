package vn.com.fecredit.app.service.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for serialization and deserialization of TableFetchRequest
 * with focus on the DataObjectKey handling that caused issues
 */
public class TableFetchRequestTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        // Configure ObjectMapper to handle Java 8 date/time types
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    @DisplayName("Should fail deserialization when key is an empty string")
    void shouldFailDeserializationWithEmptyStringKey() {
        // Given: JSON with an empty string key
        String json = """
            {
                "objectType": "Event",
                "page": 0,
                "size": 10,
                "sorts": [],
                "filters": [],
                "search": {
                    "Event": {
                        "objectType": "Event",
                        "key": "",
                        "description": "Test",
                        "data": {
                            "data": { "id": 1 }
                        }
                    }
                }
            }
            """;
        
        // When & Then: verify the exception matches what we expect
        Exception exception = assertThrows(Exception.class, () -> {
            objectMapper.readValue(json, TableFetchRequest.class);
        });
        
        assertTrue(exception.getMessage().contains("Cannot coerce empty String"));
    }
    
    @Test
    @DisplayName("Should succeed deserialization when key has proper structure")
    void shouldDeserializeFromJsonWithProperKey() throws IOException {
        // Given: JSON with a proper key structure
        String json = """
            {
                "objectType": "Event",
                "page": 0,
                "size": 10,
                "sorts": [],
                "filters": [],
                "search": {
                    "Event": {
                        "objectType": "Event",
                        "key": {"keys": []},
                        "description": "Test",
                        "data": {
                            "data": { "id": 1 }
                        }
                    }
                }
            }
            """;
        
        // When: we parse the JSON into a TableFetchRequest
        TableFetchRequest request = objectMapper.readValue(json, TableFetchRequest.class);
        
        // Then: parsing should succeed and properties should be correctly mapped
        assertNotNull(request);
        assertEquals(ObjectType.Event, request.getObjectType());
        assertEquals(0, request.getPage());
        assertEquals(10, request.getSize());
        
        DataObject eventData = request.getSearch().get(ObjectType.Event);
        assertNotNull(eventData);
        assertNotNull(eventData.getKey());
        assertNotNull(eventData.getKey().getKeys());
        assertTrue(eventData.getKey().getKeys().isEmpty());
    }
    
    @Test
    @DisplayName("Should successfully serialize TableFetchRequest with proper key structure")
    void shouldSerializeWithProperKeyStructure() throws IOException {
        // Given: a TableFetchRequest with properly structured DataObjectKey
        TableFetchRequest request = new TableFetchRequest();
        request.setObjectType(ObjectType.Event);
        request.setPage(0);
        request.setSize(10);
        
        DataObject dataObject = new DataObject();
        dataObject.setObjectType(ObjectType.Event);
        
        DataObjectKey key = new DataObjectKey();
        key.setKeys(new ArrayList<>());
        dataObject.setKey(key);
        
        TableRow tableRow = new TableRow();
        Map<String, Object> data = new HashMap<>();
        data.put("id", 1);
        tableRow.setData(data);
        dataObject.setData(tableRow);
        
        Map<ObjectType, DataObject> search = new HashMap<>();
        search.put(ObjectType.Event, dataObject);
        request.setSearch(search);
        
        // When: we serialize it
        String json = objectMapper.writeValueAsString(request);
        
        // Then: the JSON should contain a properly structured key
        assertTrue(json.contains("\"key\":{\"keys\":[]}"));
        assertFalse(json.contains("\"key\":\"\""));
        
        // And: we can deserialize it back without errors
        TableFetchRequest deserialized = objectMapper.readValue(json, TableFetchRequest.class);
        assertNotNull(deserialized.getSearch().get(ObjectType.Event).getKey().getKeys());
    }
    
    @Test
    @DisplayName("Should fix and properly deserialize the sample JSON")
    void shouldModifyJsonToFixKeyIssue() throws IOException {
        // Given: the problematic JSON from our resource file
        InputStream jsonStream = getClass().getResourceAsStream("/fetchDataRequest.json");
        String originalJson = new String(jsonStream.readAllBytes());
        
        // When: we modify the JSON to replace empty key with proper structure
        String modifiedJson = originalJson.replace("\"key\":\"\"", "\"key\":{\"keys\":[]}");
        
        // Then: the modified JSON should deserialize correctly
        TableFetchRequest request = objectMapper.readValue(modifiedJson, TableFetchRequest.class);
        
        // Verify properties to ensure deserialization was successful
        assertNotNull(request);
        assertEquals(ObjectType.EventLocation, request.getObjectType());
        assertEquals("EventLocation", request.getEntityName());
        assertEquals(0, request.getPage());
        assertEquals(20, request.getSize());
        
        // Verify search data
        Map<ObjectType, DataObject> search = request.getSearch();
        assertTrue(search.containsKey(ObjectType.Event));
        
        // Verify key structure was fixed
        DataObject eventData = search.get(ObjectType.Event);
        assertNotNull(eventData.getKey());
        assertNotNull(eventData.getKey().getKeys());
        
        // Verify some nested data
        Map<String, Object> eventProperties = eventData.getData().getData();
        assertEquals("Spring Celebration", eventProperties.get("name"));
        assertEquals("SPRING_FEST", eventProperties.get("code"));
        assertEquals(3, eventProperties.get("id"));
    }
    
    @Test
    @DisplayName("Should confirm original JSON fails with expected error message")
    void shouldFailWithOriginalJson() {
        // Given: the original problematic JSON
        Exception exception = assertThrows(Exception.class, () -> {
            objectMapper.readValue(
                getClass().getResourceAsStream("/fetchDataRequest.json"),
                TableFetchRequest.class
            );
        });
        
        // Then: verify we get the expected error message
        assertTrue(exception.getMessage().contains("Cannot coerce empty String"));
        assertTrue(exception.getMessage().contains("DataObjectKey"));
    }
}
