/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.dataqueries.service;

import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.core.StreamingOutput;

import org.apache.fineract.infrastructure.dataqueries.data.GenericResultsetData;
import org.apache.fineract.infrastructure.dataqueries.data.ReportData;
import org.apache.fineract.infrastructure.dataqueries.data.ReportParameterData;
import org.apache.fineract.useradministration.domain.AppUser;

public interface ReadReportingService {

    StreamingOutput retrieveReportCSV(String name, String type, Map<String, String> extractedQueryParams, boolean isSelfServiceUserReport);

    GenericResultsetData retrieveGenericResultset(String name, String type, Map<String, String> extractedQueryParams, boolean isSelfServiceUserReport);

    String retrieveReportPDF(String name, String type, Map<String, String> extractedQueryParams, boolean isSelfServiceUserReport);

    String getReportType(String reportName, boolean isSelfServiceUserReport);

    Collection<ReportData> retrieveReportList();

    Collection<ReportParameterData> getAllowedParameters();

    ReportData retrieveReport(final Long id);

    Collection<String> getAllowedReportTypes();
    
  //needed for smsCampaign and emailCampaign jobs where securityContext is null
    GenericResultsetData retrieveGenericResultSetForSmsEmailCampaign(String name, String type, Map<String, String> extractedQueryParams);

    String  sqlToRunForSmsEmailCampaign(String name, String type, Map<String, String> queryParams);

	ByteArrayOutputStream generatePentahoReportAsOutputStream(String reportName, String outputTypeParam,
            Map<String, String> queryParams, Locale locale, AppUser runReportAsUser, StringBuilder errorLog);
}