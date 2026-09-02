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

import static org.apache.http.HttpStatus.SC_CONFLICT;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.fineract.client.feign.ObjectMapperFactory;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.BatchRequest;
import org.apache.fineract.client.models.BatchResponse;
import org.apache.fineract.client.models.Header;
import org.apache.fineract.client.models.PostColumnHeaderData;
import org.apache.fineract.client.models.PostDataTablesRequest;
import org.apache.fineract.client.models.PostSavingsAccountTransactionsRequest;
import org.apache.fineract.client.models.PostSavingsAccountTransactionsResponse;
import org.apache.fineract.client.models.PostSavingsProductsRequest;
import org.apache.fineract.client.models.SavingsAccountTransactionData;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.apache.fineract.integrationtests.client.feign.FeignSavingsTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.batch.BatchServiceHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SavingsAccountTransactionTest extends FeignSavingsTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(SavingsAccountTransactionTest.class);

    private static final int CONCURRENT_SAVINGS_ITERATIONS = 10;
    private static final int CONCURRENT_BATCH_ITERATIONS = 5;
    private static final int DEADLOCK_BATCH_ITERATIONS = 5;
    private static final int EXECUTOR_TIMEOUT_SECONDS = 30;

    private static final String START_DATE = "03 June 2023";
    private static final String DEPOSIT_DATE = "05 June 2023";
    private static final String WITHDRAW_DATE = "10 June 2023";
    private static final String DEPOSIT_AMOUNT = "100";
    private static final String WITHDRAWAL_AMOUNT = "50";

    private static final String CONTENDED_SUBMITTED_DATE = "08 January 2013";
    private static final String CONTENDED_APPROVED_DATE = "09 January 2013";
    private static final String CONTENDED_ACTIVATED_DATE = "01 March 2013";
    private static final String CONTENDED_TRANSACTION_DATE = CONTENDED_ACTIVATED_DATE;
    private static final String CONTENDED_TRANSACTION_AMOUNT = "10";

    private static final String SAVINGS_TRANSACTION_APP_TABLE_NAME = EntityTables.SAVINGS_TRANSACTION.getName();
    private static final String DATATABLE_NAME_PREFIX = "dt_savings_transaction_";
    private static final String STRING_COLUMN_TYPE = "String";
    private static final String FIRST_COLUMN = "string1";
    private static final String SECOND_COLUMN = "string2";
    private static final long FIRST_COLUMN_LENGTH = 10L;
    private static final long SECOND_COLUMN_LENGTH = 12L;
    private static final String COLUMN_VALUE_PREFIX = "VAL_";
    private static final int COLUMN_VALUE_LENGTH = 3;
    private static final int DATATABLE_NAME_SUFFIX_LENGTH = 5;

    private static final long DEPOSIT_REQUEST_ID = 1L;
    private static final long WITHDRAW_REQUEST_ID = 2L;
    private static final long ADD_ENTRY_REQUEST_ID = 3L;
    private static final long DELETE_ENTRY_REQUEST_ID = 4L;
    private static final long SECOND_DEPOSIT_REQUEST_ID = 3L;
    private static final long SECOND_WITHDRAW_REQUEST_ID = 4L;
    private static final int FULL_BATCH_SIZE = 4;
    private static final int ROLLED_BACK_BATCH_SIZE = 1;

    private static final String DEPOSIT_COMMAND = "deposit";
    private static final String WITHDRAWAL_COMMAND = "withdrawal";
    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    private static final String DEPOSIT_RESOURCE_ID_REFERENCE = "$.resourceId";
    private static final String CREATED_TRANSACTION_ID_REFERENCE = "$.transactionId";
    private static final String CHILD_ROW_FAILURE = "Cannot add or update a child row";

    private final BatchServiceHelper batchHelper = new BatchServiceHelper();

    @Test
    public void verifySavingsTransactionSubmittedOnDateAndTransactionDate() {
        final LocalDate today = Utils.getLocalDateOfTenant();

        businessDateHelper.runAt(today.toString(), () -> {
            final Long clientId = createClient(START_DATE);
            assertNotNull(clientId);

            final Long savingsId = createApproveActivateSavings(clientId, dailyPostingProduct(), START_DATE);
            assertNotNull(savingsId);

            verifyTransactionDates(savingsId, deposit(savingsId, DEPOSIT_AMOUNT, DEPOSIT_DATE).getResourceId(),
                    Utils.getDateAsLocalDate(DEPOSIT_DATE), today, "Deposit");
            verifyTransactionDates(savingsId, withdraw(savingsId, WITHDRAWAL_AMOUNT, WITHDRAW_DATE).getResourceId(),
                    Utils.getDateAsLocalDate(WITHDRAW_DATE), today, "Withdrawal");
        });
    }

    @Test
    public void testConcurrentSavingsTransactions() {
        final Long savingsId = createContendedSavings(createClient());

        final List<Runnable> attempts = new ArrayList<>();
        for (int i = 0; i < CONCURRENT_SAVINGS_ITERATIONS; i++) {
            final String note = noteFor("note", i);
            attempts.add(() -> depositThenWithdraw(savingsId, note));
        }

        runConcurrently("Concurrent savings transactions", attempts);
    }

    @Test
    public void testConcurrentSavingsBatchTransactions() {
        final Long savingsId = createContendedSavings(createClient());
        final String datatableName = createSavingsTransactionDatatable();

        try {
            final List<Runnable> attempts = new ArrayList<>();
            for (int i = 0; i < CONCURRENT_BATCH_ITERATIONS; i++) {
                final String note = noteFor("note", i);
                attempts.add(() -> runSavingsAndDatatableBatch(savingsId, note, datatableName, true));
                attempts.add(() -> runSavingsAndDatatableBatch(savingsId, note, datatableName, false));
            }

            runConcurrently("Concurrent savings batch transactions", attempts);
        } finally {
            datatableHelper.deleteDatatable(datatableName);
        }
    }

    @Test
    public void testDeadlockSavingsBatchTransactions() {
        final Long clientId = createClient();
        final Long firstSavingsId = createContendedSavings(clientId);
        final Long secondSavingsId = createContendedSavings(clientId);

        // Half the threads take the accounts in the opposite order, which is what makes them deadlock.
        final List<Runnable> attempts = new ArrayList<>();
        for (int i = 0; i < DEADLOCK_BATCH_ITERATIONS; i++) {
            final String firstNote = noteFor("note1", i);
            final String secondNote = noteFor("note2", i);
            attempts.add(() -> runTwoAccountBatch(firstSavingsId, secondSavingsId, firstNote));
            attempts.add(() -> runTwoAccountBatch(secondSavingsId, firstSavingsId, secondNote));
        }

        runConcurrently("Concurrent deadlock savings batch transactions", attempts);
    }

    private void verifyTransactionDates(final Long savingsId, final Long transactionId, final LocalDate expectedTransactionDate,
            final LocalDate expectedSubmittedOnDate, final String transactionType) {
        assertNotNull(transactionId);

        final SavingsAccountTransactionData transaction = savingsTransactionHelper.getTransaction(savingsId, transactionId);
        assertNotNull(transaction);

        assertEquals(transactionId, transaction.getId(), "Check Savings " + transactionType + " Transaction");
        assertTrue(DateUtils.isEqual(expectedTransactionDate, transaction.getDate()), "Transaction Date check for Savings "
                + transactionType + " Transaction. Expected: " + expectedTransactionDate + ", current: " + transaction.getDate());
        assertTrue(DateUtils.isEqual(expectedSubmittedOnDate, transaction.getSubmittedOnDate()),
                "Submitted On Date check for Savings " + transactionType + " Transaction. Expected: " + expectedSubmittedOnDate
                        + ", current: " + transaction.getSubmittedOnDate());
    }

    private void depositThenWithdraw(final Long savingsId, final String note) {
        if (accepted(() -> savingsTransactionHelper.deposit(savingsId, contendedTransaction(note)))) {
            accepted(() -> savingsTransactionHelper.withdraw(savingsId, contendedTransaction(note)));
        }
    }

    private static boolean accepted(final Supplier<PostSavingsAccountTransactionsResponse> transaction) {
        try {
            assertNotNull(transaction.get().getResourceId(), "An accepted transaction should carry a resource id");
            return true;
        } catch (CallFailedRuntimeException e) {
            assertEquals(SC_CONFLICT, e.getStatus(), "A losing thread should be told it conflicted, but got: " + e.getMessage());
            return false;
        }
    }

    private void runSavingsAndDatatableBatch(final Long savingsId, final String note, final String datatableName,
            final boolean enclosingTransaction) {
        final List<BatchRequest> requests = List.of(savingsTransactionRequest(DEPOSIT_REQUEST_ID, savingsId, note, DEPOSIT_COMMAND),
                addDatatableEntryRequest(datatableName), deleteDatatableEntryRequest(datatableName),
                savingsTransactionRequest(WITHDRAW_REQUEST_ID, savingsId, note, WITHDRAWAL_COMMAND));

        final List<BatchResponse> responses = batchHelper.handleBatch(requests, enclosingTransaction);
        assertNotNull(responses, "Responses");

        if (enclosingTransaction) {
            assertEnclosingTransactionBatchResponses(responses);
        } else {
            assertNonEnclosingTransactionBatchResponses(responses);
        }
    }

    private void runTwoAccountBatch(final Long firstSavingsId, final Long secondSavingsId, final String note) {
        final List<BatchRequest> requests = List.of(savingsTransactionRequest(DEPOSIT_REQUEST_ID, firstSavingsId, note, DEPOSIT_COMMAND),
                savingsTransactionRequest(WITHDRAW_REQUEST_ID, firstSavingsId, note, WITHDRAWAL_COMMAND),
                savingsTransactionRequest(SECOND_DEPOSIT_REQUEST_ID, secondSavingsId, note, DEPOSIT_COMMAND),
                savingsTransactionRequest(SECOND_WITHDRAW_REQUEST_ID, secondSavingsId, note, WITHDRAWAL_COMMAND));

        final List<BatchResponse> responses = batchHelper.handleBatch(requests, true);
        assertNotNull(responses, "Responses");

        final BatchResponse first = responses.get(0);
        final Integer statusCode = first.getStatusCode();
        final String body = first.getBody() == null ? "" : first.getBody();
        assertNotNull(statusCode, "First response status code");
        assertTrue(SC_OK == statusCode || SC_CONFLICT == statusCode || (SC_FORBIDDEN == statusCode && body.contains(CHILD_ROW_FAILURE)),
                "Status code: " + statusCode + ", message: " + body);

        assertRolledBackOrComplete(responses, statusCode);
    }

    private static void assertEnclosingTransactionBatchResponses(final List<BatchResponse> responses) {
        final Integer statusCode = responses.get(0).getStatusCode();
        assertNotNull(statusCode, "First enclosingTransaction response status code");
        assertTrue(SC_OK == statusCode || SC_CONFLICT == statusCode, "Status code: " + statusCode);

        assertRolledBackOrComplete(responses, statusCode);
    }

    private static void assertNonEnclosingTransactionBatchResponses(final List<BatchResponse> responses) {
        assertEquals(FULL_BATCH_SIZE, responses.size(), "Response size for non-enclosingTransaction response");

        final Integer firstStatusCode = responses.get(0).getStatusCode();
        assertNotNull(firstStatusCode, "First non-enclosingTransaction response status code");
        assertTrue(SC_OK == firstStatusCode || SC_CONFLICT == firstStatusCode,
                "First non-enclosingTransaction response status code: " + firstStatusCode);

        final Integer lastStatusCode = responses.get(FULL_BATCH_SIZE - 1).getStatusCode();
        assertNotNull(lastStatusCode, "Last non-enclosingTransaction response status code");
        // Without an enclosing transaction the later steps still run after a failed deposit, but can only fail too.
        assertTrue(
                SC_OK == firstStatusCode ? (SC_OK == lastStatusCode || SC_CONFLICT == lastStatusCode)
                        : (SC_FORBIDDEN == lastStatusCode || SC_CONFLICT == lastStatusCode),
                "Last non-enclosingTransaction response status code: " + lastStatusCode);
    }

    private static void assertRolledBackOrComplete(final List<BatchResponse> responses, final Integer firstStatusCode) {
        if (SC_OK == firstStatusCode) {
            assertEquals(FULL_BATCH_SIZE, responses.size(), "Response size for OK response");
            final Integer lastStatusCode = responses.get(FULL_BATCH_SIZE - 1).getStatusCode();
            assertNotNull(lastStatusCode, "Last OK response status code");
            assertEquals(SC_OK, lastStatusCode, "Last OK response status code");
        } else {
            assertEquals(ROLLED_BACK_BATCH_SIZE, responses.size(), "Response size for failed response");
        }
    }

    private static BatchRequest savingsTransactionRequest(final long requestId, final Long savingsId, final String note,
            final String command) {
        return new BatchRequest().requestId(requestId).relativeUrl("v1/savingsaccounts/" + savingsId + "/transactions?command=" + command)
                .method("POST").body(asJson(contendedTransaction(note)))
                // Without an idempotency key the server refuses a transaction it has already seen.
                .addHeadersItem(new Header().name(IDEMPOTENCY_KEY_HEADER).value(UUID.randomUUID().toString()));
    }

    private static BatchRequest addDatatableEntryRequest(final String datatableName) {
        final Map<String, Object> entry = List.of(FIRST_COLUMN, SECOND_COLUMN).stream().collect(
                Collectors.toMap(column -> column, column -> Utils.randomStringGenerator(COLUMN_VALUE_PREFIX, COLUMN_VALUE_LENGTH)));
        return new BatchRequest().requestId(ADD_ENTRY_REQUEST_ID).reference(DEPOSIT_REQUEST_ID)
                .relativeUrl("v1/datatables/" + datatableName + "/" + DEPOSIT_RESOURCE_ID_REFERENCE).method("POST").body(asJson(entry));
    }

    private static BatchRequest deleteDatatableEntryRequest(final String datatableName) {
        return new BatchRequest().requestId(DELETE_ENTRY_REQUEST_ID).reference(ADD_ENTRY_REQUEST_ID)
                .relativeUrl("v1/datatables/" + datatableName + "/" + CREATED_TRANSACTION_ID_REFERENCE).method("DELETE");
    }

    private String createSavingsTransactionDatatable() {
        final String datatableName = Utils.uniqueRandomStringGenerator(DATATABLE_NAME_PREFIX, DATATABLE_NAME_SUFFIX_LENGTH).toLowerCase();
        datatableHelper
                .createDatatable(new PostDataTablesRequest().datatableName(datatableName).apptableName(SAVINGS_TRANSACTION_APP_TABLE_NAME)
                        .multiRow(false).addColumnsItem(stringColumn(FIRST_COLUMN, false, FIRST_COLUMN_LENGTH))
                        .addColumnsItem(stringColumn(SECOND_COLUMN, true, SECOND_COLUMN_LENGTH)));
        return datatableName;
    }

    private static PostColumnHeaderData stringColumn(final String name, final boolean mandatory, final long length) {
        return new PostColumnHeaderData().name(name).type(STRING_COLUMN_TYPE).mandatory(mandatory).length(length);
    }

    private static PostSavingsAccountTransactionsRequest contendedTransaction(final String note) {
        return SavingsRequestBuilders.deposit(CONTENDED_TRANSACTION_AMOUNT, CONTENDED_TRANSACTION_DATE).note(note);
    }

    private Long createContendedSavings(final Long clientId) {
        final Long savingsId = submitSavingsApplication(clientId, dailyPostingProduct(), CONTENDED_SUBMITTED_DATE).getSavingsId();
        assertNotNull(savingsId);

        approveSavings(savingsId, CONTENDED_APPROVED_DATE);
        activateSavings(savingsId, CONTENDED_ACTIVATED_DATE);
        return savingsId;
    }

    private Long dailyPostingProduct() {
        final PostSavingsProductsRequest product = SavingsRequestBuilders.savingsProduct(
                SavingsTestData.InterestCompoundingPeriodType.DAILY, SavingsTestData.InterestPostingPeriodType.DAILY,
                SavingsTestData.InterestCalculationType.DAILY_BALANCE);
        final Long productId = createSavingsProduct(product).getResourceId();
        assertNotNull(productId);
        return productId;
    }

    private static String noteFor(final String prefix, final int iteration) {
        return prefix + "_" + iteration;
    }

    private static String asJson(final Object body) {
        try {
            return ObjectMapperFactory.getShared().writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialise the batch request body " + body, e);
        }
    }

    private static void runConcurrently(final String description, final List<Runnable> attempts) {
        final ExecutorService executor = Executors.newFixedThreadPool(attempts.size());
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch doneLatch = new CountDownLatch(attempts.size());
        final List<String> failures = Collections.synchronizedList(new ArrayList<>());

        for (final Runnable attempt : attempts) {
            executor.execute(() -> {
                try {
                    startLatch.await();
                    attempt.run();
                } catch (AssertionError | Exception e) {
                    final String failure = description + " failed: " + e.getMessage();
                    failures.add(failure);
                    LOG.error(failure, e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        awaitCompletion(executor, startLatch, doneLatch, description, failures);
    }

    private static void awaitCompletion(final ExecutorService executor, final CountDownLatch startLatch, final CountDownLatch doneLatch,
            final String description, final List<String> failures) {
        startLatch.countDown();
        try {
            assertTrue(doneLatch.await(EXECUTOR_TIMEOUT_SECONDS, TimeUnit.SECONDS), description + " did not complete within timeout");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for " + description.toLowerCase(), e);
        } finally {
            executor.shutdown();
        }

        try {
            assertTrue(executor.awaitTermination(EXECUTOR_TIMEOUT_SECONDS, TimeUnit.SECONDS), "ExecutorService should terminate");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for executor shutdown", e);
        }
        assertTrue(failures.isEmpty(), String.join(System.lineSeparator(), failures));
    }

    @AfterEach
    public void tearDown() {
        globalConfigurationHelper.resetAllDefaultGlobalConfigurations();
        globalConfigurationHelper.verifyAllDefaultGlobalConfigurations();
    }
}
