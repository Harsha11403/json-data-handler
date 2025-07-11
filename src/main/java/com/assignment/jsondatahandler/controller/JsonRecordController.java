package com.assignment.jsondatahandler.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.assignment.jsondatahandler.dto.GroupedRecordsResponse;
import com.assignment.jsondatahandler.dto.RecordInsertResponse;
import com.assignment.jsondatahandler.dto.SortedRecordsResponse;
import com.assignment.jsondatahandler.model.JsonRecord;
import com.assignment.jsondatahandler.service.RecordService;
import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dataset")
@RequiredArgsConstructor
public class JsonRecordController {

	private final RecordService recordService;

    @Operation(summary = "Insert a JSON record into a specific dataset")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Record added successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RecordInsertResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body or dataset name")
    })
    @PostMapping("/{datasetName}/record")
    public ResponseEntity<RecordInsertResponse> insertRecord(
            @Parameter(description = "Name of the dataset", required = true) @PathVariable String datasetName,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "JSON record content", required = true,
                    content = @Content(mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(value = "{\"id\": 1, \"name\": \"John Doe\", \"age\": 30, \"department\": \"Engineering\"}")))
            @RequestBody JsonNode recordContent) {
        if (datasetName == null || datasetName.trim().isEmpty()) {
            return new ResponseEntity<>(new RecordInsertResponse("Dataset name cannot be empty", null, null), HttpStatus.BAD_REQUEST);
        }
        if (recordContent == null || recordContent.isEmpty()) {
            return new ResponseEntity<>(new RecordInsertResponse("Record content cannot be empty", datasetName, null), HttpStatus.BAD_REQUEST);
        }

        try {
            JsonRecord savedRecord = recordService.insertRecord(datasetName, recordContent);
            return new ResponseEntity<>(
                    new RecordInsertResponse("Record added successfully", datasetName, savedRecord.getId()),
                    HttpStatus.CREATED
            );
        } catch (Exception e) {
            return new ResponseEntity<>(new RecordInsertResponse("Error adding record: " + e.getMessage(), datasetName, null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Query a specific dataset and perform group-by or sort-by operations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Query successful",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(oneOf = {GroupedRecordsResponse.class, SortedRecordsResponse.class}))),
            @ApiResponse(responseCode = "400", description = "Invalid query parameters")
    })
    @GetMapping("/{datasetName}/query")
    public ResponseEntity<?> queryRecords(
            @Parameter(description = "Name of the dataset", required = true) @PathVariable String datasetName,
            @Parameter(description = "Field to group by (e.g., 'department'). Mutually exclusive with sortBy.", example = "department") @RequestParam(required = false) String groupBy,
            @Parameter(description = "Field to sort by (e.g., 'age'). Mutually exclusive with groupBy.", example = "age") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort order (asc/desc). Required if sortBy is used.", example = "asc") @RequestParam(required = false, defaultValue = "asc") String order) {

        if (datasetName == null || datasetName.trim().isEmpty()) {
            return new ResponseEntity<>("Dataset name cannot be empty", HttpStatus.BAD_REQUEST);
        }

        if (groupBy != null && sortBy != null) {
            return new ResponseEntity<>("Cannot specify both 'groupBy' and 'sortBy' parameters.", HttpStatus.BAD_REQUEST);
        }

        try {
            if (groupBy != null) {
                Map<String, List<JsonNode>> groupedRecords = recordService.groupRecords(datasetName, groupBy);
                return ResponseEntity.ok(new GroupedRecordsResponse(groupedRecords));
            } else if (sortBy != null) {
                if (!"asc".equalsIgnoreCase(order) && !"desc".equalsIgnoreCase(order)) {
                    return new ResponseEntity<>("Invalid 'order' parameter. Must be 'asc' or 'desc'.", HttpStatus.BAD_REQUEST);
                }
                List<JsonNode> sortedRecords = recordService.sortRecords(datasetName, sortBy, order);
                return ResponseEntity.ok(new SortedRecordsResponse(sortedRecords));
            } else {
                return new ResponseEntity<>("Please provide either 'groupBy' or 'sortBy' parameter.", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Error processing query: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}