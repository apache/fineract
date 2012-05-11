package org.mifosng.platform;

import java.util.Map;

import org.mifosng.data.ExtraDatasets;
import org.mifosng.data.reports.GenericResultset;

public interface ReadExtraDataAndReportingService {

	ExtraDatasets retrieveExtraDatasetNames(String datasetType);

	GenericResultset retrieveExtraData(String datasetType, String datasetName,
			String datasetPKValue);

	void tempSaveExtraData(String datasetType, String datasetName,
			String datasetPKValue, Map<String, String> queryParams);
}