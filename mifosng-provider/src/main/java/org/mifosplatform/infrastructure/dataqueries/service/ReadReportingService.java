package org.mifosplatform.infrastructure.dataqueries.service;

import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.mifosplatform.infrastructure.dataqueries.data.GenericResultsetData;

public interface ReadReportingService {

    StreamingOutput retrieveReportCSV(String name, String type, Map<String, String> extractedQueryParams);

    GenericResultsetData retrieveGenericResultset(String name, String type, Map<String, String> extractedQueryParams);

    Response processPentahoRequest(String reportName, String outputType, Map<String, String> queryParams);

    String retrieveReportPDF(String name, String type, Map<String, String> extractedQueryParams);

    String getReportType(String reportName);
}