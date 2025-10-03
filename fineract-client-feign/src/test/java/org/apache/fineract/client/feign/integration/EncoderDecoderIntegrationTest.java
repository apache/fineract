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
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import feign.RequestLine;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.fineract.client.feign.FineractFeignClientConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
class EncoderDecoderIntegrationTest {

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
    void testComplexObjectSerialization() {
        wireMockServer.stubFor(post(urlEqualTo("/complex")).willReturn(aResponse().withStatus(201)
                .withHeader("Content-Type", "application/json").withBody("{\"id\":100,\"status\":\"created\"}")));

        ComplexApi client = config.createClient(ComplexApi.class);

        ComplexRequest request = new ComplexRequest();
        request.setName("complex-test");
        request.setAmount(new BigDecimal("1234.56"));
        request.setCreatedDate(LocalDate.of(2024, 1, 15));
        request.setItems(Arrays.asList("item1", "item2", "item3"));

        Map<String, String> metadata = new HashMap<>();
        metadata.put("key1", "value1");
        metadata.put("key2", "value2");
        request.setMetadata(metadata);

        NestedObject nested = new NestedObject();
        nested.setNestedName("nested-value");
        nested.setNestedDate(LocalDate.of(2024, 2, 20));
        request.setNested(nested);

        SimpleResponse response = client.createComplex(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getStatus()).isEqualTo("created");
    }

    @Test
    void testNullValueHandling() {
        wireMockServer.stubFor(post(urlEqualTo("/complex")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("{\"id\":1,\"status\":null}")));

        ComplexApi client = config.createClient(ComplexApi.class);

        ComplexRequest request = new ComplexRequest();
        request.setName("null-test");
        request.setAmount(null);
        request.setCreatedDate(null);
        request.setItems(null);
        request.setMetadata(null);
        request.setNested(null);

        SimpleResponse response = client.createComplex(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getStatus()).isNull();
    }

    @Test
    void testDateFormats() {
        String responseJson = "{\"id\":1,\"date\":\"2024-03-15\",\"dateTime\":\"2024-03-15T14:30:00\",\"timestamp\":\"2024-03-15T14:30:00.123\"}";

        wireMockServer.stubFor(post(urlEqualTo("/date-test"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(responseJson)));

        ComplexApi client = config.createClient(ComplexApi.class);

        ComplexRequest request = new ComplexRequest();
        request.setName("date-test");
        request.setCreatedDate(LocalDate.of(2024, 3, 15));

        DateResponse response = client.testDates(request);

        assertThat(response).isNotNull();
        assertThat(response.getDate()).isEqualTo(LocalDate.of(2024, 3, 15));
        assertThat(response.getDateTime()).isEqualTo(LocalDateTime.of(2024, 3, 15, 14, 30, 0));
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    void testLargePayload() {
        StringBuilder largeString = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largeString.append("This is line ").append(i).append(". ");
        }

        wireMockServer.stubFor(post(urlEqualTo("/complex")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody("{\"id\":1,\"status\":\"processed\"}")));

        ComplexApi client = config.createClient(ComplexApi.class);

        ComplexRequest request = new ComplexRequest();
        request.setName(largeString.toString());
        request.setAmount(new BigDecimal("999999.99"));
        request.setCreatedDate(LocalDate.now(ZoneId.systemDefault()));

        SimpleResponse response = client.createComplex(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("processed");
    }

    @Test
    void testEmptyCollections() {
        wireMockServer.stubFor(post(urlEqualTo("/complex")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("{\"id\":1,\"status\":\"ok\"}")));

        ComplexApi client = config.createClient(ComplexApi.class);

        ComplexRequest request = new ComplexRequest();
        request.setName("empty-test");
        request.setItems(Arrays.asList());
        request.setMetadata(new HashMap<>());

        SimpleResponse response = client.createComplex(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("ok");
    }

    @Test
    void testBigDecimalPrecision() {
        wireMockServer.stubFor(post(urlEqualTo("/complex")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("{\"id\":1,\"status\":\"precise\"}")));

        ComplexApi client = config.createClient(ComplexApi.class);

        ComplexRequest request = new ComplexRequest();
        request.setName("precision-test");
        request.setAmount(new BigDecimal("123456789.123456789"));

        SimpleResponse response = client.createComplex(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("precise");
    }

    @Test
    void testNestedObjectDeserialization() {
        String responseJson = "{\"id\":1,\"nested\":{\"nestedName\":\"deep-value\",\"nestedDate\":\"2024-04-01\"},\"items\":[\"a\",\"b\",\"c\"]}";

        wireMockServer.stubFor(post(urlEqualTo("/nested"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(responseJson)));

        ComplexApi client = config.createClient(ComplexApi.class);

        ComplexRequest request = new ComplexRequest();
        request.setName("nested-test");

        NestedResponse response = client.testNested(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getNested()).isNotNull();
        assertThat(response.getNested().getNestedName()).isEqualTo("deep-value");
        assertThat(response.getNested().getNestedDate()).isEqualTo(LocalDate.of(2024, 4, 1));
        assertThat(response.getItems()).containsExactly("a", "b", "c");
    }

    @Test
    void testSpecialCharacterHandling() {
        wireMockServer.stubFor(post(urlEqualTo("/complex")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("{\"id\":1,\"status\":\"special\"}")));

        ComplexApi client = config.createClient(ComplexApi.class);

        ComplexRequest request = new ComplexRequest();
        request.setName("Test with special chars: <>\"'&\n\t");

        SimpleResponse response = client.createComplex(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("special");
    }

    interface ComplexApi {

        @RequestLine("POST /complex")
        SimpleResponse createComplex(ComplexRequest request);

        @RequestLine("POST /date-test")
        DateResponse testDates(ComplexRequest request);

        @RequestLine("POST /nested")
        NestedResponse testNested(ComplexRequest request);
    }

    static class ComplexRequest {

        private String name;
        private BigDecimal amount;
        private LocalDate createdDate;
        private List<String> items;
        private Map<String, String> metadata;
        private NestedObject nested;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public LocalDate getCreatedDate() {
            return createdDate;
        }

        public void setCreatedDate(LocalDate createdDate) {
            this.createdDate = createdDate;
        }

        public List<String> getItems() {
            return items;
        }

        public void setItems(List<String> items) {
            this.items = items;
        }

        public Map<String, String> getMetadata() {
            return metadata;
        }

        public void setMetadata(Map<String, String> metadata) {
            this.metadata = metadata;
        }

        public NestedObject getNested() {
            return nested;
        }

        public void setNested(NestedObject nested) {
            this.nested = nested;
        }
    }

    static class NestedObject {

        private String nestedName;
        private LocalDate nestedDate;

        public String getNestedName() {
            return nestedName;
        }

        public void setNestedName(String nestedName) {
            this.nestedName = nestedName;
        }

        public LocalDate getNestedDate() {
            return nestedDate;
        }

        public void setNestedDate(LocalDate nestedDate) {
            this.nestedDate = nestedDate;
        }
    }

    static class SimpleResponse {

        private Long id;
        private String status;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    static class DateResponse {

        private Long id;
        private LocalDate date;
        private LocalDateTime dateTime;
        private LocalDateTime timestamp;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public LocalDateTime getDateTime() {
            return dateTime;
        }

        public void setDateTime(LocalDateTime dateTime) {
            this.dateTime = dateTime;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
    }

    static class NestedResponse {

        private Long id;
        private NestedObject nested;
        private List<String> items;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public NestedObject getNested() {
            return nested;
        }

        public void setNested(NestedObject nested) {
            this.nested = nested;
        }

        public List<String> getItems() {
            return items;
        }

        public void setItems(List<String> items) {
            this.items = items;
        }
    }
}
