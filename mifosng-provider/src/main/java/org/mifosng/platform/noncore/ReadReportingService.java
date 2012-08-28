package org.mifosng.platform.noncore;

import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.mifosng.platform.api.data.GenericResultsetData;

public interface ReadReportingService {

	StreamingOutput retrieveReportCSV(String name, String type,
			Map<String, String> extractedQueryParams);

	GenericResultsetData retrieveGenericResultset(String name, String type,
			Map<String, String> extractedQueryParams);

	Response processPentahoRequest(String reportName, String outputType,
			Map<String, String> queryParams);

	String getReportType(String reportName);
}