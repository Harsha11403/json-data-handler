package com.assignment.jsondatahandler.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordInsertResponse {

	private String message;
	private String dataSet;
	private Long recordId;
}
