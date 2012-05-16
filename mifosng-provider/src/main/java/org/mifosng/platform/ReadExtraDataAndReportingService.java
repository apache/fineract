package org.mifosng.platform;

import java.util.Map;

import javax.ws.rs.core.StreamingOutput;

import org.mifosng.data.AdditionalFieldsSets;
import org.mifosng.data.reports.GenericResultset;

public interface ReadExtraDataAndReportingService {

	StreamingOutput retrieveReportCSV(String name, String type,
			Map<String, String> extractedQueryParams);

	// @PreAuthorize(value = "hasAnyRole('REPORTING_SUPER_USER_ROLE')")
	GenericResultset retrieveGenericResultset(String name, String type,
			Map<String, String> extractedQueryParams);

	AdditionalFieldsSets retrieveExtraDatasetNames(String type);

	GenericResultset retrieveExtraData(String type, String set, String id);

	void updateExtraData(String type, String set, String id,
			Map<String, String> queryParams);
}