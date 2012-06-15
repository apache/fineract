package org.mifosng.platform;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.StreamingOutput;

import org.mifosng.platform.api.data.AdditionalFieldsSet;
import org.mifosng.platform.api.data.GenericResultset;

public interface ReadExtraDataAndReportingService {

	StreamingOutput retrieveReportCSV(String name, String type,
			Map<String, String> extractedQueryParams);

	// @PreAuthorize(value = "hasAnyRole('REPORTING_SUPER_USER_ROLE')")
	GenericResultset retrieveGenericResultset(String name, String type,
			Map<String, String> extractedQueryParams);

	List<AdditionalFieldsSet> retrieveExtraDatasetNames(String type);

	GenericResultset retrieveExtraData(String type, String set, Long id);

	void updateExtraData(String type, String set, Long id,
			Map<String, String> queryParams);

	String getReportType(String reportName);
}