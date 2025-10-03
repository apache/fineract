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
package org.apache.fineract.client.feign.services;

import feign.Param;
import feign.QueryMap;
import feign.RequestLine;
import feign.Response;
import java.util.Map;
import org.apache.fineract.client.models.RunReportsResponse;

public interface RunReportsApi {

    /**
     * Running a Report This resource allows you to run and receive output from pre-defined Apache Fineract reports.
     * Reports can also be used to provide data for searching and workflow functionality. The default output is a JSON
     * formatted "Generic Resultset". The Generic Resultset contains Column Heading as well as Data information.
     * However, you can export to CSV format by simply adding "&amp;exportCSV=true" to the end of your URL. If Pentaho
     * reports have been pre-defined, they can also be run through this resource. Pentaho reports can return HTML, PDF
     * or CSV formats. The Apache Fineract reference application uses a JQuery plugin called stretchy reporting which,
     * itself, uses this reports resource to provide a pretty flexible reporting User Interface (UI). Example Requests:
     * runreports/Client%20Listing?R_officeId=1 runreports/Client%20Listing?R_officeId=1&amp;exportCSV=true
     * runreports/OfficeIdSelectOne?R_officeId=1&amp;parameterType=true
     * runreports/OfficeIdSelectOne?R_officeId=1&amp;parameterType=true&amp;exportCSV=true
     * runreports/Expected%20Payments%20By%20Date%20-%20Formatted?R_endDate=2013-04-30&amp;R_loanOfficerId=-1&amp;R_officeId=1&amp;R_startDate=2013-04-16&amp;output-type=HTML&amp;R_officeId=1
     * runreports/Expected%20Payments%20By%20Date%20-%20Formatted?R_endDate=2013-04-30&amp;R_loanOfficerId=-1&amp;R_officeId=1&amp;R_startDate=2013-04-16&amp;output-type=XLS&amp;R_officeId=1
     * runreports/Expected%20Payments%20By%20Date%20-%20Formatted?R_endDate=2013-04-30&amp;R_loanOfficerId=-1&amp;R_officeId=1&amp;R_startDate=2013-04-16&amp;output-type=CSV&amp;R_officeId=1
     * runreports/Expected%20Payments%20By%20Date%20-%20Formatted?R_endDate=2013-04-30&amp;R_loanOfficerId=-1&amp;R_officeId=1&amp;R_startDate=2013-04-16&amp;output-type=PDF&amp;R_officeId=1
     *
     * @param reportName
     *            reportName (required)
     * @param parameters
     *            Dynamic query parameters for the report (required)
     * @return RunReportsResponse
     */
    @RequestLine("GET /v1/runreports/{reportName}")
    RunReportsResponse runReportGetData(@Param("reportName") String reportName, @QueryMap Map<String, String> parameters);

    /**
     * Run Report which returns a response such as a PDF, CSV, XLS or XLSX file.
     *
     * @param reportName
     *            reportName (required)
     * @param parameters
     *            Dynamic query parameters for the report (required)
     * @return Response containing the file content
     */
    @RequestLine("GET /v1/runreports/{reportName}")
    Response runReportGetFile(@Param("reportName") String reportName, @QueryMap Map<String, String> parameters);
}
