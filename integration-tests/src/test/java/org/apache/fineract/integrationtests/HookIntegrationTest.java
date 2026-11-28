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
package org.apache.fineract.integrationtests;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.awaitility.Awaitility.await;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.util.UUID;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignHookHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignOfficeHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignStaffHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HookIntegrationTest extends FeignIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(HookIntegrationTest.class);

    private static final String ECHO_WEBHOOK_BASE_URL = "http://echo-webhook.herokuapp.com:80/";
    private static final String UPDATED_PAYLOAD_URL = "http://localhost";
    private static final LocalDate OFFICE_OPENING_DATE = LocalDate.of(2012, Month.JANUARY, 1);

    private static final String ENTITY_HEADER = "X-Fineract-Entity";
    private static final String ACTION_HEADER = "X-Fineract-Action";
    private static final Duration WEBHOOK_TIMEOUT = Duration.ofSeconds(30);

    /**
     * Receives the webhooks. Fineract can run in a container, so the hooks point at
     * {@link ConfigProperties.ExternalServices#HOST}, from which it reaches this JVM.
     */
    @RegisterExtension
    static WireMockExtension webhookReceiver = WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

    private FeignHookHelper hookHelper;
    private FeignOfficeHelper officeHelper;
    private FeignStaffHelper staffHelper;
    private Long registeredHookId;

    @BeforeAll
    public void setup() {
        hookHelper = new FeignHookHelper(fineractClient());
        officeHelper = new FeignOfficeHelper(fineractClient());
        staffHelper = new FeignStaffHelper(fineractClient());
    }

    /** A hook left registered makes every later command of its event in the suite dispatch a webhook. */
    @AfterEach
    public void deleteRegisteredHook() {
        if (registeredHookId != null) {
            hookHelper.deleteHook(registeredHookId);
            registeredHookId = null;
        }
    }

    /** Offices still go through the legacy command processing, which puts the office at the top of the payload. */
    @Test
    public void shouldSendOfficeCreationNotification() {
        final String path = registerHookAt("OFFICE", "CREATE");

        final Long officeId = officeHelper.createOffice(OFFICE_OPENING_DATE).getResourceId();

        awaitWebhook(path, "OFFICE", "CREATE", "$.officeId", officeId);
    }

    /** Staff goes through the new command processing, which used to send no webhook for it (FINERACT-3335). */
    @Test
    public void shouldSendStaffCreationNotification() {
        final String path = registerHookAt("STAFF", "CREATE");

        final Long staffId = staffHelper.createStaff().getResourceId();

        awaitWebhook(path, "STAFF", "CREATE", "$.response.resourceId", staffId);
    }

    @Test
    public void createUpdateAndDeleteHook() {
        final String payloadUrl = ECHO_WEBHOOK_BASE_URL + UUID.randomUUID() + "/";

        registeredHookId = hookHelper.createHook(payloadUrl).getResourceId();
        Assertions.assertNotNull(registeredHookId);
        Assertions.assertEquals(registeredHookId, hookHelper.retrieveHook(registeredHookId).getId());
        Assertions.assertEquals(payloadUrl, hookHelper.retrievePayloadUrl(registeredHookId));
        LOG.info("---------------------SUCCESSFULLY CREATED AND VERIFIED HOOK------------------------- {}", registeredHookId);

        hookHelper.updateHook(registeredHookId, UPDATED_PAYLOAD_URL);
        Assertions.assertEquals(UPDATED_PAYLOAD_URL, hookHelper.retrievePayloadUrl(registeredHookId));
        LOG.info("---------------------SUCCESSFULLY UPDATED AND VERIFIED HOOK------------------------- {}", registeredHookId);

        hookHelper.deleteHook(registeredHookId);
        final CallFailedRuntimeException exception = hookHelper.retrieveHookExpectingError(registeredHookId);
        Assertions.assertEquals(404, exception.getStatus());
        LOG.info("---------------------SUCCESSFULLY DELETED AND VERIFIED HOOK------------------------- {}", registeredHookId);
        registeredHookId = null;
    }

    /** Registers a hook for one event at a path of its own, so that no other webhook can satisfy the test. */
    private String registerHookAt(final String entityName, final String actionName) {
        final String path = "/webhooks/" + UUID.randomUUID() + "/";
        webhookReceiver.stubFor(post(urlEqualTo(path)).willReturn(aResponse().withStatus(200)));

        final String payloadUrl = "http://" + ConfigProperties.ExternalServices.HOST + ":" + webhookReceiver.getPort() + path;
        registeredHookId = hookHelper.createHook(payloadUrl, entityName, actionName).getResourceId();
        Assertions.assertNotNull(registeredHookId);
        return path;
    }

    /** Hooks are notified after the command commits, off the request thread, so the webhook arrives a moment later. */
    private void awaitWebhook(final String path, final String entityName, final String actionName, final String idJsonPath, final Long id) {
        await().atMost(WEBHOOK_TIMEOUT)
                .untilAsserted(() -> webhookReceiver.verify(1,
                        postRequestedFor(urlEqualTo(path)).withHeader(ENTITY_HEADER, equalTo(entityName))
                                .withHeader(ACTION_HEADER, equalTo(actionName))
                                .withRequestBody(matchingJsonPath(idJsonPath, equalTo(String.valueOf(id))))));
    }
}
