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
package org.apache.fineract.integrationtests.client.feign.helpers;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.RunReportsResponse;

public class FeignReportHelper {

    private final FineractFeignClient fineractClient;

    public FeignReportHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    /**
     * Runs a table report and returns its generic result set: the column headers plus one row per record.
     *
     * @param reportParameters
     *            the report's {@code R_}-prefixed parameters, as expected by the report definition
     */
    public RunReportsResponse runReport(String reportName, Map<String, String> reportParameters) {
        return ok(() -> fineractClient.runReports().runReportGetData(reportName, reportParameters));
    }

    /**
     * Runs a table report with {@code genericResultSet=false}, which answers a plain array of rows rather than the
     * {@code columnHeaders}/{@code data} shape {@link #runReport} decodes. The keys of each row are whatever columns
     * the report's stored SQL selects, so there is no generated model to bind them to.
     *
     * @param reportParameters
     *            the report's {@code R_}-prefixed parameters, as expected by the report definition
     */
    public List<Map<String, Object>> runReportRows(String reportName, Map<String, String> reportParameters) {
        Map<String, String> parameters = new HashMap<>(reportParameters);
        parameters.put("genericResultSet", "false");
        return ok(() -> fineractClient.runReports().runReportGetRows(reportName, parameters));
    }
}
