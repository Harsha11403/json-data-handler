package com.assignment.jsondatahandler.service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.assignment.jsondatahandler.model.JsonRecord;
import com.assignment.jsondatahandler.repository.JsonRecordRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecordService {

    private final JsonRecordRepository recordRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public JsonRecord insertRecord(String datasetName, JsonNode recordContent) {
        JsonRecord record = new JsonRecord();
        record.setDatasetName(datasetName);
        record.setRecordContent(recordContent);
        return recordRepository.save(record);
    }

    public Map<String, List<JsonNode>> groupRecords(String datasetName, String groupByField) {
        List<JsonRecord> records = recordRepository.findByDatasetName(datasetName);

        Map<String, List<JsonNode>> groupedRecords = records.stream()
                .filter(record -> record.getRecordContent() != null && record.getRecordContent().has(groupByField))
                .collect(Collectors.groupingBy(
                        record -> {
                            JsonNode fieldNode = record.getRecordContent().get(groupByField);
                            if (fieldNode.isTextual()) {
                                return fieldNode.asText();
                            } else if (fieldNode.isNumber()) {
                                return String.valueOf(fieldNode.asDouble());
                            } else if (fieldNode.isBoolean()) {
                                return String.valueOf(fieldNode.asBoolean());
                            }
                            return fieldNode.toString();
                        },
                        LinkedHashMap::new,
                        Collectors.mapping(JsonRecord::getRecordContent, Collectors.toList())
                ));

        return groupedRecords;
    }

    public List<JsonNode> sortRecords(String datasetName, String sortByField, String order) {
        List<JsonRecord> records = recordRepository.findByDatasetName(datasetName);

        Comparator<JsonRecord> comparator = (record1, record2) -> {
            JsonNode field1 = record1.getRecordContent().get(sortByField);
            JsonNode field2 = record2.getRecordContent().get(sortByField);

            if (field1 == null && field2 == null) return 0;
            if (field1 == null) return -1;
            if (field2 == null) return 1;

            if (field1.isTextual() && field2.isTextual()) {
                return field1.asText().compareTo(field2.asText());
            } else if (field1.isNumber() && field2.isNumber()) {
                return Double.compare(field1.asDouble(), field2.asDouble());
            } else if (field1.isBoolean() && field2.isBoolean()) {
                return Boolean.compare(field1.asBoolean(), field2.asBoolean());
            } else {
                return field1.toString().compareTo(field2.toString());
            }
        };

        if ("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }

        List<JsonNode> sortedRecords = records.stream()
                .filter(record -> record.getRecordContent() != null)
                .sorted(comparator)
                .map(JsonRecord::getRecordContent)
                .collect(Collectors.toList());

        return sortedRecords;
    }
}