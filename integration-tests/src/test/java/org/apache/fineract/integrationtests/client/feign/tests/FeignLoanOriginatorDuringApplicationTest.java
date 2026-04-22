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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.PostLoansOriginatorData;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGlobalConfigurationHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignLoanHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignLoanOriginatorHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

@Order(2)
public class FeignLoanOriginatorDuringApplicationTest extends FeignIntegrationTest {

    private static FeignLoanOriginatorHelper originatorHelper;
    private static FeignClientHelper clientHelper;
    private static FeignLoanHelper loanHelper;
    private static FeignGlobalConfigurationHelper configHelper;

    @BeforeAll
    public static void setup() {
        FineractFeignClient fineractClient = FineractFeignClientHelper.getFineractFeignClient();
        originatorHelper = new FeignLoanOriginatorHelper(fineractClient);
        clientHelper = new FeignClientHelper(fineractClient);
        loanHelper = new FeignLoanHelper(fineractClient);
        configHelper = new FeignGlobalConfigurationHelper(fineractClient);
    }

    @Test
    public void testCreateLoanWithExistingOriginatorById() {
        final String originatorExternalId = FeignLoanOriginatorHelper.generateUniqueExternalId();
        final Long originatorId = originatorHelper.createOriginator(originatorExternalId);
        final Long clientId = clientHelper.createClient();

        final List<PostLoansOriginatorData> originators = List.of(new PostLoansOriginatorData().id(originatorId));
        final Long loanId = loanHelper.createSubmittedLoanWithOriginators(clientId, originators);

        assertThat(loanId).isNotNull();
        final var loanDetails = loanHelper.getLoanDetailsWithAssociations(loanId, "originators");
        assertThat(loanDetails.getOriginators()).hasSize(1);
        assertThat(loanDetails.getOriginators().get(0).getId()).isEqualTo(originatorId);

        originatorHelper.detachOriginatorFromLoan(loanId, originatorId);
        originatorHelper.deleteOriginator(originatorId);
    }

    @Test
    public void testCreateLoanWithExistingOriginatorByExternalId() {
        final String originatorExternalId = FeignLoanOriginatorHelper.generateUniqueExternalId();
        final Long originatorId = originatorHelper.createOriginator(originatorExternalId);
        final Long clientId = clientHelper.createClient();

        final List<PostLoansOriginatorData> originators = List.of(new PostLoansOriginatorData().externalId(originatorExternalId));
        final Long loanId = loanHelper.createSubmittedLoanWithOriginators(clientId, originators);

        assertThat(loanId).isNotNull();
        final var loanDetails = loanHelper.getLoanDetailsWithAssociations(loanId, "originators");
        assertThat(loanDetails.getOriginators()).hasSize(1);
        assertThat(loanDetails.getOriginators().get(0).getExternalId()).isEqualTo(originatorExternalId);

        originatorHelper.detachOriginatorFromLoan(loanId, originatorId);
        originatorHelper.deleteOriginator(originatorId);
    }

    @Test
    public void testCreateLoanWithNewOriginatorWhenConfigEnabled() {
        configHelper.enableOriginatorCreationDuringLoanApplication();

        try {
            final Long clientId = clientHelper.createClient();
            final String newOriginatorExternalId = FeignLoanOriginatorHelper.generateUniqueExternalId();

            final List<PostLoansOriginatorData> originators = List
                    .of(new PostLoansOriginatorData().externalId(newOriginatorExternalId).name("New Merchant Created During Loan"));
            final Long loanId = loanHelper.createSubmittedLoanWithOriginators(clientId, originators);

            assertThat(loanId).isNotNull();

            final var createdOriginator = originatorHelper.getOriginatorByExternalId(newOriginatorExternalId);
            assertThat(createdOriginator).isNotNull();
            assertThat(createdOriginator.getName()).isEqualTo("New Merchant Created During Loan");
            assertThat(createdOriginator.getStatus()).isEqualTo("ACTIVE");

            final var loanDetails = loanHelper.getLoanDetailsWithAssociations(loanId, "originators");
            assertThat(loanDetails.getOriginators()).hasSize(1);
            assertThat(loanDetails.getOriginators().get(0).getExternalId()).isEqualTo(newOriginatorExternalId);

            originatorHelper.detachOriginatorFromLoan(loanId, createdOriginator.getId());
            originatorHelper.deleteOriginator(createdOriginator.getId());
        } finally {
            configHelper.disableOriginatorCreationDuringLoanApplication();
        }
    }

    @Test
    public void testCreateLoanWithNewOriginatorFailsWhenConfigDisabled() {
        configHelper.disableOriginatorCreationDuringLoanApplication();

        final Long clientId = clientHelper.createClient();
        final String nonExistingExternalId = FeignLoanOriginatorHelper.generateUniqueExternalId();

        final List<PostLoansOriginatorData> originators = List.of(new PostLoansOriginatorData().externalId(nonExistingExternalId));

        final CallFailedRuntimeException exception = loanHelper.createSubmittedLoanWithOriginatorsExpectingError(clientId, originators);

        assertThat(exception.getStatus()).isIn(403, 404);
    }

    @Test
    public void testCreateLoanWithMultipleOriginators() {
        final String externalId1 = FeignLoanOriginatorHelper.generateUniqueExternalId();
        final String externalId2 = FeignLoanOriginatorHelper.generateUniqueExternalId();
        final Long originatorId1 = originatorHelper.createOriginator(externalId1);
        final Long originatorId2 = originatorHelper.createOriginator(externalId2);
        final Long clientId = clientHelper.createClient();

        final List<PostLoansOriginatorData> originators = List.of(new PostLoansOriginatorData().id(originatorId1),
                new PostLoansOriginatorData().externalId(externalId2));
        final Long loanId = loanHelper.createSubmittedLoanWithOriginators(clientId, originators);

        final var loanDetails = loanHelper.getLoanDetailsWithAssociations(loanId, "originators");
        assertThat(loanDetails.getOriginators()).hasSize(2);

        originatorHelper.detachOriginatorFromLoan(loanId, originatorId1);
        originatorHelper.detachOriginatorFromLoan(loanId, originatorId2);
        originatorHelper.deleteOriginator(originatorId1);
        originatorHelper.deleteOriginator(originatorId2);
    }

    @Test
    public void testCreateLoanWithInvalidOriginatorDataReturns400() {
        final Long clientId = clientHelper.createClient();

        final List<PostLoansOriginatorData> originators = List.of(new PostLoansOriginatorData().name("Invalid - no id or externalId"));

        final CallFailedRuntimeException exception = loanHelper.createSubmittedLoanWithOriginatorsExpectingError(clientId, originators);

        assertThat(exception.getStatus()).isEqualTo(400);
    }

    @Test
    public void testCreateLoanWithInactiveOriginatorReturns403() {
        final String externalId = FeignLoanOriginatorHelper.generateUniqueExternalId();
        final Long originatorId = originatorHelper.createOriginator(externalId, "Inactive Originator", "INACTIVE");
        final Long clientId = clientHelper.createClient();

        final List<PostLoansOriginatorData> originators = List.of(new PostLoansOriginatorData().id(originatorId));

        final CallFailedRuntimeException exception = loanHelper.createSubmittedLoanWithOriginatorsExpectingError(clientId, originators);

        assertThat(exception.getStatus()).isEqualTo(403);

        originatorHelper.deleteOriginator(originatorId);
    }

    @Test
    public void testCreateLoanWithNonExistingOriginatorIdReturns404() {
        final Long clientId = clientHelper.createClient();

        final List<PostLoansOriginatorData> originators = List.of(new PostLoansOriginatorData().id(999999L));

        final CallFailedRuntimeException exception = loanHelper.createSubmittedLoanWithOriginatorsExpectingError(clientId, originators);

        assertThat(exception.getStatus()).isEqualTo(404);
    }

    @Test
    public void testCreateLoanWithoutOriginatorsStillWorks() {
        final Long clientId = clientHelper.createClient();

        final Long loanId = loanHelper.createSubmittedLoan(clientId);

        assertThat(loanId).isNotNull();

        final var loanDetails = loanHelper.getLoanDetailsWithAssociations(loanId, "originators");
        assertThat(loanDetails.getOriginators()).isEmpty();
    }

    @Test
    public void testCreateLoanWithDuplicateOriginatorInListAttachesOnce() {
        final String externalId = FeignLoanOriginatorHelper.generateUniqueExternalId();
        final Long originatorId = originatorHelper.createOriginator(externalId);
        final Long clientId = clientHelper.createClient();

        final List<PostLoansOriginatorData> originators = List.of(new PostLoansOriginatorData().id(originatorId),
                new PostLoansOriginatorData().externalId(externalId));
        final Long loanId = loanHelper.createSubmittedLoanWithOriginators(clientId, originators);

        final var loanDetails = loanHelper.getLoanDetailsWithAssociations(loanId, "originators");
        assertThat(loanDetails.getOriginators()).hasSize(1);

        originatorHelper.detachOriginatorFromLoan(loanId, originatorId);
        originatorHelper.deleteOriginator(originatorId);
    }

    @Test
    public void testCreateLoanWithSameOriginatorExternalIdInParallelShouldNotFail() throws InterruptedException {
        configHelper.enableOriginatorCreationDuringLoanApplication();

        try {
            final int threadCount = 10;
            final String sharedOriginatorExternalId = FeignLoanOriginatorHelper.generateUniqueExternalId();
            final Long productId = loanHelper.createSimpleLoanProduct();

            final List<Long> clientIds = new ArrayList<>();
            for (int i = 0; i < threadCount; i++) {
                clientIds.add(clientHelper.createClient());
            }

            final ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            final CountDownLatch startLatch = new CountDownLatch(1);
            final CountDownLatch doneLatch = new CountDownLatch(threadCount);
            final List<Long> results = Collections.synchronizedList(new ArrayList<>());
            final List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

            for (int i = 0; i < threadCount; i++) {
                final Long clientId = clientIds.get(i);
                executorService.execute(() -> {
                    try {
                        startLatch.await();
                        final List<PostLoansOriginatorData> originators = List.of(
                                new PostLoansOriginatorData().externalId(sharedOriginatorExternalId).name("Parallel Created Originator"));
                        final Long loanId = loanHelper.createSubmittedLoanWithOriginators(clientId, productId, originators);
                        results.add(loanId);
                    } catch (final Exception e) {
                        exceptions.add(e);
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            startLatch.countDown();
            assertTrue(doneLatch.await(30, TimeUnit.SECONDS), "All threads should complete within timeout");
            executorService.shutdown();
            assertTrue(executorService.awaitTermination(10, TimeUnit.SECONDS), "ExecutorService should terminate");

            assertTrue(exceptions.isEmpty(),
                    "Expected no exceptions but got " + exceptions.size() + ": " + exceptions.stream().map(Throwable::getMessage).toList());
            assertEquals(threadCount, results.size(), "All loan applications should succeed");

            // Verify all loans have the same originator attached
            for (final Long loanId : results) {
                final var loanDetails = loanHelper.getLoanDetailsWithAssociations(loanId, "originators");
                assertNotNull(loanDetails.getOriginators());
                assertEquals(1, loanDetails.getOriginators().size());
                assertEquals(sharedOriginatorExternalId, loanDetails.getOriginators().get(0).getExternalId(),
                        "All loans should reference the same originator");
            }

            // Cleanup
            final var createdOriginator = originatorHelper.getOriginatorByExternalId(sharedOriginatorExternalId);
            for (final Long loanId : results) {
                originatorHelper.detachOriginatorFromLoan(loanId, createdOriginator.getId());
            }
            originatorHelper.deleteOriginator(createdOriginator.getId());
        } finally {
            configHelper.disableOriginatorCreationDuringLoanApplication();
        }
    }
}
