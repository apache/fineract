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
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.common.ContentTypes.CONTENT_TYPE;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import feign.Response;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.fineract.client.feign.FineractFeignClientConfig;
import org.apache.fineract.client.feign.FineractMultipartEncoder.MultipartData;
import org.apache.fineract.client.feign.services.BulkImportTemplatesApi;
import org.apache.fineract.client.models.ApiResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
class BulkImportTemplatesApiIntegrationTest {

    private static final String EXCEL = "application/vnd.ms-excel";
    private static final byte[] WORKBOOK = new byte[] { (byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, 0x01, 0x02 };

    private WireMockServer wireMockServer;
    private BulkImportTemplatesApi api;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
        api = FineractFeignClientConfig.builder().baseUrl("http://localhost:" + wireMockServer.port()).credentials("test", "test")
                .connectTimeout(5, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build().createClient(BulkImportTemplatesApi.class);
    }

    @AfterEach
    void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    void testDownloadTemplateReturnsTheWorkbook() throws IOException {
        wireMockServer
                .stubFor(get(urlPathEqualTo("/v1/loans/repayments/downloadtemplate")).withQueryParam("dateFormat", equalTo("dd MMMM yyyy"))
                        .willReturn(aResponse().withStatus(200).withHeader(CONTENT_TYPE, EXCEL).withBody(WORKBOOK)));

        Response response = api.downloadTemplate("loans/repayments", Map.of("dateFormat", "dd MMMM yyyy"));

        assertThat(response.status()).isEqualTo(200);
        assertThat(response.body().asInputStream().readAllBytes()).isEqualTo(WORKBOOK);
        wireMockServer
                .verify(getRequestedFor(urlPathEqualTo("/v1/loans/repayments/downloadtemplate")).withHeader("Accept", equalTo(EXCEL)));
    }

    @Test
    void testUploadTemplateSendsTheWorkbookAsMultipart() {
        wireMockServer.stubFor(post(urlPathEqualTo("/v1/clients/uploadtemplate")).withQueryParam("legalFormType", equalTo("CLIENTS_ENTITY"))
                .willReturn(aResponse().withStatus(200).withHeader(CONTENT_TYPE, "application/json").withBody("42")));

        ApiResponse<Long> response = api.uploadTemplateWithHttpInfo("clients", Map.of("legalFormType", "CLIENTS_ENTITY"),
                new MultipartData().addFile("file", "Client.xls", WORKBOOK, EXCEL).addText("locale", "en").addText("dateFormat",
                        "dd MMMM yyyy"));

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getData()).isEqualTo(42L);
        wireMockServer.verify(
                postRequestedFor(urlPathEqualTo("/v1/clients/uploadtemplate")).withHeader(CONTENT_TYPE, containing("multipart/form-data"))
                        .withRequestBody(containing("filename=\"Client.xls\"")).withRequestBody(containing("name=\"dateFormat\"")));
    }

    @Test
    void testDownloadOutputTemplateReturnsTheWorkbook() throws IOException {
        wireMockServer.stubFor(get(urlEqualTo("/v1/imports/downloadOutputTemplate?importDocumentId=42"))
                .willReturn(aResponse().withStatus(200).withHeader(CONTENT_TYPE, EXCEL).withBody(WORKBOOK)));

        Response response = api.downloadOutputTemplate(42L);

        assertThat(response.status()).isEqualTo(200);
        assertThat(response.body().asInputStream().readAllBytes()).isEqualTo(WORKBOOK);
    }
}
