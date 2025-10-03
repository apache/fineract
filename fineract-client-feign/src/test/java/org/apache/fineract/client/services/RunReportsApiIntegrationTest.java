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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import feign.Response;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.fineract.client.feign.FineractFeignClientConfig;
import org.apache.fineract.client.feign.services.RunReportsApi;
import org.apache.fineract.client.models.RunReportsResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
class RunReportsApiIntegrationTest {

    private WireMockServer wireMockServer;
    private FineractFeignClientConfig config;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
        config = FineractFeignClientConfig.builder().baseUrl("http://localhost:" + wireMockServer.port()).credentials("test", "test")
                .connectTimeout(5, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build();
    }

    @AfterEach
    void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    void testRunReportGetDataWithMultipleParameters() {
        wireMockServer.stubFor(get(urlPathEqualTo("/v1/runreports/DateRangeReport")).withQueryParam("officeId", equalTo("1"))
                .withQueryParam("fromDate", equalTo("2024-01-01")).withQueryParam("toDate", equalTo("2024-12-31")).willReturn(aResponse()
                        .withStatus(200).withHeader("Content-Type", "application/json").withBody("{\"columnHeaders\":[],\"data\":[]}")));

        RunReportsApi api = config.createClient(RunReportsApi.class);
        Map<String, String> params = new HashMap<>();
        params.put("officeId", "1");
        params.put("fromDate", "2024-01-01");
        params.put("toDate", "2024-12-31");

        RunReportsResponse response = api.runReportGetData("DateRangeReport", params);

        assertThat(response).isNotNull();
    }

    @Test
    void testRunReportGetDataWithEmptyResult() {
        wireMockServer.stubFor(get(urlPathEqualTo("/v1/runreports/EmptyReport")).withQueryParam("officeId", equalTo("1")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("{\"columnHeaders\":[],\"data\":[]}")));

        RunReportsApi api = config.createClient(RunReportsApi.class);
        Map<String, String> params = new HashMap<>();
        params.put("officeId", "1");

        RunReportsResponse response = api.runReportGetData("EmptyReport", params);

        assertThat(response).isNotNull();
        assertThat(response.getColumnHeaders()).isEmpty();
        assertThat(response.getData()).isEmpty();
    }

    @Test
    void testRunReportGetFileAsPdf() throws IOException {
        byte[] pdfContent = "%PDF-1.4 test content".getBytes(StandardCharsets.UTF_8);
        wireMockServer.stubFor(get(urlPathEqualTo("/v1/runreports/LoanSummary")).withQueryParam("output-type", equalTo("PDF"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/pdf")
                        .withHeader("Content-Disposition", "attachment; filename=\"report.pdf\"").withBody(pdfContent)));

        RunReportsApi api = config.createClient(RunReportsApi.class);
        Map<String, String> params = new HashMap<>();
        params.put("output-type", "PDF");

        Response response = api.runReportGetFile("LoanSummary", params);

        assertThat(response.status()).isEqualTo(200);
        assertThat(response.headers()).containsKey("Content-Type");
        assertThat(response.headers().get("Content-Type")).contains("application/pdf");
        assertThat(response.body().asInputStream().readAllBytes()).isEqualTo(pdfContent);
    }

    @Test
    void testRunReportGetFileAsCsv() throws IOException {
        String csvContent = "id,name,balance\n1,Client1,1000.00\n2,Client2,2000.00";
        wireMockServer.stubFor(get(urlPathEqualTo("/v1/runreports/ClientBalance")).withQueryParam("exportCSV", equalTo("true"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "text/csv")
                        .withHeader("Content-Disposition", "attachment; filename=\"report.csv\"").withBody(csvContent)));

        RunReportsApi api = config.createClient(RunReportsApi.class);
        Map<String, String> params = new HashMap<>();
        params.put("exportCSV", "true");

        Response response = api.runReportGetFile("ClientBalance", params);

        assertThat(response.status()).isEqualTo(200);
        assertThat(response.headers()).containsKey("Content-Type");
        assertThat(response.headers().get("Content-Type")).contains("text/csv");
        String content = new String(response.body().asInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertThat(content).isEqualTo(csvContent);
    }

    @Test
    void testRunReportGetFileAsExcel() throws IOException {
        byte[] excelContent = "fake excel data".getBytes(StandardCharsets.UTF_8);
        wireMockServer.stubFor(get(urlPathEqualTo("/v1/runreports/FinancialReport")).withQueryParam("output-type", equalTo("XLS"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/vnd.ms-excel")
                        .withHeader("Content-Disposition", "attachment; filename=\"report.xls\"").withBody(excelContent)));

        RunReportsApi api = config.createClient(RunReportsApi.class);
        Map<String, String> params = new HashMap<>();
        params.put("output-type", "XLS");

        Response response = api.runReportGetFile("FinancialReport", params);

        assertThat(response.status()).isEqualTo(200);
        assertThat(response.headers()).containsKey("Content-Type");
        assertThat(response.body().asInputStream().readAllBytes()).isEqualTo(excelContent);
    }

    @Test
    void testRunReportGetDataWithNoParameters() {
        wireMockServer.stubFor(get(urlPathEqualTo("/v1/runreports/AllClients")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("{\"columnHeaders\":[],\"data\":[]}")));

        RunReportsApi api = config.createClient(RunReportsApi.class);

        RunReportsResponse response = api.runReportGetData("AllClients", new HashMap<>());

        assertThat(response).isNotNull();
    }

}
