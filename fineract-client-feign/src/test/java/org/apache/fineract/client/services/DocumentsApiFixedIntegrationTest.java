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
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import feign.Response;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.fineract.client.feign.FineractFeignClientConfig;
import org.apache.fineract.client.feign.services.DocumentsApiFixed;
import org.apache.fineract.client.models.DeleteEntityTypeEntityIdDocumentsResponse;
import org.apache.fineract.client.models.DocumentData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@Tag("integration")
class DocumentsApiFixedIntegrationTest {

    @TempDir
    File tempDir;

    private WireMockServer wireMockServer;
    private FineractFeignClientConfig config;
    private File testDocumentFile;

    @BeforeEach
    void setUp() throws IOException {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
        config = FineractFeignClientConfig.builder().baseUrl("http://localhost:" + wireMockServer.port()).credentials("test", "test")
                .connectTimeout(5, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build();

        testDocumentFile = new File(tempDir, "test-doc.pdf");
        Files.write(testDocumentFile.toPath(), "%PDF-1.4 test content".getBytes(StandardCharsets.UTF_8));
    }

    @AfterEach
    void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    void testGetDocumentsForClient() {
        String responseBody = "[{\"id\":1,\"name\":\"Document1\",\"description\":\"Test document\"}]";
        wireMockServer.stubFor(get(urlEqualTo("/v1/clients/123/documents"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(responseBody)));

        DocumentsApiFixed api = config.createClient(DocumentsApiFixed.class);
        List<DocumentData> documents = api.retrieveAllDocuments("clients", 123L);

        assertThat(documents).isNotNull();
        assertThat(documents).hasSize(1);
        assertThat(documents.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void testGetDocumentsForLoan() {
        String responseBody = "[{\"id\":2,\"name\":\"LoanDocument\",\"description\":\"Loan agreement\"}]";
        wireMockServer.stubFor(get(urlEqualTo("/v1/loans/456/documents"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(responseBody)));

        DocumentsApiFixed api = config.createClient(DocumentsApiFixed.class);
        List<DocumentData> documents = api.retrieveAllDocuments("loans", 456L);

        assertThat(documents).isNotNull();
        assertThat(documents).hasSize(1);
        assertThat(documents.get(0).getName()).isEqualTo("LoanDocument");
    }

    @Test
    void testGetDocumentsEmpty() {
        wireMockServer.stubFor(get(urlEqualTo("/v1/clients/999/documents"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("[]")));

        DocumentsApiFixed api = config.createClient(DocumentsApiFixed.class);
        List<DocumentData> documents = api.retrieveAllDocuments("clients", 999L);

        assertThat(documents).isNotNull();
        assertThat(documents).isEmpty();
    }

    @Test
    void testGetSingleDocument() {
        String responseBody = "{\"id\":1,\"name\":\"Document1\",\"description\":\"Test document\",\"fileName\":\"doc.pdf\"}";
        wireMockServer.stubFor(get(urlEqualTo("/v1/clients/123/documents/1"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(responseBody)));

        DocumentsApiFixed api = config.createClient(DocumentsApiFixed.class);
        DocumentData document = api.getDocument("clients", 123L, 1L);

        assertThat(document).isNotNull();
        assertThat(document.getId()).isEqualTo(1L);
        assertThat(document.getName()).isEqualTo("Document1");
        assertThat(document.getFileName()).isEqualTo("doc.pdf");
    }

    @Test
    void testDownloadDocument() throws IOException {
        byte[] docContent = "PDF document content here".getBytes(StandardCharsets.UTF_8);
        wireMockServer.stubFor(get(urlEqualTo("/v1/clients/123/documents/456/attachment"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/pdf")
                        .withHeader("Content-Disposition", "attachment; filename=\"document.pdf\"").withBody(docContent)));

        DocumentsApiFixed api = config.createClient(DocumentsApiFixed.class);
        Response response = api.downloadFile("clients", 123L, 456L);

        assertThat(response.status()).isEqualTo(200);
        assertThat(response.headers()).containsKey("Content-Type");
        assertThat(response.headers().get("Content-Type")).contains("application/pdf");
        assertThat(response.body().asInputStream().readAllBytes()).isEqualTo(docContent);
    }

    @Test
    void testDeleteDocument() {
        String responseBody = "{\"resourceId\":456}";
        wireMockServer.stubFor(delete(urlEqualTo("/v1/clients/123/documents/456"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(responseBody)));

        DocumentsApiFixed api = config.createClient(DocumentsApiFixed.class);
        DeleteEntityTypeEntityIdDocumentsResponse response = api.deleteDocument("clients", 123L, 456L);

        assertThat(response).isNotNull();
        assertThat(response.getResourceId()).isEqualTo(456L);
    }

    @Test
    void testDeleteDocumentForLoan() {
        String responseBody = "{\"resourceId\":999}";
        wireMockServer.stubFor(delete(urlEqualTo("/v1/loans/789/documents/999"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(responseBody)));

        DocumentsApiFixed api = config.createClient(DocumentsApiFixed.class);
        DeleteEntityTypeEntityIdDocumentsResponse response = api.deleteDocument("loans", 789L, 999L);

        assertThat(response).isNotNull();
        assertThat(response.getResourceId()).isEqualTo(999L);
    }

    @Test
    void testMultipleEntityTypes() {
        String[] entityTypes = { "clients", "loans", "savings", "groups" };
        Long[] entityIds = { 100L, 200L, 300L, 400L };

        for (int i = 0; i < entityTypes.length; i++) {
            String entityType = entityTypes[i];
            Long entityId = entityIds[i];

            wireMockServer.stubFor(get(urlEqualTo("/v1/" + entityType + "/" + entityId + "/documents"))
                    .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("[]")));
        }

        DocumentsApiFixed api = config.createClient(DocumentsApiFixed.class);

        for (int i = 0; i < entityTypes.length; i++) {
            List<DocumentData> documents = api.retrieveAllDocuments(entityTypes[i], entityIds[i]);
            assertThat(documents).isNotNull();
            assertThat(documents).isEmpty();
        }
    }

    @Test
    void testDownloadTextDocument() throws IOException {
        String textContent = "Plain text document content";
        wireMockServer.stubFor(get(urlEqualTo("/v1/clients/123/documents/999/attachment"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "text/plain")
                        .withHeader("Content-Disposition", "attachment; filename=\"readme.txt\"").withBody(textContent)));

        DocumentsApiFixed api = config.createClient(DocumentsApiFixed.class);
        Response response = api.downloadFile("clients", 123L, 999L);

        assertThat(response.status()).isEqualTo(200);
        assertThat(response.headers().get("Content-Type")).contains("text/plain");
        String content = new String(response.body().asInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertThat(content).isEqualTo(textContent);
    }
}
