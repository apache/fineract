package org.mifosng.platform;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.mifosng.platform.api.data.AdditionalFieldsSetData;
import org.mifosng.platform.api.data.GenericResultsetData;

public interface ReadExtraDataAndReportingService {

	StreamingOutput retrieveReportCSV(String name, String type,
			Map<String, String> extractedQueryParams);

	// @PreAuthorize(value = "hasAnyRole('REPORTING_SUPER_USER_ROLE')")
	GenericResultsetData retrieveGenericResultset(String name, String type,
			Map<String, String> extractedQueryParams);

	List<AdditionalFieldsSetData> retrieveExtraDatasetNames(String type);

	GenericResultsetData retrieveExtraData(String type, String set, Long id);

	void updateExtraData(String type, String set, Long id,
			Map<String, String> queryParams);

	String retrieveDataTable(String datatable, String sqlSearch, String sqlOrder);	

	Response processPentahoRequest(String reportName, String outputType,
			Map<String, String> queryParams);

	String getReportType(String reportName);
}