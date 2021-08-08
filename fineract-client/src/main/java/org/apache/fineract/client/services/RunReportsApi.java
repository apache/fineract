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
package org.apache.fineract.client.services;

import java.util.Map;
import okhttp3.ResponseBody;
import org.apache.fineract.client.models.RunReportsResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

// https://issues.apache.org/jira/browse/FINERACT-1263
public interface RunReportsApi {

    /**
     * Running a Report This resource allows you to run and receive output from pre-defined Apache Fineract reports.
     * Reports can also be used to provide data for searching and workflow functionality. The default output is a JSON
     * formatted \&quot;Generic Resultset\&quot;. The Generic Resultset contains Column Heading as well as Data
     * information. However, you can export to CSV format by simply adding \&quot;&amp;exportCSV&#x3D;true\&quot; to the
     * end of your URL. If Pentaho reports have been pre-defined, they can also be run through this resource. Pentaho
     * reports can return HTML, PDF or CSV formats. The Apache Fineract reference application uses a JQuery plugin
     * called stretchy reporting which, itself, uses this reports resource to provide a pretty flexible reporting User
     * Interface (UI). Example Requests: runreports/Client%20Listing?R_officeId&#x3D;1
     * runreports/Client%20Listing?R_officeId&#x3D;1&amp;exportCSV&#x3D;true
     * runreports/OfficeIdSelectOne?R_officeId&#x3D;1&amp;parameterType&#x3D;true
     * runreports/OfficeIdSelectOne?R_officeId&#x3D;1&amp;parameterType&#x3D;true&amp;exportCSV&#x3D;true
     * runreports/Expected%20Payments%20By%20Date%20-%20Formatted?R_endDate&#x3D;2013-04-30&amp;R_loanOfficerId&#x3D;-1&amp;R_officeId&#x3D;1&amp;R_startDate&#x3D;2013-04-16&amp;output-type&#x3D;HTML&amp;R_officeId&#x3D;1
     * runreports/Expected%20Payments%20By%20Date%20-%20Formatted?R_endDate&#x3D;2013-04-30&amp;R_loanOfficerId&#x3D;-1&amp;R_officeId&#x3D;1&amp;R_startDate&#x3D;2013-04-16&amp;output-type&#x3D;XLS&amp;R_officeId&#x3D;1
     * runreports/Expected%20Payments%20By%20Date%20-%20Formatted?R_endDate&#x3D;2013-04-30&amp;R_loanOfficerId&#x3D;-1&amp;R_officeId&#x3D;1&amp;R_startDate&#x3D;2013-04-16&amp;output-type&#x3D;CSV&amp;R_officeId&#x3D;1
     * runreports/Expected%20Payments%20By%20Date%20-%20Formatted?R_endDate&#x3D;2013-04-30&amp;R_loanOfficerId&#x3D;-1&amp;R_officeId&#x3D;1&amp;R_startDate&#x3D;2013-04-16&amp;output-type&#x3D;PDF&amp;R_officeId&#x3D;1
     *
     * @param reportName
     *            reportName (required)
     * @param isSelfServiceUserReport
     *            isSelfServiceUserReport (optional, default to false)
     * @return Call&lt;GetReportNameResponse&gt;
     */
    @GET("runreports/{reportName}")
    Call<RunReportsResponse> runReportGetData(@retrofit2.http.Path("reportName") String reportName,
            @QueryMap Map<String, String> parameters, @retrofit2.http.Query("isSelfServiceUserReport") Boolean isSelfServiceUserReport);

    /**
     * Run Report which returns a response such as a PDF, CSV, XLS or XSLX file.
     */
    @GET("runreports/{reportName}")
    Call<ResponseBody> runReportGetFile(@retrofit2.http.Path("reportName") String reportName, @QueryMap Map<String, String> parameters,
            @retrofit2.http.Query("isSelfServiceUserReport") Boolean isSelfServiceUserReport);

}
