package com.assignment.jsondatahandler.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupedRecordsResponse {

	private Map<String, List<JsonNode>> groupedRecords;
}
