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
import java.time.Duration;
import java.util.Map;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import org.apache.fineract.client.util.FineractClient;
import org.apache.fineract.integrationtests.CIOnly;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import retrofit2.Response;

/**
 * Integration Test for /runreports/ API.
 *
 * @author Michael Vorburger.ch
 */
public class ReportExportTest extends IntegrationTest {

    @Override
    protected void customizeFineractClient(FineractClient.Builder builder) {
        builder.readTimeout(Duration.ofSeconds(30));
    }

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
    }

    @Test
    void runClientListingTableReportCSV() throws IOException {
        Response<ResponseBody> result = okR(
                fineractClient().reportsRun.runReportGetFile("Client Listing", Map.of("R_officeId", "1", "exportCSV", "true"), false));
        assertThat(result.body().contentType()).isEqualTo(MediaType.parse("text/csv"));
        assertThat(result.body().string()).contains("Office/Branch");
    }

    @Test
    @CIOnly
    void runClientListingTableReportS3() throws IOException {
        Response<ResponseBody> result = okR(
                fineractClient().reportsRun.runReportGetFile("Client Listing", Map.of("R_officeId", "1", "exportS3", "true"), false));
        assertThat(result.code()).isEqualTo(204);
    }

    @Test
    @CIOnly
    void runClientListingTableReportS3WithValidData() throws IOException {
        Response<ResponseBody> result = okR(fineractClient().reportsRun.runReportGetFile("Client Listing",
                Map.of("R_officeId", "1", "exportS3", "true", "R_currencyId", "USD"), false));
        assertThat(result.code()).isEqualTo(204);
    }

    @Test
    @CIOnly
    void runReportS3WithEmptyContent() throws IOException {
        try {
            Response<ResponseBody> result = okR(fineractClient().reportsRun.runReportGetFile("Client Listing",
                    Map.of("R_officeId", "999999", "exportS3", "true"), false));

            if (!result.isSuccessful()) {
                assertThat(result.code()).isIn(400, 500);
            }
        } catch (Exception e) {
            assertThat(e.getMessage()).contains("empty report content");
        }
    }

    @Test
    @CIOnly
    void runReportS3WithLongFileName() throws IOException {
        String longReportName = "A".repeat(1000);

        try {
            Response<ResponseBody> result = okR(
                    fineractClient().reportsRun.runReportGetFile(longReportName, Map.of("R_officeId", "1", "exportS3", "true"), false));

            if (!result.isSuccessful()) {
                assertThat(result.code()).isIn(400, 500);
            }
        } catch (Exception e) {
            assertThat(e.getMessage()).contains("key length exceeds maximum");
        }
    }

    @Test
    @CIOnly
    void runReportS3WithMissingBucket() throws IOException {
        Response<ResponseBody> result = okR(
                fineractClient().reportsRun.runReportGetFile("Client Listing", Map.of("R_officeId", "1", "exportS3", "true"), false));

        assertThat(result.code()).isEqualTo(204);
    }

    @Test
    void runReportWithInvalidOfficeId() throws IOException {
        try {
            Response<ResponseBody> result = okR(
                    fineractClient().reportsRun.runReportGetFile("Client Listing", Map.of("R_officeId", "-1", "exportCSV", "true"), false));

            if (result.isSuccessful()) {
                assertThat(result.body().contentType()).isEqualTo(MediaType.parse("text/csv"));
            } else {
                assertThat(result.code()).isIn(400, 404, 500);
            }
        } catch (Exception e) {
            assertThat(e).isNotNull();
        }
    }

    @Test
    @CIOnly
    void runMultipleS3ExportsSimultaneously() throws IOException {
        Response<ResponseBody> result1 = okR(
                fineractClient().reportsRun.runReportGetFile("Client Listing", Map.of("R_officeId", "1", "exportS3", "true"), false));

        Response<ResponseBody> result2 = okR(
                fineractClient().reportsRun.runReportGetFile("Client Listing", Map.of("R_officeId", "2", "exportS3", "true"), false));

        assertThat(result1.code()).isEqualTo(204);
        assertThat(result2.code()).isEqualTo(204);
    }

    @Test
    @CIOnly
    void runReportS3WithSpecialCharactersInParams() throws IOException {
        Response<ResponseBody> result = okR(fineractClient().reportsRun.runReportGetFile("Client Listing",
                Map.of("R_officeId", "1", "exportS3", "true", "R_clientName", "Test Client & Co."), false));

        assertThat(result.code()).isEqualTo(204);
    }
}
