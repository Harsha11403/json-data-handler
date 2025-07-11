package com.assignment.jsondatahandler.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.assignment.jsondatahandler.model.JsonRecord;

@Repository
public interface JsonRecordRepository extends JpaRepository<JsonRecord, Long> {

	List<JsonRecord> findByDatasetName(String datasetName);
	
	@Query(value = "SELECT * FROM json_record WHERE dataset_name = :datasetName", nativeQuery = true)
    List<JsonRecord> findByDatasetNameForGrouping(@Param("datasetName") String datasetName);
}
