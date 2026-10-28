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
package org.apache.fineract.integrationtests.client.feign.tests;

import static org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants.IDEMPOTENCY_RECOVERABLE_ERROR_CODES;
import static org.apache.fineract.integrationtests.common.GlobalConfigurationHelper.IDEMPOTENCY_RECOVERABLE_ERROR_CODES_DEFAULT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.fineract.client.models.AuditData;
import org.apache.fineract.client.models.GlobalConfigurationPropertyData;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessDateHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGlobalConfigurationHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignStaffHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignUserHelper;
import org.apache.fineract.integrationtests.client.feign.modules.ClientRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.IdempotentCallOutcome;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.AuditHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

/**
 * A {@code POST /clients} that fails with a recoverable error (activation date ahead of a lagging business date) frees
 * its {@code Idempotency-Key}, so a retry with the same key runs again instead of replaying the cached failure.
 */
public class ClientActivationIdempotencyRetryIntegrationTest extends FeignIntegrationTest {

    private static final String BUSINESS_DATE = "BUSINESS_DATE";
    private static final String ENABLE_BUSINESS_DATE = "enable-business-date";
    private static final String LAGGED_BUSINESS_DATE = "14 January 2025";
    private static final String REQUEST_DATE = "15 January 2025";

    private static final String CLIENT_ACTIVATION_IN_FUTURE = "error.msg.clients.activationDate.in.the.future";
    private static final String CLIENT_SUBMITTED_IN_FUTURE = "error.msg.clients.submittedOnDate.in.the.future";
    private static final String GROUP_ACTIVATION_IN_FUTURE = "error.msg.group.activationDate.in.the.future";

    private static final Long SUPER_USER_ROLE_ID = 1L;
    private static final String USER_PASSWORD = "A1b2c3d4e5f$";
    private static final String CREATE = "CREATE";
    private static final String CLIENT = "CLIENT";
    private static final String ERROR = "Error";
    private static final String PROCESSED = "Processed";

    private FeignClientHelper clientHelper;
    private FeignBusinessDateHelper businessDateHelper;
    private FeignGlobalConfigurationHelper configHelper;
    private boolean originalBusinessDateEnabled;
    private LocalDate originalBusinessDate;

    @BeforeAll
    public void setupHelpers() {
        clientHelper = new FeignClientHelper(fineractClient());
        businessDateHelper = new FeignBusinessDateHelper(fineractClient());
        configHelper = new FeignGlobalConfigurationHelper(fineractClient());
        originalBusinessDateEnabled = Boolean.TRUE.equals(configHelper.getGlobalConfigurationByName(ENABLE_BUSINESS_DATE).getEnabled());
        originalBusinessDate = businessDateHelper.getBusinessDate(BUSINESS_DATE).getDate();
    }

    @BeforeEach
    public void lagBusinessDate() {
        configHelper.updateConfigurationByName(ENABLE_BUSINESS_DATE, true);
        moveBusinessDateTo(LAGGED_BUSINESS_DATE);
    }

    @AfterEach
    public void restoreConfiguration() {
        if (configHelper.getGlobalConfigurationByName(IDEMPOTENCY_RECOVERABLE_ERROR_CODES) != null) {
            configHelper.updateConfigurationStringValue(IDEMPOTENCY_RECOVERABLE_ERROR_CODES, IDEMPOTENCY_RECOVERABLE_ERROR_CODES_DEFAULT);
            configHelper.updateConfigurationByName(IDEMPOTENCY_RECOVERABLE_ERROR_CODES, true);
        }
        if (originalBusinessDate != null) {
            businessDateHelper.updateBusinessDate(BUSINESS_DATE, originalBusinessDate.toString());
        }
        configHelper.updateConfigurationByName(ENABLE_BUSINESS_DATE, originalBusinessDateEnabled);
    }

    @Test
    @Order(1)
    void defaultRecoverableErrorCodes_enabledWithClientAndGroupActivationCodes() {
        assertRecoverableErrorCodesDefault();
    }

    @Test
    @Order(2)
    void retryAfterBusinessDateMoves_createsClientThenReplaysSuccessFromCache() {
        PostClientsRequest request = ClientRequestBuilders.createActivePersonClientWithExternalId(REQUEST_DATE);
        String key = UUID.randomUUID().toString();

        IdempotentCallOutcome<PostClientsResponse> first = createClient(request, key);
        assertFreshFailure(first, CLIENT_ACTIVATION_IN_FUTURE);

        moveBusinessDateTo(REQUEST_DATE);

        IdempotentCallOutcome<PostClientsResponse> retry = createClient(request, key);
        assertEquals(200, retry.status(), "Retry after the business date moved must run again, not replay the 400: " + retry);
        assertFalse(retry.servedFromCache(), "Retry after a recoverable failure must not be served from cache: " + retry);
        Long clientId = retry.data().getClientId();
        assertNotNull(clientId, "Successful retry must return the created clientId");

        IdempotentCallOutcome<PostClientsResponse> replay = createClient(request, key);
        assertEquals(200, replay.status(), "Third call must replay the success: " + replay);
        assertTrue(replay.servedFromCache(), "Third call must be served from cache: " + replay);
        assertEquals(clientId, replay.data().getClientId(), "Cached replay must return the clientId of the processed attempt");

        assertEquals(1, clientHelper.countClientsByExternalId(request.getExternalId()), "Exactly one client must exist");
    }

    @Test
    @Order(3)
    void twoRecoverableFailuresThenSuccess_everyAttemptKeptInAuditTrail() {
        String username = Utils.uniqueRandomStringGenerator("idemUser", 6);
        Long staffId = new FeignStaffHelper(fineractClient()).createStaff().getResourceId();
        Long makerId = FeignUserHelper.createUser(SUPER_USER_ROLE_ID, staffId, username, USER_PASSWORD).getResourceId();
        FeignClientHelper makerClientHelper = new FeignClientHelper(
                FineractFeignClientHelper.createNewFineractFeignClient(username, USER_PASSWORD));

        PostClientsRequest request = ClientRequestBuilders.createActivePersonClientWithExternalId(REQUEST_DATE);
        String key = UUID.randomUUID().toString();

        IdempotentCallOutcome<PostClientsResponse> first = createClient(makerClientHelper, request, key);
        assertFreshFailure(first, CLIENT_ACTIVATION_IN_FUTURE);
        IdempotentCallOutcome<PostClientsResponse> second = createClient(makerClientHelper, request, key);
        assertFreshFailure(second, CLIENT_ACTIVATION_IN_FUTURE);

        moveBusinessDateTo(REQUEST_DATE);

        IdempotentCallOutcome<PostClientsResponse> third = createClient(makerClientHelper, request, key);
        assertEquals(200, third.status(), "Third attempt after the business date moved must succeed: " + third);
        assertFalse(third.servedFromCache(), "Third attempt must not be served from cache: " + third);
        Long clientId = third.data().getClientId();

        List<AuditData> audits = AuditHelper.getAuditDetailsByMaker(CREATE, CLIENT, makerId);
        assertEquals(3, audits.size(), "Each attempt must keep its own audit row: " + audits);
        assertAuditRow(audits.get(0), username, ERROR, null);
        assertAuditRow(audits.get(1), username, ERROR, null);
        assertAuditRow(audits.get(2), username, PROCESSED, clientId);
        assertTrue(audits.get(0).getId() < audits.get(1).getId() && audits.get(1).getId() < audits.get(2).getId(),
                "Audit rows must be distinct and in call order: " + audits);
    }

    @Test
    @Order(4)
    void concurrentRetriesAfterRecoverableFailure_createExactlyOneClient() throws Exception {
        PostClientsRequest request = ClientRequestBuilders.createActivePersonClientWithExternalId(REQUEST_DATE);
        String key = UUID.randomUUID().toString();

        assertFreshFailure(createClient(request, key), CLIENT_ACTIVATION_IN_FUTURE);

        moveBusinessDateTo(REQUEST_DATE);

        List<IdempotentCallOutcome<PostClientsResponse>> outcomes = createClientConcurrently(request, key);

        Map<Boolean, List<IdempotentCallOutcome<PostClientsResponse>>> byExecuted = outcomes.stream()
                .collect(Collectors.partitioningBy(o -> o.status() == 200 && !o.servedFromCache()));
        List<IdempotentCallOutcome<PostClientsResponse>> executed = byExecuted.get(true);
        assertEquals(1, executed.size(), "Exactly one concurrent retry must execute and return a fresh 200: " + outcomes);
        Long clientId = executed.get(0).data().getClientId();
        assertNotNull(clientId, "The executed retry must return the created clientId");

        IdempotentCallOutcome<PostClientsResponse> other = byExecuted.get(false).get(0);
        assertTrue(other.servedFromCache(), "The other concurrent retry must be answered by the idempotency layer: " + other);
        if (other.status() == 200) {
            assertEquals(clientId, other.data().getClientId(), "A cached replay must return the executed attempt's clientId");
        } else {
            assertEquals(425, other.status(), "The other concurrent retry must be 425 (under processing) or a cached 200: " + other);
        }

        assertEquals(1, clientHelper.countClientsByExternalId(request.getExternalId()), "Exactly one client must be created");
    }

    @Test
    @Order(5)
    void unlistedErrorCode_retryReplaysCachedFailure() {
        PostClientsRequest request = ClientRequestBuilders.createPendingClient(REQUEST_DATE);
        String key = UUID.randomUUID().toString();

        IdempotentCallOutcome<PostClientsResponse> first = createClient(request, key);
        assertFreshFailure(first, CLIENT_SUBMITTED_IN_FUTURE);

        moveBusinessDateTo(REQUEST_DATE);

        IdempotentCallOutcome<PostClientsResponse> retry = createClient(request, key);
        assertCachedReplayOf(first, retry);
        assertEquals(0, clientHelper.countClientsByExternalId(request.getExternalId()), "No client may be created");
    }

    @Test
    @Order(6)
    void errorCodeRemovedFromConfiguration_retryReplaysCachedFailure() {
        assertRecoverableErrorCodesDefault();
        configHelper.updateConfigurationStringValue(IDEMPOTENCY_RECOVERABLE_ERROR_CODES, GROUP_ACTIVATION_IN_FUTURE);

        assertActivationFailureReplayedAfterBusinessDateMoves();
    }

    @Test
    @Order(7)
    void recoverableErrorCodesDisabled_retryReplaysCachedFailure() {
        assertRecoverableErrorCodesDefault();
        configHelper.updateConfigurationByName(IDEMPOTENCY_RECOVERABLE_ERROR_CODES, false);

        assertActivationFailureReplayedAfterBusinessDateMoves();
    }

    @Test
    @Order(8)
    void renamedKeyCollidesWithLiteralClientKey_failureMovesToNextFreeNumberAndRetryExecutes() {
        String key = UUID.randomUUID().toString();
        String literalKey = key + "_RE2";
        PostClientsRequest literalRequest = ClientRequestBuilders.createActivePersonClientWithExternalId(LAGGED_BUSINESS_DATE);
        IdempotentCallOutcome<PostClientsResponse> literal = createClient(literalRequest, literalKey);
        assertEquals(200, literal.status(), "The literal-key request must succeed on the lagged business date: " + literal);
        assertFalse(literal.servedFromCache(), "The literal-key request must execute: " + literal);
        Long literalClientId = literal.data().getClientId();

        PostClientsRequest request = ClientRequestBuilders.createActivePersonClientWithExternalId(REQUEST_DATE);
        IdempotentCallOutcome<PostClientsResponse> first = createClient(request, key);
        assertFreshFailure(first, CLIENT_ACTIVATION_IN_FUTURE);

        moveBusinessDateTo(REQUEST_DATE);

        IdempotentCallOutcome<PostClientsResponse> retry = createClient(request, key);
        assertEquals(200, retry.status(), "Retry must run again despite the rename collision: " + retry);
        assertFalse(retry.servedFromCache(), "Retry must not be served from cache: " + retry);
        assertNotNull(retry.data().getClientId(), "Successful retry must return the created clientId");
        assertEquals(1, clientHelper.countClientsByExternalId(request.getExternalId()), "Exactly one client must be created");

        IdempotentCallOutcome<PostClientsResponse> literalReplay = createClient(literalRequest, literalKey);
        assertEquals(200, literalReplay.status(), "The literal key must keep its own result: " + literalReplay);
        assertTrue(literalReplay.servedFromCache(), "The literal key must be served from cache: " + literalReplay);
        assertEquals(literalClientId, literalReplay.data().getClientId(), "The literal key must replay its own clientId");
        assertEquals(1, clientHelper.countClientsByExternalId(literalRequest.getExternalId()), "The literal-key client must exist once");

        assertCachedReplayOf(first, createClient(request, key + "_RE3"));
    }

    private void assertActivationFailureReplayedAfterBusinessDateMoves() {
        PostClientsRequest request = ClientRequestBuilders.createActivePersonClientWithExternalId(REQUEST_DATE);
        String key = UUID.randomUUID().toString();

        IdempotentCallOutcome<PostClientsResponse> first = createClient(request, key);
        assertFreshFailure(first, CLIENT_ACTIVATION_IN_FUTURE);

        moveBusinessDateTo(REQUEST_DATE);

        IdempotentCallOutcome<PostClientsResponse> retry = createClient(request, key);
        assertCachedReplayOf(first, retry);
        assertEquals(0, clientHelper.countClientsByExternalId(request.getExternalId()), "No client may be created");
    }

    private void assertRecoverableErrorCodesDefault() {
        GlobalConfigurationPropertyData config = configHelper.getGlobalConfigurationByName(IDEMPOTENCY_RECOVERABLE_ERROR_CODES);
        assertNotNull(config, "Global configuration '" + IDEMPOTENCY_RECOVERABLE_ERROR_CODES + "' must exist");
        assertEquals(Boolean.TRUE, config.getEnabled(), "Recoverable error codes must be enabled by default");
        assertEquals(IDEMPOTENCY_RECOVERABLE_ERROR_CODES_DEFAULT, config.getStringValue(), "Default recoverable error codes");
    }

    private static void assertFreshFailure(IdempotentCallOutcome<PostClientsResponse> outcome, String expectedCode) {
        assertEquals(400, outcome.status(), "Expected a validation failure: " + outcome);
        assertFalse(outcome.servedFromCache(), "The failure must come from executing the command, not from cache: " + outcome);
        assertEquals(List.of(expectedCode), outcome.errorCodes(), "Validation error codes");
    }

    private static void assertCachedReplayOf(IdempotentCallOutcome<PostClientsResponse> original,
            IdempotentCallOutcome<PostClientsResponse> retry) {
        assertEquals(400, retry.status(), "A non-recoverable failure must be replayed as 400: " + retry);
        assertTrue(retry.servedFromCache(), "A non-recoverable failure must be served from cache: " + retry);
        assertEquals(original.responseBody(), retry.responseBody(), "The replay must return the original failure body");
    }

    private static void assertAuditRow(AuditData audit, String maker, String processingResult, Long resourceId) {
        assertEquals(CREATE, audit.getActionName(), "actionName of " + audit);
        assertEquals(CLIENT, audit.getEntityName(), "entityName of " + audit);
        assertEquals(maker, audit.getMaker(), "maker of " + audit);
        assertEquals(processingResult, audit.getProcessingResult(), "processingResult of " + audit);
        if (resourceId == null) {
            assertNull(audit.getResourceId(), "A failed attempt has no resourceId: " + audit);
        } else {
            assertEquals(resourceId, audit.getResourceId(), "resourceId of " + audit);
        }
    }

    private IdempotentCallOutcome<PostClientsResponse> createClient(PostClientsRequest request, String key) {
        return createClient(clientHelper, request, key);
    }

    private static IdempotentCallOutcome<PostClientsResponse> createClient(FeignClientHelper helper, PostClientsRequest request,
            String key) {
        return IdempotentCallOutcome.capture(() -> helper.createClientWithIdempotencyKey(request, key));
    }

    private List<IdempotentCallOutcome<PostClientsResponse>> createClientConcurrently(PostClientsRequest request, String key)
            throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            CountDownLatch ready = new CountDownLatch(2);
            CountDownLatch start = new CountDownLatch(1);
            List<Future<IdempotentCallOutcome<PostClientsResponse>>> futures = List.of(executor.submit(() -> {
                ready.countDown();
                start.await();
                return createClient(request, key);
            }), executor.submit(() -> {
                ready.countDown();
                start.await();
                return createClient(request, key);
            }));
            assertTrue(ready.await(30, TimeUnit.SECONDS), "Both requests must be ready to start");
            start.countDown();
            return List.of(futures.get(0).get(120, TimeUnit.SECONDS), futures.get(1).get(120, TimeUnit.SECONDS));
        } finally {
            executor.shutdownNow();
        }
    }

    private void moveBusinessDateTo(String date) {
        businessDateHelper.updateBusinessDate(BUSINESS_DATE, date, LoanTestData.DATETIME_PATTERN);
    }
}
