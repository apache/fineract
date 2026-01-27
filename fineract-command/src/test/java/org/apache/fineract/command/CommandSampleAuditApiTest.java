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
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.command.persistence.domain.CommandEntity;
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
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = { "fineract.command.executor=audit" })
@ContextConfiguration(classes = TestConfiguration.class)
class CommandSampleAuditApiTest extends CommandBaseTest {

    @LocalServerPort
    private int port;

    private String baseUrl;

    private List<ClientHttpRequestInterceptor> interceptors;

    @Autowired
    private TestRestTemplate restTemplate;

    private String id;

    @BeforeEach
    public void setUp() {
        this.baseUrl = "http://localhost:" + port + "/test/dummy";
        this.interceptors = Collections.singletonList((request, body, execution) -> {
            var headers = request.getHeaders();
            id = UUID.randomUUID().toString();
            headers.add(COMMAND_REQUEST_ID, id);
            headers.add("x-fineract-tenant-id", "dummy");
            headers.add("x-fineract-username", "dummy-user");
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
    void dummyApiAudit() {
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

        List<CommandEntity> allCommands = commandRepository.findAll();

        assertNotNull(allCommands, "All commands should not be null.");

    }

    @Test
    void auditPersisted() {
        var content = "test-audit";
        restTemplate.getRestTemplate().setInterceptors(interceptors);
        var request = DummyRequest.builder().content(content).build();
        var result = restTemplate.postForObject(baseUrl + "/sync", request, DummyResponse.class);

        log.warn("Result (sync) : {} ({})", result.getContent(), result.getRequestId());

        List<CommandEntity> allCommands = commandRepository.findAll();
        assertNotNull(allCommands, "All commands should not be null.");

        CommandEntity entity = allCommands.stream().filter(e -> id.equals(e.getCommandId().toString())).findFirst()
                .orElseThrow(() -> new AssertionError("No command audit row found for requestId=" + id));

        assertEquals("SUCCESS", entity.getResult(), "Audit result should be SUCCESS.");
        assertNotNull(entity.getPayload(), "Audit payload should be stored.");
        assertEquals("dummy", entity.getTenantId(), "Audit tenantId should match header.");
        assertEquals("dummy-user", entity.getUsername(), "Audit username should be test-user.");
        assertThat(entity.getErrorMessage()).as("Audit error message should be null on success").isNull();
    }
}
