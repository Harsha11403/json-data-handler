package com.assignment.jsondatahandler.dto;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SortedRecordsResponse {

	private List<JsonNode> sortedRecords;
}
