package com.assignment.jsondatahandler.model;

import org.hibernate.annotations.Type;

import com.fasterxml.jackson.databind.JsonNode;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="json_record")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JsonRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="dataset_name", nullable = false)
	private String datasetName;
	
	@Type(JsonType.class)
	@Column(name = "record_content", columnDefinition = "jsonb")
	private JsonNode recordContent;
	 
}
