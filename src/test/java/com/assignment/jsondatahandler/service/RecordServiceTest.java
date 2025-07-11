package com.assignment.jsondatahandler.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.assignment.jsondatahandler.model.JsonRecord;
import com.assignment.jsondatahandler.repository.JsonRecordRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ExtendWith(MockitoExtension.class)
class RecordServiceTest {

    @Mock
    private JsonRecordRepository recordRepository;

    @InjectMocks
    private RecordService recordService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        recordService = new RecordService(recordRepository, objectMapper);
    }

    private JsonRecord createJsonRecord(Long id, String datasetName, String jsonString) {
        try {
            return new JsonRecord(id, datasetName, objectMapper.readTree(jsonString));
        } catch (Exception e) {
            throw new RuntimeException("Error creating JsonRecord for test", e);
        }
    }

    @Test
    @DisplayName("Should sort records by a string field in ascending order")
    void shouldSortRecordsByStringFieldAscending() {
        JsonRecord record1 = createJsonRecord(1L, "dataset1", "{\"name\":\"Charlie\", \"age\":30}");
        JsonRecord record2 = createJsonRecord(2L, "dataset1", "{\"name\":\"Alice\", \"age\":25}");
        JsonRecord record3 = createJsonRecord(3L, "dataset1", "{\"name\":\"Bob\", \"age\":35}");

        when(recordRepository.findByDatasetName("dataset1"))
            .thenReturn(Arrays.asList(record1, record2, record3));

        String datasetName = "dataset1";
        String sortByField = "name";
        String order = "ASC";

        List<JsonNode> sortedRecords = recordService.sortRecords(datasetName, sortByField, order);

        assertNotNull(sortedRecords, "Sorted records list should not be null");
        assertEquals(3, sortedRecords.size(), "Sorted records list should have 3 elements");

        assertEquals("Alice", sortedRecords.get(0).get("name").asText(), "First record should be Alice");
        assertEquals("Bob", sortedRecords.get(1).get("name").asText(), "Second record should be Bob");
        assertEquals("Charlie", sortedRecords.get(2).get("name").asText(), "Third record should be Charlie");
    }

    @Test
    @DisplayName("Should sort records by a number field in descending order")
    void shouldSortRecordsByNumberFieldDescending() {
        JsonRecord record1 = createJsonRecord(1L, "dataset1", "{\"name\":\"A\", \"age\":30}");
        JsonRecord record2 = createJsonRecord(2L, "dataset1", "{\"name\":\"B\", \"age\":25}");
        JsonRecord record3 = createJsonRecord(3L, "dataset1", "{\"name\":\"C\", \"age\":35}");

        when(recordRepository.findByDatasetName("dataset1"))
            .thenReturn(Arrays.asList(record1, record2, record3));

        String datasetName = "dataset1";
        String sortByField = "age";
        String order = "DESC";

        List<JsonNode> sortedRecords = recordService.sortRecords(datasetName, sortByField, order);

        assertNotNull(sortedRecords, "Sorted records list should not be null");
        assertEquals(3, sortedRecords.size(), "Sorted records list should have 3 elements");

        assertEquals(35, sortedRecords.get(0).get("age").asInt(), "First record should have age 35");
        assertEquals(30, sortedRecords.get(1).get("age").asInt(), "Second record should have age 30");
        assertEquals(25, sortedRecords.get(2).get("age").asInt(), "Third record should have age 25");
    }
}