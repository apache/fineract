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
package org.apache.fineract.client.feign.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import feign.RequestLine;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import org.apache.fineract.client.feign.FeignException;
import org.apache.fineract.client.feign.FineractFeignClientConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
class FineractFeignClientIntegrationTest {

    private WireMockServer wireMockServer;
    private String baseUrl;
    private FineractFeignClientConfig config;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
        baseUrl = "http://localhost:" + wireMockServer.port();

        config = FineractFeignClientConfig.builder().baseUrl(baseUrl).credentials("testuser", "testpass")
                .connectTimeout(5, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build();
    }

    @AfterEach
    void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    void testJsonEncoding() {
        wireMockServer
                .stubFor(post(urlEqualTo("/test")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":1,\"message\":\"success\",\"timestamp\":\"2024-01-15T10:30:00\"}")));

        TestApi client = config.createClient(TestApi.class);
        TestRequest request = new TestRequest();
        request.setName("test-name");
        request.setDate(LocalDate.of(2024, 1, 15));

        TestResponse response = client.createTest(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getMessage()).isEqualTo("success");
        assertThat(response.getTimestamp()).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
    }

    @Test
    void testJsonDecoding() {
        wireMockServer
                .stubFor(get(urlEqualTo("/test")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":42,\"message\":\"decoded-message\",\"timestamp\":\"2024-12-25T18:45:30\"}")));

        TestApi client = config.createClient(TestApi.class);
        TestResponse response = client.getTest();

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(42L);
        assertThat(response.getMessage()).isEqualTo("decoded-message");
        assertThat(response.getTimestamp()).isEqualTo(LocalDateTime.of(2024, 12, 25, 18, 45, 30));
    }

    @Test
    void testJava8DateTimeSerialization() {
        wireMockServer.stubFor(post(urlEqualTo("/test")).withRequestBody(containing("2024-06-30"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":99,\"message\":\"date-test\",\"timestamp\":\"2024-06-30T12:00:00\"}")));

        TestApi client = config.createClient(TestApi.class);
        TestRequest request = new TestRequest();
        request.setName("date-test");
        request.setDate(LocalDate.of(2024, 6, 30));

        TestResponse response = client.createTest(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(99L);
        assertThat(response.getTimestamp()).isNotNull();
        assertThat(response.getTimestamp().toLocalDate()).isEqualTo(LocalDate.of(2024, 6, 30));
    }

    @Test
    void testErrorDecoder() {
        wireMockServer.stubFor(
                get(urlEqualTo("/error")).willReturn(aResponse().withStatus(404).withHeader("Content-Type", "application/json").withBody(
                        "{\"developerMessage\":\"Resource not found\",\"httpStatusCode\":\"404\",\"defaultUserMessage\":\"Not Found\"}")));

        TestApi client = config.createClient(TestApi.class);

        assertThatThrownBy(client::getError).isInstanceOf(FeignException.class).hasMessageContaining("Resource not found");
    }

    @Test
    void testErrorDecoderWithServerError() {
        wireMockServer.stubFor(
                get(urlEqualTo("/error")).willReturn(aResponse().withStatus(500).withHeader("Content-Type", "application/json").withBody(
                        "{\"developerMessage\":\"Internal server error occurred\",\"httpStatusCode\":\"500\",\"defaultUserMessage\":\"Server Error\"}")));

        TestApi client = config.createClient(TestApi.class);

        assertThatThrownBy(client::getError).isInstanceOf(FeignException.class).hasMessageContaining("Internal server error occurred");
    }

    @Test
    void testConnectionTimeToLiveConfiguration() {
        FineractFeignClientConfig ttlConfig = FineractFeignClientConfig.builder().baseUrl(baseUrl).credentials("testuser", "testpass")
                .connectionTimeToLive(5, TimeUnit.MINUTES).build();

        wireMockServer
                .stubFor(get(urlEqualTo("/test")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":1,\"message\":\"ttl-test\",\"timestamp\":\"2024-01-01T00:00:00\"}")));

        TestApi client = ttlConfig.createClient(TestApi.class);
        TestResponse response = client.getTest();

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo("ttl-test");
    }

    @Test
    void testBasicAuthentication() {
        wireMockServer.stubFor(get(urlEqualTo("/test")).withHeader("Authorization", equalTo("Basic dGVzdHVzZXI6dGVzdHBhc3M="))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":1,\"message\":\"authenticated\",\"timestamp\":\"2024-01-01T00:00:00\"}")));

        TestApi client = config.createClient(TestApi.class);
        TestResponse response = client.getTest();

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo("authenticated");
    }

    @Test
    void testNullValueHandling() {
        wireMockServer
                .stubFor(post(urlEqualTo("/test")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":1,\"message\":null,\"timestamp\":\"2024-01-01T00:00:00\"}")));

        TestApi client = config.createClient(TestApi.class);
        TestRequest request = new TestRequest();
        request.setName("null-test");
        request.setDate(null);

        TestResponse response = client.createTest(request);

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isNull();
    }

    interface TestApi {

        @RequestLine("GET /test")
        TestResponse getTest();

        @RequestLine("POST /test")
        TestResponse createTest(TestRequest request);

        @RequestLine("GET /error")
        TestResponse getError();
    }

    static class TestRequest {

        private String name;
        private LocalDate date;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }
    }

    static class TestResponse {

        private Long id;
        private String message;
        private LocalDateTime timestamp;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
    }
}
