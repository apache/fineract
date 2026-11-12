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
package org.apache.fineract.test.stepdef.common;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FeignException;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.ApiResponse;
import org.apache.fineract.client.models.ClientAddressRequest;
import org.apache.fineract.client.models.GetClientsPageItemsResponse;
import org.apache.fineract.client.models.GetClientsResponse;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostOfficesResponse;
import org.apache.fineract.test.api.FineractClientConfiguration;
import org.apache.fineract.test.factory.ClientRequestFactory;
import org.apache.fineract.test.helper.ErrorResponse;
import org.apache.fineract.test.messaging.event.EventCheckHelper;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;

@Slf4j
@RequiredArgsConstructor
public class ClientStepDef extends AbstractStepDef {

    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    private static final String SERVED_FROM_CACHE_HEADER = "x-served-from-cache";

    private final FineractFeignClient fineractClient;
    private final ClientRequestFactory clientRequestFactory;
    private final EventCheckHelper eventCheckHelper;
    private final FineractClientConfiguration fineractClientConfiguration;

    /**
     * Captured outcome of a {@code POST /clients} attempt made with an {@code Idempotency-Key} header.
     */
    public record ClientCallOutcome(int status, boolean servedFromCache, Long clientId, String responseBody, List<String> errorCodes) {
    }

    @When("Admin creates a client with random data")
    public void createClientRandomFirstNameLastName() {
        PostClientsRequest clientsRequest = clientRequestFactory.defaultClientCreationRequest();

        PostClientsResponse response = ok(() -> fineractClient.clients().createClient(clientsRequest));
        testContext().set(TestContextKey.CLIENT_CREATE_RESPONSE, response);

        eventCheckHelper.clientEventCheck(response);
    }

    @When("Admin creates a client with random data in the last created office")
    public void createClientInLastCreatedOffice() {
        final PostOfficesResponse officeResponse = testContext().get(TestContextKey.OFFICE_CREATE_RESPONSE);
        assertThat(officeResponse).as("No office was created. Use 'Admin creates a new office' step first.").isNotNull();
        final PostClientsRequest clientsRequest = clientRequestFactory.defaultClientCreationRequest()//
                .officeId(officeResponse.getOfficeId());

        final PostClientsResponse response = ok(() -> fineractClient.clients().createClient(clientsRequest));
        testContext().set(TestContextKey.CLIENT_CREATE_RESPONSE, response);

        eventCheckHelper.clientEventCheck(response);
    }

    @When("Admin creates a client with Firstname {string} and Lastname {string}")
    public void createClient(String firstName, String lastName) {
        PostClientsRequest clientsRequest = clientRequestFactory.defaultClientCreationRequest().firstname(firstName).lastname(lastName);

        PostClientsResponse response = ok(() -> fineractClient.clients().createClient(clientsRequest));
        testContext().set(TestContextKey.CLIENT_CREATE_RESPONSE, response);
    }

    @When("Admin creates a client with Firstname {string} and Lastname {string} with address")
    public void createClientWithAddress(String firstName, String lastName) {
        Long addressTypeId = 15L;
        Long countryId = 17L;
        Long stateId = 18L;
        String city = "Budapest";
        boolean addressIsActive = true;
        String postalCode = "1000";

        ClientAddressRequest addressRequest = new ClientAddressRequest().postalCode(postalCode).city(city).countryId(countryId)
                .stateProvinceId(stateId).addressTypeId(addressTypeId).isActive(addressIsActive);

        PostClientsRequest clientsRequest = clientRequestFactory.defaultClientCreationRequest().firstname(firstName).lastname(lastName)
                .address(Collections.singletonList(addressRequest));

        PostClientsResponse response = ok(() -> fineractClient.clients().createClient(clientsRequest));
        testContext().set(TestContextKey.CLIENT_CREATE_RESPONSE, response);

    }

    @Then("Client is created successfully")
    public void checkClientCreatedSuccessfully() {
        PostClientsResponse response = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);

        assertThat(response.getClientId()).isNotNull();

        eventCheckHelper.clientEventCheck(response);
    }

    @When("Admin creates an active client with activation date {string} and idempotency key")
    public void createActiveClientWithIdempotencyKey(String activationDate) {
        PostClientsRequest request = clientRequestFactory.defaultClientCreationRequest().active(true).activationDate(activationDate);
        executeClientAttempt(request, UUID.randomUUID().toString(), TestContextKey.CLIENT_IDEMPOTENT_REQUEST,
                TestContextKey.CLIENT_IDEMPOTENT_KEY);
    }

    @When("Admin prepares an idempotency key")
    public void prepareIdempotencyKey() {
        testContext().set(TestContextKey.CLIENT_IDEMPOTENT_KEY, UUID.randomUUID().toString());
    }

    @When("Admin creates an active client with activation date {string} and the stored idempotency key")
    public void createActiveClientWithStoredIdempotencyKey(String activationDate) {
        PostClientsRequest request = clientRequestFactory.defaultClientCreationRequest().active(true).activationDate(activationDate);
        executeClientAttempt(request, storedKey(), TestContextKey.CLIENT_IDEMPOTENT_REQUEST, TestContextKey.CLIENT_IDEMPOTENT_KEY);
    }

    @When("Admin creates a pending client with submitted on date {string} and idempotency key")
    public void createPendingClientWithIdempotencyKey(String submittedOnDate) {
        PostClientsRequest request = clientRequestFactory.defaultClientCreationRequest().active(false).activationDate(null)
                .submittedOnDate(submittedOnDate);
        executeClientAttempt(request, UUID.randomUUID().toString(), TestContextKey.CLIENT_IDEMPOTENT_REQUEST,
                TestContextKey.CLIENT_IDEMPOTENT_KEY);
    }

    @When("Admin creates an active client with activation date {string} and idempotency key suffix {string}")
    // creates a second, independent client under the literal key "<stored key><suffix>"
    public void createActiveClientWithIdempotencyKeySuffix(String activationDate, String keySuffix) {
        String baseKey = storedKey();
        PostClientsRequest request = clientRequestFactory.defaultClientCreationRequest().active(true).activationDate(activationDate);
        executeClientAttempt(request, baseKey + keySuffix, TestContextKey.CLIENT_IDEMPOTENT_REQUEST_SECOND,
                TestContextKey.CLIENT_IDEMPOTENT_KEY_SECOND);
    }

    @When("Admin retries the stored client request with the stored idempotency key")
    public void retryStoredClientRequest() {
        executeClientAttempt(storedRequest(TestContextKey.CLIENT_IDEMPOTENT_REQUEST), storedKey(), TestContextKey.CLIENT_IDEMPOTENT_REQUEST,
                TestContextKey.CLIENT_IDEMPOTENT_KEY);
    }

    @When("Admin retries the stored second client request with the stored second idempotency key")
    public void retryStoredSecondClientRequest() {
        String key = testContext().get(TestContextKey.CLIENT_IDEMPOTENT_KEY_SECOND);
        assertThat(key).as("No second idempotency key was stored yet").isNotNull();
        executeClientAttempt(storedRequest(TestContextKey.CLIENT_IDEMPOTENT_REQUEST_SECOND), key,
                TestContextKey.CLIENT_IDEMPOTENT_REQUEST_SECOND, TestContextKey.CLIENT_IDEMPOTENT_KEY_SECOND);
    }

    @When("Created user creates an active client with activation date {string} and idempotency key")
    public void createdUserCreatesActiveClientWithIdempotencyKey(String activationDate) {
        PostClientsRequest request = clientRequestFactory.defaultClientCreationRequest().active(true).activationDate(activationDate);
        executeClientAttemptAsCreatedUser(request, UUID.randomUUID().toString());
    }

    @When("Created user retries the stored client request with the stored idempotency key")
    public void createdUserRetriesStoredClientRequest() {
        executeClientAttemptAsCreatedUser(storedRequest(TestContextKey.CLIENT_IDEMPOTENT_REQUEST), storedKey());
    }

    @When("Two admins create the client concurrently with the stored idempotency key")
    public void createClientConcurrentlyWithStoredKey() {
        PostClientsRequest request = storedRequest(TestContextKey.CLIENT_IDEMPOTENT_REQUEST);
        String key = storedKey();

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            CountDownLatch ready = new CountDownLatch(2);
            CountDownLatch start = new CountDownLatch(1);
            Runnable attempt = () -> {
                ready.countDown();
                try {
                    start.await(30, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(e);
                }
            };
            List<Future<ClientCallOutcome>> futures = List.of(executor.submit(() -> {
                attempt.run();
                return callCreateClient(request, key);
            }), executor.submit(() -> {
                attempt.run();
                return callCreateClient(request, key);
            }));

            boolean allReady = ready.await(30, TimeUnit.SECONDS);
            assertThat(allReady).as("Both concurrent client creation requests must be ready to start").isTrue();
            start.countDown();

            testContext().set(TestContextKey.CLIENT_IDEMPOTENT_CONCURRENT_ATTEMPTS,
                    List.of(futures.get(0).get(120, TimeUnit.SECONDS), futures.get(1).get(120, TimeUnit.SECONDS)));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while running concurrent client creation", e);
        } catch (Exception e) {
            throw new IllegalStateException("Concurrent client creation failed", e);
        } finally {
            executor.shutdownNow();
        }
    }

    @Then("The client creation attempt returned the following error:")
    public void clientAttemptReturnedError(DataTable table) {
        List<Map<String, String>> expectedErrors = table.asMaps();
        assertThat(expectedErrors).as("Exactly one expected error row must be provided").hasSize(1);
        Map<String, String> expectedError = expectedErrors.get(0);

        ClientCallOutcome outcome = lastOutcome();
        assertThat(outcome.status()).as("HTTP status of the client creation attempt")
                .isEqualTo(Integer.parseInt(expectedError.get("httpCode")));
        assertThat(outcome.errorCodes()).as("Error codes of the client creation attempt")
                .containsExactly(expectedError.get("errorMessage"));
    }

    @Then("The client creation attempt was not served from cache")
    public void clientAttemptNotFromCache() {
        assertThat(lastOutcome().servedFromCache()).as("The client creation attempt must not be served from cache").isFalse();
    }

    @Then("The client creation attempt was served from cache")
    public void clientAttemptFromCache() {
        assertThat(lastOutcome().servedFromCache()).as("The client creation attempt must be served from cache").isTrue();
    }

    @Then("The client creation attempt returned {int} and was not served from cache")
    public void clientAttemptStatusNotFromCache(int status) {
        ClientCallOutcome outcome = lastOutcome();
        assertThat(outcome.status()).as("HTTP status of the client creation attempt").isEqualTo(status);
        assertThat(outcome.servedFromCache()).as("The client creation attempt must not be served from cache").isFalse();
    }

    @Then("The client creation attempt returned a clientId")
    public void clientAttemptReturnedClientId() {
        ClientCallOutcome outcome = lastOutcome();
        assertThat(outcome.status()).as("HTTP status of the client creation attempt").isEqualTo(200);
        assertThat(outcome.clientId()).as("The client creation attempt must return a clientId").isNotNull();
        log.info("Client creation attempt returned clientId={}", outcome.clientId());
    }

    @Then("The client creation attempt returned {int} served from cache with the same clientId")
    public void clientAttemptCachedWithSameClientId(int status) {
        ClientCallOutcome outcome = lastOutcome();
        assertThat(outcome.status()).as("HTTP status of the client creation attempt").isEqualTo(status);
        assertThat(outcome.servedFromCache()).as("The client creation attempt must be served from cache").isTrue();

        Long storedClientId = storedSuccessClientId();
        assertThat(outcome.clientId()).as("Cached replay must return the clientId of the processed attempt").isEqualTo(storedClientId);
    }

    @Then("The client creation attempt returned the same failure body")
    public void clientAttemptSameFailureBody() {
        ClientCallOutcome outcome = lastOutcome();
        String firstFailureBody = testContext().get(TestContextKey.CLIENT_IDEMPOTENT_FIRST_FAILURE_BODY);
        assertThat(firstFailureBody).as("No failed client creation attempt body was stored").isNotNull();
        assertThat(outcome.responseBody()).as("Cached replay must return the original failure body").isEqualTo(firstFailureBody);
    }

    @Then("Exactly {int} clients exist with the stored externalId")
    @Then("Exactly {int} client exists with the stored externalId")
    public void countClientsWithStoredExternalId(int expectedCount) {
        assertClientCountByExternalId(storedRequest(TestContextKey.CLIENT_IDEMPOTENT_REQUEST), expectedCount);
    }

    @Then("Exactly {int} clients exist with the stored second externalId")
    @Then("Exactly {int} client exists with the stored second externalId")
    public void countClientsWithStoredSecondExternalId(int expectedCount) {
        assertClientCountByExternalId(storedRequest(TestContextKey.CLIENT_IDEMPOTENT_REQUEST_SECOND), expectedCount);
    }

    private void assertClientCountByExternalId(PostClientsRequest request, int expectedCount) {
        GetClientsResponse clients = ok(() -> fineractClient.clients().retrieveAllClients(null, request.getExternalId(), null, null, null,
                null, null, null, null, null, null, null, null, null));
        List<Long> ids = clients.getPageItems() == null ? List.of()
                : clients.getPageItems().stream().map(GetClientsPageItemsResponse::getId).toList();
        log.info("Clients found by externalId {}: ids={}", request.getExternalId(), ids);
        Integer actual = clients.getTotalFilteredRecords();
        assertThat(actual != null ? actual : ids.size()).as("Clients found by externalId %s", request.getExternalId())
                .isEqualTo(expectedCount);
    }

    @Then("Exactly one of the two attempts returned {int} and was not served from cache")
    public void exactlyOneConcurrentAttemptExecuted(int status) {
        List<ClientCallOutcome> outcomes = concurrentOutcomes();
        List<ClientCallOutcome> executed = outcomes.stream().filter(o -> o.status() == status && !o.servedFromCache()).toList();
        assertThat(executed).as("Exactly one concurrent attempt must execute and return a fresh %d: %s", status, outcomes).hasSize(1);
        assertThat(executed.get(0).clientId()).as("The executed attempt must return a clientId").isNotNull();
        testContext().set(TestContextKey.CLIENT_IDEMPOTENT_FIRST_SUCCESS_CLIENT_ID, executed.get(0).clientId());
        log.info("Concurrent attempts outcomes: {}", outcomes);
    }

    @Then("The other attempt returned one of the following:")
    public void otherConcurrentAttemptReturned(DataTable table) {
        List<ClientCallOutcome> outcomes = concurrentOutcomes();
        List<ClientCallOutcome> executed = outcomes.stream().filter(o -> o.status() == 200 && !o.servedFromCache()).toList();
        ClientCallOutcome other = outcomes.stream().filter(o -> !executed.contains(o)).findFirst()
                .orElseThrow(() -> new IllegalStateException("No non-executed attempt found in: " + outcomes));

        boolean matches = table.asMaps().stream().anyMatch(row -> other.status() == Integer.parseInt(row.get("httpCode"))
                && other.servedFromCache() == Boolean.parseBoolean(row.get("servedFromCache")));
        assertThat(matches).as("The other concurrent attempt must match one of the expected rows: %s", other).isTrue();

        if (other.status() == 200) {
            Long executedClientId = testContext().get(TestContextKey.CLIENT_IDEMPOTENT_FIRST_SUCCESS_CLIENT_ID);
            assertThat(other.clientId()).as("A cached replay must return the executed attempt's clientId").isEqualTo(executedClientId);
        }
    }

    private void executeClientAttemptAsCreatedUser(PostClientsRequest request, String idempotencyKey) {
        testContext().set(TestContextKey.CLIENT_IDEMPOTENT_REQUEST, request);
        testContext().set(TestContextKey.CLIENT_IDEMPOTENT_KEY, idempotencyKey);

        String username = testContext().get(TestContextKey.CREATED_SIMPLE_USER_USERNAME);
        String password = testContext().get(TestContextKey.CREATED_SIMPLE_USER_PASSWORD);
        FineractFeignClient userClient = fineractClientConfiguration.fineractFeignClientForUser(username, password);

        ClientCallOutcome outcome = callCreateClient(userClient, request, idempotencyKey);
        storeAttemptOutcome(outcome, TestContextKey.CLIENT_IDEMPOTENT_REQUEST);
    }

    private void executeClientAttempt(PostClientsRequest request, String idempotencyKey, String requestContextKey, String keyContextKey) {
        testContext().set(requestContextKey, request);
        testContext().set(keyContextKey, idempotencyKey);

        ClientCallOutcome outcome = callCreateClient(fineractClient, request, idempotencyKey);
        storeAttemptOutcome(outcome, requestContextKey);
    }

    private void storeAttemptOutcome(ClientCallOutcome outcome, String requestContextKey) {
        testContext().set(TestContextKey.CLIENT_IDEMPOTENT_LAST_ATTEMPT, outcome);
        testContext().set(TestContextKey.CLIENT_IDEMPOTENT_LAST_REQUEST_IS_SECOND,
                TestContextKey.CLIENT_IDEMPOTENT_REQUEST_SECOND.equals(requestContextKey));

        if (outcome.clientId() != null) {
            String successKey = TestContextKey.CLIENT_IDEMPOTENT_REQUEST_SECOND.equals(requestContextKey)
                    ? TestContextKey.CLIENT_IDEMPOTENT_SECOND_SUCCESS_CLIENT_ID
                    : TestContextKey.CLIENT_IDEMPOTENT_FIRST_SUCCESS_CLIENT_ID;
            if (testContext().<Long>get(successKey) == null) {
                testContext().set(successKey, outcome.clientId());
            }
        }
        if (outcome.status() >= 400 && outcome.responseBody() != null
                && testContext().<String>get(TestContextKey.CLIENT_IDEMPOTENT_FIRST_FAILURE_BODY) == null) {
            testContext().set(TestContextKey.CLIENT_IDEMPOTENT_FIRST_FAILURE_BODY, outcome.responseBody());
        }
    }

    private ClientCallOutcome callCreateClient(PostClientsRequest request, String idempotencyKey) {
        return callCreateClient(fineractClient, request, idempotencyKey);
    }

    private ClientCallOutcome callCreateClient(FineractFeignClient client, PostClientsRequest request, String idempotencyKey) {
        Map<String, String> headers = Map.of(IDEMPOTENCY_KEY_HEADER, idempotencyKey);
        try {
            ApiResponse<PostClientsResponse> response = client.clients().createClientWithHttpInfo(request, headers);
            PostClientsResponse data = response.getData();
            return new ClientCallOutcome(response.getStatusCode(), isServedFromCache(response.getHeaders()),
                    data != null ? data.getClientId() : null, null, List.of());
        } catch (FeignException e) {
            return new ClientCallOutcome(e.status(), isServedFromCache(e.headers()), null, e.responseBodyAsString(), errorCodes(e));
        }
    }

    private boolean isServedFromCache(Map<String, Collection<String>> headers) {
        return headers != null && headers.keySet().stream().anyMatch(SERVED_FROM_CACHE_HEADER::equalsIgnoreCase);
    }

    private List<String> errorCodes(FeignException e) {
        ErrorResponse errorResponse = ErrorResponse.fromFeignException(e);
        if (errorResponse.getErrors() == null || errorResponse.getErrors().isEmpty()) {
            return List.of();
        }
        return errorResponse.getErrors().stream().map(ErrorResponse.ErrorDetail::getUserMessageGlobalisationCode).toList();
    }

    private PostClientsRequest storedRequest(String requestContextKey) {
        PostClientsRequest request = testContext().get(requestContextKey);
        assertThat(request).as("No client request was stored under %s", requestContextKey).isNotNull();
        return request;
    }

    private String storedKey() {
        String key = testContext().get(TestContextKey.CLIENT_IDEMPOTENT_KEY);
        assertThat(key).as("No idempotency key was stored yet").isNotNull();
        return key;
    }

    private ClientCallOutcome lastOutcome() {
        ClientCallOutcome outcome = testContext().get(TestContextKey.CLIENT_IDEMPOTENT_LAST_ATTEMPT);
        assertThat(outcome).as("No client creation attempt was executed yet").isNotNull();
        return outcome;
    }

    private Long storedSuccessClientId() {
        boolean isSecond = Boolean.TRUE.equals(testContext().<Boolean>get(TestContextKey.CLIENT_IDEMPOTENT_LAST_REQUEST_IS_SECOND));
        Long clientId = testContext().get(isSecond ? TestContextKey.CLIENT_IDEMPOTENT_SECOND_SUCCESS_CLIENT_ID
                : TestContextKey.CLIENT_IDEMPOTENT_FIRST_SUCCESS_CLIENT_ID);
        assertThat(clientId).as("No successful client creation attempt was recorded yet").isNotNull();
        return clientId;
    }

    private List<ClientCallOutcome> concurrentOutcomes() {
        List<ClientCallOutcome> outcomes = testContext().get(TestContextKey.CLIENT_IDEMPOTENT_CONCURRENT_ATTEMPTS);
        assertThat(outcomes).as("No concurrent client creation attempts were executed yet").isNotNull();
        return outcomes;
    }
}
