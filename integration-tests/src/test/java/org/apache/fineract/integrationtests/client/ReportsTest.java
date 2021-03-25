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
package org.apache.fineract.integrationtests.client;

import java.io.IOException;
import java.util.Map;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Integration Test for /runreports/ API.
 *
 * @author Michael Vorburger.ch
 */
public class ReportsTest extends IntegrationTest {

    @Test
    void listReports() {
        assertThat(ok(fineract().reports.retrieveReportList())).hasSize(117);
    }

    @Test
    void runClientListingTableReport() {
        assertThat(ok(fineract().reportsRun.runReportGetData("Client Listing", Map.of("R_officeId", "1"), false)).getColumnHeaders().get(0)
                .getColumnName()).isEqualTo("Office/Branch");
    }

    @Test // see FINERACT-1306
    void runReportCategory() throws IOException {
        // Using raw OkHttp instead of Retrofit API here, because /runreports/reportCategoryList returns JSON Array -
        // but runReportGetData() expects columnHeaders/data JSON.
        var req = new Request.Builder().url(fineract().baseURL().resolve(
                "/fineract-provider/api/v1/runreports/reportCategoryList?R_reportCategory=Fund&genericResultSet=false&parameterType=true&tenantIdentifier=default"))
                .build();
        try (var response = fineract().okHttpClient().newCall(req).execute()) {
            assertThat(response.code()).isEqualTo(200);
        }
    }

    @Test
    void runExpectedPaymentsPentahoReportWithoutPlugin() {
        assertThat(fineract().reportsRun.runReportGetFile("Expected Payments By Date - Formatted", Map.of("R_endDate", "2013-04-30",
                "R_loanOfficerId", "-1", "R_officeId", "1", "R_startDate", "2013-04-16", "output-type", "PDF"), false)).hasHttpStatus(503);
    }

    @Test
    @Disabled
    void runExpectedPaymentsPentahoReport() {
        ResponseBody r = ok(fineract().reportsRun.runReportGetFile("Expected Payments By Date - Formatted", Map.of("R_endDate",
                "2013-04-30", "R_loanOfficerId", "-1", "R_officeId", "1", "R_startDate", "2013-04-16", "output-type", "PDF"), false));
        assertThat(r.contentType()).isEqualTo(MediaType.get("application/pdf"));
    }
}
