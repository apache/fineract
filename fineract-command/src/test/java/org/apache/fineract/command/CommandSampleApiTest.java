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
package org.apache.fineract.command;

import static org.apache.fineract.command.core.CommandConstants.COMMAND_REQUEST_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.command.sample.data.DummyRequest;
import org.apache.fineract.command.sample.data.DummyResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ProblemDetail;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.test.context.ContextConfiguration;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = TestConfiguration.class)
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
class CommandSampleApiTest extends CommandBaseTest {

    @LocalServerPort
    private int port;

    private String baseUrl;

    private List<ClientHttpRequestInterceptor> interceptors;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    public void setUp() {
        this.baseUrl = "http://localhost:" + port + "/test/dummy";
        this.interceptors = Collections.singletonList((request, body, execution) -> {
            var headers = request.getHeaders();
            headers.add(COMMAND_REQUEST_ID, UUID.randomUUID().toString());
            headers.add("x-fineract-tenant-id", "dummy");
            headers.add(CONTENT_TYPE, APPLICATION_JSON_VALUE);
            headers.addAll(ACCEPT, List.of(APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE));
            return execution.execute(request, body);
        });
    }

    @Test
    void validation() {
        restTemplate.getRestTemplate().setInterceptors(interceptors);
        var problemDetail = restTemplate.postForObject(baseUrl + "/sync", DummyRequest.builder().build(), ProblemDetail.class);

        log.warn("Problem detail (sync) : {} ({})", problemDetail.getDetail(), problemDetail.getProperties());

        assertNotNull(problemDetail, "Response should not be null.");
    }

    @Test
    void dummyApiSync() {
        var content = "test-sync";

        restTemplate.getRestTemplate().setInterceptors(interceptors);
        var result = restTemplate.postForObject(baseUrl + "/sync", DummyRequest.builder().content(content).build(), DummyResponse.class);

        log.warn("Result (sync) : {} ({})", result.getContent(), result.getRequestId());

        assertNotNull(result, "Response should not be null.");
        assertNotNull(result.getContent(), "Response body should not be null.");
        assertNotNull(result.getRequestId(), "Request ID should not be null.");
        assertNotNull(result.getTenantId(), "Tenant ID should not be null.");
        assertEquals("dummy", result.getTenantId(), "Unexpected tenant ID.");
        assertEquals(content.toUpperCase(Locale.ROOT), result.getContent(), "Wrong response content.");
    }

    @Test
    void dummyApiAsync() {
        var content = "test-async";

        restTemplate.getRestTemplate().setInterceptors(interceptors);
        var result = restTemplate.postForObject(baseUrl + "/async", DummyRequest.builder().content(content).build(), DummyResponse.class);

        log.warn("Result (async): {} ({})", result.getContent(), result.getRequestId());

        assertNotNull(result, "Response should not be null.");
        assertNotNull(result.getContent(), "Response body should not be null.");
        assertNotNull(result.getRequestId(), "Request ID should not be null.");
        // TODO: make sure all ThreadLocal variables are available
        // assertNotNull(result.getTenantId(), "Tenant ID should not be null.");
        // assertEquals("dummy", result.getTenantId(), "Unexpected tenant ID.");
        assertEquals(content.toUpperCase(Locale.ROOT), result.getContent(), "Wrong response content.");
    }

    @Test
    void dummyApiIdempotencyAsync() {
        dummyApiIdempotency("/async");
    }

    @Test
    void dummyApiIdempotencySync() {
        dummyApiIdempotency("/sync");
    }

    void dummyApiIdempotency(String path) {
        var id = UUID.randomUUID().toString();
        var content = "test-idempotency";
        var request = DummyRequest.builder().content(content).build();
        List<ClientHttpRequestInterceptor> interceptors = Collections.singletonList((req, body, execution) -> {
            var headers = req.getHeaders();
            headers.add(COMMAND_REQUEST_ID, id);
            headers.add("x-fineract-tenant-id", "dummy");
            headers.add(CONTENT_TYPE, APPLICATION_JSON_VALUE);
            headers.addAll(ACCEPT, List.of(APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE));
            return execution.execute(req, body);
        });

        restTemplate.getRestTemplate().setInterceptors(interceptors);

        // first request passes
        var response = restTemplate.postForEntity(baseUrl + path, request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(OK);

        // second request fails, because we are using the same request ID in both cases
        response = restTemplate.postForEntity(baseUrl + path, request, Map.class);

        log.warn("Body: {} - {}", response.getBody(), response.getStatusCode());

        // Assert HTTP status
        assertThat(response.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);

        log.info("Idempotency all good!");
    }
}
