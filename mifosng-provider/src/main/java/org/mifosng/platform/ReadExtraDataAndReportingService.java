package org.mifosng.platform;

import java.util.Map;

import org.mifosng.data.ExtraDatasets;
import org.mifosng.data.reports.GenericResultset;

public interface ReadExtraDataAndReportingService {

	//@PreAuthorize(value = "hasAnyRole('REPORTING_SUPER_USER_ROLE')")
	GenericResultset retrieveGenericResultset(String rptDB, String name,
			String type, Map<String, String> extractedQueryParams);
	
	ExtraDatasets retrieveExtraDatasetNames(String datasetType);

	GenericResultset retrieveExtraData(String datasetType, String datasetName,
			String datasetPKValue);

	void tempSaveExtraData(String datasetType, String datasetName,
			String datasetPKValue, Map<String, String> queryParams);
}