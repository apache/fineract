/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.dataqueries.service;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.mifosplatform.infrastructure.dataqueries.data.GenericResultsetData;
import org.mifosplatform.infrastructure.dataqueries.data.ReportData;
import org.mifosplatform.infrastructure.dataqueries.data.ReportParameterData;

public interface ReadReportingService {

    StreamingOutput retrieveReportCSV(String name, String type, Map<String, String> extractedQueryParams);

    GenericResultsetData retrieveGenericResultset(String name, String type, Map<String, String> extractedQueryParams);

    Response processPentahoRequest(String reportName, String outputType, Map<String, String> queryParams, Locale locale);

    String retrieveReportPDF(String name, String type, Map<String, String> extractedQueryParams);

    String getReportType(String reportName);

    Collection<ReportData> retrieveReportList();

    Collection<ReportParameterData> getAllowedParameters();

    ReportData retrieveReport(final Long id);
}