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

import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.util.UUID;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignHookHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignOfficeHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HookIntegrationTest extends FeignIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(HookIntegrationTest.class);

    private static final String ECHO_WEBHOOK_BASE_URL = "http://echo-webhook.herokuapp.com:80/";
    private static final String UPDATED_PAYLOAD_URL = "http://localhost";
    private static final String NOTIFIED_OFFICE_ID_FIELD = "officeId";
    private static final LocalDate OFFICE_OPENING_DATE = LocalDate.of(2012, Month.JANUARY, 1);

    private static final int WEBHOOK_POLL_ATTEMPTS = 5;
    private static final Duration WEBHOOK_POLL_INTERVAL = Duration.ofSeconds(1);
    private static final Duration WEBHOOK_TIMEOUT = Duration.ofSeconds(5);

    private FeignHookHelper hookHelper;
    private FeignOfficeHelper officeHelper;
    private Long registeredHookId;

    @BeforeAll
    public void setup() {
        hookHelper = new FeignHookHelper(fineractClient());
        officeHelper = new FeignOfficeHelper(fineractClient());
    }

    /** A hook left registered makes every later office creation in the suite dispatch a webhook. */
    @AfterEach
    public void deleteRegisteredHook() {
        if (registeredHookId != null) {
            hookHelper.deleteHook(registeredHookId);
            registeredHookId = null;
        }
    }

    @Test
    @Disabled("echo-webhook.herokuapp.com went away with Heroku's free dynos in 2022; needs a replacement echo endpoint")
    public void shouldSendOfficeCreationNotification() {
        final String payloadUrl = ECHO_WEBHOOK_BASE_URL + UUID.randomUUID() + "/";

        registeredHookId = hookHelper.createHook(payloadUrl).getResourceId();
        Assertions.assertNotNull(registeredHookId);

        final Long createdOfficeId = officeHelper.createOffice(OFFICE_OPENING_DATE).getResourceId();
        Assertions.assertNotNull(createdOfficeId);

        Assertions.assertEquals(createdOfficeId, pollForNotifiedOfficeId(payloadUrl),
                "Equality check for created officeId and hook received payload officeId");
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

    private Long pollForNotifiedOfficeId(final String payloadUrl) {
        final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(WEBHOOK_TIMEOUT).build();
        for (int attempt = 1; attempt <= WEBHOOK_POLL_ATTEMPTS; attempt++) {
            try {
                final String json = readEchoedPayload(httpClient, payloadUrl);
                return JsonParser.parseString(json).getAsJsonObject().get(NOTIFIED_OFFICE_ID_FIELD).getAsLong();
            } catch (IOException | RuntimeException e) {
                LOG.info("Echo webhook not reachable on attempt {} - {}", attempt, e.getMessage());
            }
            sleepBetweenPolls();
        }
        throw new AssertionError("Echo webhook " + payloadUrl + " echoed nothing after " + WEBHOOK_POLL_ATTEMPTS + " attempts");
    }

    private String readEchoedPayload(final HttpClient httpClient, final String payloadUrl) throws IOException {
        final HttpRequest request = HttpRequest.newBuilder(URI.create(payloadUrl)).timeout(WEBHOOK_TIMEOUT).GET().build();
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    private void sleepBetweenPolls() {
        try {
            Thread.sleep(WEBHOOK_POLL_INTERVAL);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
