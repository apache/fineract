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
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import feign.Response;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.fineract.client.feign.FineractFeignClientConfig;
import org.apache.fineract.client.feign.services.ImagesApi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@Tag("integration")
class ImagesApiIntegrationTest {

    @TempDir
    File tempDir;

    private WireMockServer wireMockServer;
    private FineractFeignClientConfig config;
    private File testImageFile;

    @BeforeEach
    void setUp() throws IOException {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
        config = FineractFeignClientConfig.builder().baseUrl("http://localhost:" + wireMockServer.port()).credentials("test", "test")
                .connectTimeout(5, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build();

        testImageFile = new File(tempDir, "test-image.jpg");
        Files.write(testImageFile.toPath(), new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x01, 0x02, 0x03 });
    }

    @AfterEach
    void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    void testRetrieveClientImage() throws IOException {
        byte[] imageData = new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x10, 0x11, 0x12 };
        wireMockServer.stubFor(get(urlEqualTo("/v1/clients/123/images"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "image/jpeg").withBody(imageData)));

        ImagesApi api = config.createClient(ImagesApi.class);
        Response response = api.get("clients", 123L, new HashMap<>());

        assertThat(response.status()).isEqualTo(200);
        assertThat(response.headers()).containsKey("Content-Type");
        assertThat(response.headers().get("Content-Type")).contains("image/jpeg");
        assertThat(response.body().asInputStream().readAllBytes()).isEqualTo(imageData);
    }

    @Test
    void testRetrieveClientImageWithMaxDimensions() throws IOException {
        byte[] resizedImage = new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x20, 0x21 };
        wireMockServer.stubFor(get(urlPathEqualTo("/v1/clients/123/images")).withQueryParam("maxWidth", equalTo("100"))
                .withQueryParam("maxHeight", equalTo("100"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "image/jpeg").withBody(resizedImage)));

        ImagesApi api = config.createClient(ImagesApi.class);
        Map<String, Object> params = new HashMap<>();
        params.put("maxWidth", 100);
        params.put("maxHeight", 100);

        Response response = api.get("clients", 123L, params);

        assertThat(response.status()).isEqualTo(200);
        assertThat(response.body().asInputStream().readAllBytes()).isEqualTo(resizedImage);
    }

    @Test
    void testRetrieveClientImageWithMaxWidthOnly() throws IOException {
        byte[] resizedImage = new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x30 };
        wireMockServer.stubFor(get(urlPathEqualTo("/v1/clients/123/images")).withQueryParam("maxWidth", equalTo("200"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "image/jpeg").withBody(resizedImage)));

        ImagesApi api = config.createClient(ImagesApi.class);
        Map<String, Object> params = new HashMap<>();
        params.put("maxWidth", 200);

        Response response = api.get("clients", 123L, params);

        assertThat(response.status()).isEqualTo(200);
        assertThat(response.body().asInputStream().readAllBytes()).isEqualTo(resizedImage);
    }

    @Test
    void testDeleteClientImage() {
        wireMockServer.stubFor(delete(urlEqualTo("/v1/clients/123/images"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("{\"resourceId\":123}")));

        ImagesApi api = config.createClient(ImagesApi.class);
        Response response = api.delete("clients", 123L);

        assertThat(response.status()).isEqualTo(200);
    }

    @Test
    void testDeleteStaffImage() {
        wireMockServer.stubFor(delete(urlEqualTo("/v1/staff/456/images"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("{\"resourceId\":456}")));

        ImagesApi api = config.createClient(ImagesApi.class);
        Response response = api.delete("staff", 456L);

        assertThat(response.status()).isEqualTo(200);
    }

    @Test
    void testRetrieveImageWithPngFormat() throws IOException {
        byte[] pngImage = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A };
        wireMockServer.stubFor(get(urlEqualTo("/v1/clients/123/images"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "image/png").withBody(pngImage)));

        ImagesApi api = config.createClient(ImagesApi.class);
        Response response = api.get("clients", 123L, new HashMap<>());

        assertThat(response.status()).isEqualTo(200);
        assertThat(response.headers().get("Content-Type")).contains("image/png");
        assertThat(response.body().asInputStream().readAllBytes()).isEqualTo(pngImage);
    }

    @Test
    void testMultipleEntityTypes() {
        String[] entityTypes = { "clients", "staff" };
        Long[] entityIds = { 100L, 200L };

        for (int i = 0; i < entityTypes.length; i++) {
            String entityType = entityTypes[i];
            Long entityId = entityIds[i];

            wireMockServer.stubFor(get(urlEqualTo("/v1/" + entityType + "/" + entityId + "/images"))
                    .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "image/jpeg").withBody(new byte[] { 0x01 })));
        }

        ImagesApi api = config.createClient(ImagesApi.class);

        for (int i = 0; i < entityTypes.length; i++) {
            Response response = api.get(entityTypes[i], entityIds[i], new HashMap<>());
            assertThat(response.status()).isEqualTo(200);
        }
    }

}
