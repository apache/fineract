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

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.ExecuteWorkingCapitalLoanTransactionCommandRequest;
import org.apache.fineract.client.models.ExecuteWorkingCapitalLoanTransactionCommandResponse;
import org.apache.fineract.client.models.GetBalance;
import org.apache.fineract.client.models.GetWorkingCapitalLoanTransactionIdResponse;
import org.apache.fineract.infrastructure.event.external.data.ExternalEventResponse;
import org.apache.fineract.integrationtests.client.feign.FeignWorkingCapitalTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalLoanRequestBuilders;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.Test;

/**
 * Aggregated repayments are adjusted again and again against the same transaction external id. Passing that external id
 * as the adjustment's externalId moves it to the replacement, and a command sent by external id resolves it while it
 * runs - on every retry attempt - so concurrent adjustments each land once on the live transaction.
 */
public class FeignWorkingCapitalLoanAdjustExternalIdInheritanceTest extends FeignWorkingCapitalTestBase {

    private static final String BUSINESS_DATE = "2026-01-10";
    private static final String DISBURSE_DATE = "01 January 2026";
    private static final String REPAYMENT_DATE = "05 January 2026";
    private static final BigDecimal PRINCIPAL = new BigDecimal("9000");
    private static final BigDecimal REPAYMENT_AMOUNT = new BigDecimal("100");
    private static final String ADJUST_BY_DELTA = "adjustByDelta";
    private static final String ADJUST = "adjust";
    private static final String ADJUST_TRANSACTION_EVENT = "WorkingCapitalLoanAdjustTransactionBusinessEvent";
    private static final String REPAYMENT_TRANSACTION_EVENT = "WorkingCapitalLoanRepaymentTransactionBusinessEvent";
    private static final String ALREADY_UNDONE_CODE = "error.msg.wc.loan.transaction.already.undone";

    private final FineractFeignClient client = FineractFeignClientHelper.getFineractFeignClient();

    @Test
    public void adjustByDeltaWithOwnExternalIdMovesItToTheReplacement() {
        runAt(BUSINESS_DATE, () -> {
            final Long loanId = createLoan();
            final String externalId = uniqueExternalId();
            final Long originalId = repay(loanId, externalId);

            final ExecuteWorkingCapitalLoanTransactionCommandResponse response = adjustByExternalId(loanId, externalId, ADJUST_BY_DELTA,
                    deltaRequest("-5", externalId));

            assertEquals(originalId, response.getSubResourceId(), "the response names the transaction that was reversed");
            assertEquals(externalId, response.getResourceExternalId());
            final GetWorkingCapitalLoanTransactionIdResponse reversed = wcLoanHelper.getTransaction(loanId, originalId);
            assertTrue(reversed.getReversed());
            assertNull(reversed.getExternalId(), "the reversed transaction gives its external id up");

            final GetWorkingCapitalLoanTransactionIdResponse live = byExternalId(loanId, externalId);
            assertEquals(response.getResourceId(), live.getId());
            assertFalse(live.getReversed());
            assertEquals(0, new BigDecimal("95").compareTo(live.getTransactionAmount()));
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    public void adjustmentEventShowsTheExternalIdMove() {
        final Map<String, Boolean> previousEventStates = externalEventHelper
                // The adjust event of a repayment is only posted while the repayment event type is enabled too.
                .enableBusinessEvents(List.of(ADJUST_TRANSACTION_EVENT, REPAYMENT_TRANSACTION_EVENT));
        try {
            runAt(BUSINESS_DATE, () -> {
                final Long loanId = createLoan();
                final String externalId = uniqueExternalId();
                final Long originalId = repay(loanId, externalId);

                final ExecuteWorkingCapitalLoanTransactionCommandResponse response = adjustByExternalId(loanId, externalId, ADJUST_BY_DELTA,
                        deltaRequest("-5", externalId));

                final List<ExternalEventResponse> events = externalEventHelper.getExternalEvents(ADJUST_TRANSACTION_EVENT, loanId);
                final Map<String, Object> adjustment = events.stream().map(ExternalEventResponse::getPayLoad)
                        .filter(payLoad -> payLoad.get("transactionToAdjust") instanceof Map<?, ?> reversed
                                && originalId.equals(toLong(reversed.get("id"))))
                        .findFirst().orElseThrow(() -> new AssertionError("no adjust event for transaction " + originalId));
                final Map<String, Object> reversed = (Map<String, Object>) adjustment.get("transactionToAdjust");
                final Map<String, Object> replacement = (Map<String, Object>) adjustment.get("newTransactionDetail");
                assertEquals(Boolean.TRUE, reversed.get("reversed"));
                assertNull(reversed.get("externalId"), "lifted from the reversed transaction");
                assertEquals(response.getResourceId(), toLong(replacement.get("id")));
                assertEquals(externalId, replacement.get("externalId"), "set on the replacement");
            });
        } finally {
            externalEventHelper.restoreBusinessEvents(previousEventStates);
        }
    }

    @Test
    public void absoluteAdjustWithOwnExternalIdMovesItToTheReplacement() {
        runAt(BUSINESS_DATE, () -> {
            final Long loanId = createLoan();
            final String externalId = uniqueExternalId();
            final Long originalId = repay(loanId, externalId);

            final ExecuteWorkingCapitalLoanTransactionCommandResponse response = adjustByExternalId(loanId, externalId, ADJUST,
                    adjustRequest("80", externalId));

            assertEquals(originalId, response.getSubResourceId());
            assertNull(wcLoanHelper.getTransaction(loanId, originalId).getExternalId());
            final GetWorkingCapitalLoanTransactionIdResponse live = byExternalId(loanId, externalId);
            assertEquals(response.getResourceId(), live.getId());
            assertEquals(0, new BigDecimal("80").compareTo(live.getTransactionAmount()));
        });
    }

    @Test
    public void withoutOwnExternalIdTheReversedTransactionKeepsIt() {
        runAt(BUSINESS_DATE, () -> {
            final Long loanId = createLoan();
            final String externalId = uniqueExternalId();
            final Long originalId = repay(loanId, externalId);
            final String otherExternalId = uniqueExternalId();

            final ExecuteWorkingCapitalLoanTransactionCommandResponse response = adjustByExternalId(loanId, externalId, ADJUST_BY_DELTA,
                    deltaRequest("-5", otherExternalId));

            assertEquals(otherExternalId, response.getResourceExternalId());
            final GetWorkingCapitalLoanTransactionIdResponse reversed = byExternalId(loanId, externalId);
            assertEquals(originalId, reversed.getId());
            assertTrue(reversed.getReversed());
        });
    }

    @Test
    public void externalIdOfAnotherTransactionIsRefused() {
        runAt(BUSINESS_DATE, () -> {
            final Long loanId = createLoan();
            final String externalId = uniqueExternalId();
            repay(loanId, externalId);
            final String otherExternalId = uniqueExternalId();
            repay(loanId, otherExternalId);

            final CallFailedRuntimeException failure = fail(
                    () -> client.workingCapitalLoanTransactions().executeWorkingCapitalLoanTransactionCommandByLoanIdTransactionExternalId(
                            loanId, externalId, ADJUST_BY_DELTA, deltaRequest("-5", otherExternalId)));

            assertEquals(400, failure.getStatus());
            assertTrue(failure.getResponseBody().contains("validation.msg.WORKINGCAPITALLOAN.externalId.already.exists"),
                    failure.getResponseBody());
        });
    }

    @Test
    public void consecutiveAdjustmentsFollowTheExternalId() {
        runAt(BUSINESS_DATE, () -> {
            final Long loanId = createLoan();
            final String externalId = uniqueExternalId();
            final Long originalId = repay(loanId, externalId);

            final ExecuteWorkingCapitalLoanTransactionCommandResponse first = adjustByExternalId(loanId, externalId, ADJUST_BY_DELTA,
                    deltaRequest("-5", externalId));
            final ExecuteWorkingCapitalLoanTransactionCommandResponse second = adjustByExternalId(loanId, externalId, ADJUST_BY_DELTA,
                    deltaRequest("-5", externalId));
            final ExecuteWorkingCapitalLoanTransactionCommandResponse third = adjustByExternalId(loanId, externalId, ADJUST_BY_DELTA,
                    deltaRequest("2.5", externalId));

            assertEquals(originalId, first.getSubResourceId());
            assertEquals(first.getResourceId(), second.getSubResourceId());
            assertEquals(second.getResourceId(), third.getSubResourceId());
            final GetWorkingCapitalLoanTransactionIdResponse live = byExternalId(loanId, externalId);
            assertEquals(third.getResourceId(), live.getId());
            assertEquals(0, new BigDecimal("92.5").compareTo(live.getTransactionAmount()));
            assertEquals(1, liveRepayments(loanId).size(), "only the last replacement is live");
        });
    }

    @Test
    public void concurrentAdjustmentsAgainstTheSameExternalIdAreEachAppliedOnce() {
        final List<Long> loans = new ArrayList<>();
        final List<String> externalIds = new ArrayList<>();
        runAt(BUSINESS_DATE, () -> {
            for (int i = 0; i < 2; i++) {
                final Long loanId = createLoan();
                final String externalId = uniqueExternalId();
                repay(loanId, externalId);
                loans.add(loanId);
                externalIds.add(externalId);
            }
        });
        final Long sequentialLoanId = loans.get(0);
        final Long concurrentLoanId = loans.get(1);
        final List<String> deltas = List.of("-5", "-7.5");

        runAt(BUSINESS_DATE, () -> deltas.forEach(delta -> adjustByExternalId(sequentialLoanId, externalIds.get(0), ADJUST_BY_DELTA,
                deltaRequest(delta, externalIds.get(0)))));

        final List<Supplier<ExecuteWorkingCapitalLoanTransactionCommandResponse>> calls = deltas.stream()
                .<Supplier<ExecuteWorkingCapitalLoanTransactionCommandResponse>>map(delta -> () -> adjustByExternalId(concurrentLoanId,
                        externalIds.get(1), ADJUST_BY_DELTA, deltaRequest(delta, externalIds.get(1))))
                .toList();
        runAt(BUSINESS_DATE, () -> {
            final List<ExecuteWorkingCapitalLoanTransactionCommandResponse> responses = runConcurrently(calls);
            assertEquals(2, responses.size(), "both adjustments succeed");
            assertNotEquals(responses.get(0).getSubResourceId(), responses.get(1).getSubResourceId(),
                    "each adjustment reversed a different transaction");
        });

        runAt(BUSINESS_DATE, () -> {
            final GetWorkingCapitalLoanTransactionIdResponse live = byExternalId(concurrentLoanId, externalIds.get(1));
            assertFalse(live.getReversed());
            assertEquals(0, new BigDecimal("87.5").compareTo(live.getTransactionAmount()), "original plus all deltas");
            assertEquals(1, liveRepayments(concurrentLoanId).size());
            assertSameBalance(getWcLoanDetails(sequentialLoanId).getBalance(), getWcLoanDetails(concurrentLoanId).getBalance());
        });
    }

    @Test
    public void concurrentAdjustmentsAgainstTheSameNumericIdLetOnlyOneWin() {
        final List<Long> ids = new ArrayList<>();
        runAt(BUSINESS_DATE, () -> {
            final Long loanId = createLoan();
            ids.add(loanId);
            ids.add(repay(loanId, uniqueExternalId()));
        });
        final Long loanId = ids.get(0);
        final Long transactionId = ids.get(1);

        final List<Supplier<Object>> calls = List.of(() -> adjustById(loanId, transactionId, "-5"),
                () -> adjustById(loanId, transactionId, "-7.5"));
        runAt(BUSINESS_DATE, () -> {
            final List<Object> outcomes = runConcurrentlyCollectingFailures(calls);
            final List<CallFailedRuntimeException> failures = outcomes.stream().filter(CallFailedRuntimeException.class::isInstance)
                    .map(CallFailedRuntimeException.class::cast).toList();
            assertEquals(1, failures.size(), "the numeric id is not followed to the replacement");
            assertAlreadyUndone(failures.get(0));
        });
    }

    @Test
    public void afterAFullReversalTheExternalIdStaysOnTheReversedTransaction() {
        runAt(BUSINESS_DATE, () -> {
            final Long loanId = createLoan();
            final String externalId = uniqueExternalId();
            final Long originalId = repay(loanId, externalId);

            final ExecuteWorkingCapitalLoanTransactionCommandResponse response = adjustByExternalId(loanId, externalId, ADJUST_BY_DELTA,
                    deltaRequest("-100", externalId));

            assertEquals(originalId, response.getResourceId());
            final GetWorkingCapitalLoanTransactionIdResponse reversed = byExternalId(loanId, externalId);
            assertEquals(originalId, reversed.getId());
            assertTrue(reversed.getReversed());

            // Still reversed after every retry attempt.
            final CallFailedRuntimeException failure = fail(
                    () -> client.workingCapitalLoanTransactions().executeWorkingCapitalLoanTransactionCommandByLoanIdTransactionExternalId(
                            loanId, externalId, ADJUST_BY_DELTA, deltaRequest("-5", externalId)));
            assertAlreadyUndone(failure);
        });
    }

    @Test
    public void numericIdOfAReversedTransactionIsRefusedWith403() {
        runAt(BUSINESS_DATE, () -> {
            final Long loanId = createLoan();
            final Long originalId = repay(loanId, uniqueExternalId());
            undoWcTransaction(loanId, originalId);

            final CallFailedRuntimeException failure = fail(
                    () -> client.workingCapitalLoanTransactions().executeWorkingCapitalLoanTransactionCommandByLoanIdTransactionId(loanId,
                            originalId, ADJUST_BY_DELTA, deltaRequest("-5", null)));
            assertAlreadyUndone(failure);
        });
    }

    private Long createLoan() {
        final Long clientId = createClient(DISBURSE_DATE);
        final Long productId = productHelper.createWorkingCapitalLoanProduct(
                new WorkingCapitalLoanProductTestBuilder().withName("WCL ExtId " + Utils.uniqueRandomStringGenerator("", 8))
                        .withShortName(Utils.uniqueRandomStringGenerator("", 4)).build())
                .getResourceId();
        return createApproveAndDisburseWcLoan(clientId, productId, PRINCIPAL, DISBURSE_DATE);
    }

    private Long repay(final Long loanId, final String externalId) {
        return wcLoanHelper.makeRepayment(loanId,
                WorkingCapitalLoanRequestBuilders.repayment(REPAYMENT_AMOUNT, REPAYMENT_DATE).externalId(externalId));
    }

    private ExecuteWorkingCapitalLoanTransactionCommandResponse adjustByExternalId(final Long loanId, final String transactionExternalId,
            final String command, final ExecuteWorkingCapitalLoanTransactionCommandRequest request) {
        return ok(() -> client.workingCapitalLoanTransactions()
                .executeWorkingCapitalLoanTransactionCommandByLoanIdTransactionExternalId(loanId, transactionExternalId, command, request));
    }

    private ExecuteWorkingCapitalLoanTransactionCommandResponse adjustById(final Long loanId, final Long transactionId,
            final String delta) {
        return ok(() -> client.workingCapitalLoanTransactions().executeWorkingCapitalLoanTransactionCommandByLoanIdTransactionId(loanId,
                transactionId, ADJUST_BY_DELTA, deltaRequest(delta, null)));
    }

    private GetWorkingCapitalLoanTransactionIdResponse byExternalId(final Long loanId, final String externalId) {
        return ok(() -> client.workingCapitalLoanTransactions().retrieveWorkingCapitalLoanTransactionByExternalTransactionId(loanId,
                externalId));
    }

    private List<GetWorkingCapitalLoanTransactionIdResponse> liveRepayments(final Long loanId) {
        return wcLoanHelper.getTransactions(loanId).stream().filter(txn -> !Boolean.TRUE.equals(txn.getReversed()))
                .filter(txn -> txn.getType() != null && "loanTransactionType.repayment".equals(txn.getType().getCode())).toList();
    }

    private static ExecuteWorkingCapitalLoanTransactionCommandRequest deltaRequest(final String delta, final String externalId) {
        return new ExecuteWorkingCapitalLoanTransactionCommandRequest().transactionAmount(new BigDecimal(delta)).externalId(externalId)
                .locale("en").dateFormat("dd MMMM yyyy");
    }

    private static ExecuteWorkingCapitalLoanTransactionCommandRequest adjustRequest(final String amount, final String externalId) {
        return deltaRequest(amount, externalId).transactionDate(REPAYMENT_DATE);
    }

    /**
     * A domain rule (403) reports the generic validation.msg.domain.rule.violation at the top and its own code in the
     * errors.
     */
    private static void assertAlreadyUndone(final CallFailedRuntimeException failure) {
        assertEquals(403, failure.getStatus());
        assertTrue(failure.getResponseBody().contains(ALREADY_UNDONE_CODE), failure.getResponseBody());
    }

    private static Long toLong(final Object value) {
        return value == null ? null : ((Number) value).longValue();
    }

    private static String uniqueExternalId() {
        return Utils.uniqueRandomStringGenerator("WC", 12);
    }

    private static void assertSameBalance(final GetBalance expected, final GetBalance actual) {
        assertNotNull(actual);
        assertEquals(0, expected.getPrincipalOutstanding().compareTo(actual.getPrincipalOutstanding()), "principal outstanding");
        assertEquals(0, expected.getPrincipalPaid().compareTo(actual.getPrincipalPaid()), "principal paid");
        assertEquals(0, expected.getTotalOutstanding().compareTo(actual.getTotalOutstanding()), "total outstanding");
    }

    private static <T> List<T> runConcurrently(final List<Supplier<T>> calls) {
        final List<T> results = new ArrayList<>();
        for (final Object outcome : runConcurrentlyCollectingFailures(calls)) {
            if (outcome instanceof RuntimeException failure) {
                throw failure;
            }
            @SuppressWarnings("unchecked")
            final T result = (T) outcome;
            results.add(result);
        }
        return results;
    }

    /**
     * Fires the calls at the same moment and returns each one's result, or the {@link RuntimeException} it failed with.
     */
    private static List<Object> runConcurrentlyCollectingFailures(final List<? extends Supplier<?>> calls) {
        final ExecutorService executor = Executors.newFixedThreadPool(calls.size());
        try {
            final CountDownLatch start = new CountDownLatch(1);
            final List<Future<Object>> futures = calls.stream().map(call -> executor.submit(() -> {
                start.await();
                try {
                    return (Object) call.get();
                } catch (final RuntimeException e) {
                    return e;
                }
            })).toList();
            start.countDown();
            final List<Object> outcomes = new ArrayList<>();
            for (final Future<Object> future : futures) {
                outcomes.add(future.get(2, TimeUnit.MINUTES));
            }
            return outcomes;
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        } finally {
            executor.shutdownNow();
        }
    }
}
