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

import static org.apache.fineract.integrationtests.common.CommonConstants.RESPONSE_RESOURCE_ID;
import static org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper.PAYMENT_TYPE_ID;
import static org.apache.fineract.integrationtests.common.system.DatatableHelper.addDatatableColumn;
import static org.apache.hc.core5.http.HttpStatus.SC_CONFLICT;
import static org.apache.hc.core5.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.hc.core5.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.apache.fineract.batch.domain.Header;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.integrationtests.common.BatchHelper;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsProductHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsStatusChecker;
import org.apache.fineract.integrationtests.common.savings.SavingsTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.savings.SavingsTransactionData;
import org.apache.fineract.integrationtests.common.system.DatatableHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({ "rawtypes" })
@ExtendWith({ SavingsTestLifecycleExtension.class })
public class SavingsAccountTransactionTest {

    private static final Logger log = LoggerFactory.getLogger(SavingsAccountTransactionTest.class);

    public static final String ACCOUNT_TYPE_INDIVIDUAL = "INDIVIDUAL";
    private static final int CONCURRENT_SAVINGS_ITERATIONS = 10;
    private static final int CONCURRENT_SAVINGS_THREAD_COUNT = CONCURRENT_SAVINGS_ITERATIONS;
    private static final int CONCURRENT_BATCH_ITERATIONS = 5;
    private static final int CONCURRENT_BATCH_THREAD_COUNT = CONCURRENT_BATCH_ITERATIONS * 2;
    private static final int DEADLOCK_BATCH_ITERATIONS = 5;
    private static final int DEADLOCK_BATCH_THREAD_COUNT = DEADLOCK_BATCH_ITERATIONS * 2;
    private static final int EXECUTOR_TIMEOUT_SECONDS = 30;
    final String startDateString = "03 June 2023";
    final String depositDateString = "05 June 2023";
    final String withdrawDateString = "10 June 2023";

    private String authenticationKey;
    private ResponseSpecification responseSpec;
    private ResponseSpecification concurrentResponseSpec;
    private ResponseSpecification deadlockResponseSpec;
    private RequestSpecification requestSpec;
    private SavingsProductHelper savingsProductHelper;
    private SavingsAccountHelper savingsAccountHelper;
    private DatatableHelper datatableHelper;
    private GlobalConfigurationHelper globalConfigurationHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.authenticationKey = Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey();
        this.requestSpec.header("Authorization", "Basic " + authenticationKey);
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(SC_OK).build();
        this.concurrentResponseSpec = new ResponseSpecBuilder().expectStatusCode(anyOf(is(SC_OK), is(SC_CONFLICT))).build();
        this.deadlockResponseSpec = new ResponseSpecBuilder().expectStatusCode(anyOf(is(SC_OK), is(SC_CONFLICT), is(SC_FORBIDDEN))).build();
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.savingsProductHelper = new SavingsProductHelper();
        this.datatableHelper = new DatatableHelper(this.requestSpec, this.responseSpec);
        globalConfigurationHelper = new GlobalConfigurationHelper();
    }

    @Test
    public void verifySavingsTransactionSubmittedOnDateAndTransactionDate() {
        LocalDate today = Utils.getLocalDateOfTenant();
        try {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            BusinessDateHelper.updateBusinessDate(BusinessDateType.BUSINESS_DATE, today);

            LocalDate depositDate = Utils.getDateAsLocalDate(depositDateString);
            LocalDate withdrawDate = Utils.getDateAsLocalDate(withdrawDateString);

            final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, startDateString);
            assertNotNull(clientID);

            final Integer savingsId = createApproveActivateSavingsAccountDailyPosting(clientID, startDateString);
            assertNotNull(savingsId);

            performSavingsTransaction(savingsId, "100", depositDate, true);
            performSavingsTransaction(savingsId, "50", withdrawDate, false);
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void testConcurrentSavingsTransactions() {
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        final Integer savingsProductId = createSavingsProductDailyPosting();
        assertNotNull(savingsProductId);

        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductId, ACCOUNT_TYPE_INDIVIDUAL);
        this.savingsAccountHelper.approveSavings(savingsId);
        HashMap savingsStatusHashMap = this.savingsAccountHelper.activateSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);

        runConcurrentSavingsTransactions(savingsId);
    }

    @Test
    public void testConcurrentSavingsBatchTransactions() {
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        final Integer savingsProductId = createSavingsProductDailyPosting();
        assertNotNull(savingsProductId);

        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductId, ACCOUNT_TYPE_INDIVIDUAL);
        this.savingsAccountHelper.approveSavings(savingsId);
        HashMap savingsStatusHashMap = this.savingsAccountHelper.activateSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);

        // creating datatable for client entity
        final HashMap<String, Object> columnMap = new HashMap<>();
        String datatableName = Utils.uniqueRandomStringGenerator("dt_savings_transaction_", 5).toLowerCase();
        columnMap.put("datatableName", datatableName);
        columnMap.put("apptableName", "m_savings_account_transaction");
        columnMap.put("multiRow", false);
        String string1 = "string1";
        String string2 = "string2";
        List<String> columnNames = List.of(string1, string2);

        final List<HashMap<String, Object>> datatableColumnsList = new ArrayList<>();
        addDatatableColumn(datatableColumnsList, string1, "String", false, 10, null);
        addDatatableColumn(datatableColumnsList, string2, "String", true, 12, null);
        columnMap.put("columns", datatableColumnsList);
        String datatableJson = new Gson().toJson(columnMap);
        this.datatableHelper.createDatatable(datatableJson, "");

        try {
            runConcurrentSavingsBatchTransactions(savingsId, datatableName, columnNames);
        } finally {
            this.datatableHelper.deleteDatatable(datatableName);
        }
    }

    @Test
    public void testDeadlockSavingsBatchTransactions() {
        final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec);
        ClientHelper.verifyClientCreatedOnServer(requestSpec, responseSpec, clientID);

        final Integer savingsProductId = createSavingsProductDailyPosting();
        assertNotNull(savingsProductId);

        final Integer savingsId1 = savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductId, ACCOUNT_TYPE_INDIVIDUAL);
        savingsAccountHelper.approveSavings(savingsId1);
        savingsAccountHelper.activateSavings(savingsId1);

        final Integer savingsId2 = savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductId, ACCOUNT_TYPE_INDIVIDUAL);
        savingsAccountHelper.approveSavings(savingsId2);
        savingsAccountHelper.activateSavings(savingsId2);

        runConcurrentDeadlockSavingsBatchTransactions(savingsId1, savingsId2);
    }

    private void performSavingsTransaction(Integer savingsId, String amount, LocalDate transactionDate, boolean isDeposit) {
        String transactionType = isDeposit ? "Deposit" : "Withdrawal";
        String formattedTransactionDate = Utils.dateFormatter.format(transactionDate);
        Integer transactionId = isDeposit
                ? (Integer) this.savingsAccountHelper.depositToSavingsAccount(savingsId, amount, formattedTransactionDate,
                        RESPONSE_RESOURCE_ID)
                : (Integer) this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, amount, formattedTransactionDate,
                        RESPONSE_RESOURCE_ID);

        assertNotNull(transactionId);

        HashMap transaction = savingsAccountHelper.getSavingsTransaction(savingsId, transactionId);
        assertNotNull(transaction);

        assertEquals(transactionId, (Integer) transaction.get("id"), "Check Savings " + transactionType + " Transaction");
        LocalDate transactionDateFromResponse = extractLocalDate(transaction, "date");
        assertTrue(DateUtils.isEqual(transactionDate, transactionDateFromResponse), "Transaction Date check for Savings " + transactionType
                + " Transaction. Expected: " + transactionDate + ", current: " + transactionDateFromResponse);
        LocalDate submittedOnDate = Utils.getLocalDateOfTenant();
        LocalDate submittedOnDateFromResponse = extractLocalDate(transaction, "submittedOnDate");
        assertTrue(DateUtils.isEqual(submittedOnDate, submittedOnDateFromResponse), "Submitted On Date check for Savings " + transactionType
                + " Transaction. Expected: " + submittedOnDate + ", current: " + submittedOnDateFromResponse);
    }

    private LocalDate extractLocalDate(HashMap transactionMap, String fieldName) {
        List<Integer> dateStringList = (List<Integer>) transactionMap.get(fieldName);
        LocalDate extractedDate = dateStringList.stream()
                .collect(Collectors.collectingAndThen(Collectors.toList(), list -> LocalDate.of(list.get(0), list.get(1), list.get(2))));
        return extractedDate;
    }

    private Integer createApproveActivateSavingsAccountDailyPosting(final Integer clientID, final String startDate) {
        final Integer savingsProductID = createSavingsProductDailyPosting();
        assertNotNull(savingsProductID);
        return savingsAccountHelper.createApproveActivateSavingsAccount(clientID, savingsProductID, startDate);
    }

    private Integer createSavingsProductDailyPosting() {
        final String savingsProductJSON = this.savingsProductHelper.withInterestCompoundingPeriodTypeAsDaily()
                .withInterestPostingPeriodTypeAsDaily().withInterestCalculationPeriodTypeAsDailyBalance().build();
        return SavingsProductHelper.createSavingsProduct(savingsProductJSON, requestSpec, responseSpec);
    }

    private void runConcurrentSavingsTransactions(Integer savingsId) {
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_SAVINGS_THREAD_COUNT);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(CONCURRENT_SAVINGS_THREAD_COUNT);
        List<String> failures = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < CONCURRENT_SAVINGS_ITERATIONS; i++) {
            SavingsTransactionData transactionData = SavingsTransactionData.builder().transactionDate(SavingsAccountHelper.TRANSACTION_DATE)
                    .transactionAmount("10").paymentTypeId(PAYMENT_TYPE_ID).note("note_" + i).build();
            executor.execute(() -> {
                try {
                    startLatch.await();
                    new TransactionExecutor(new SavingsAccountHelper(newConcurrentRequestSpecification(), concurrentResponseSpec),
                            savingsId, transactionData).run();
                } catch (AssertionError | Exception e) {
                    String failure = "concurrent savings transaction failed for note " + transactionData.getNote() + ": " + e.getMessage();
                    failures.add(failure);
                    log.error(failure, e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        awaitExecutorCompletion(executor, startLatch, doneLatch, "Concurrent savings transactions", failures);
    }

    private void runConcurrentSavingsBatchTransactions(Integer savingsId, String datatableName, List<String> columnNames) {
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_BATCH_THREAD_COUNT);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(CONCURRENT_BATCH_THREAD_COUNT);
        List<String> failures = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < CONCURRENT_BATCH_ITERATIONS; i++) {
            SavingsTransactionData transactionData = SavingsTransactionData.builder().transactionDate(SavingsAccountHelper.TRANSACTION_DATE)
                    .transactionAmount("10").paymentTypeId(PAYMENT_TYPE_ID).note("note_" + i).build();
            submitConcurrentSavingsBatch(executor, startLatch, doneLatch, failures, savingsId, transactionData, true, datatableName,
                    columnNames);
            submitConcurrentSavingsBatch(executor, startLatch, doneLatch, failures, savingsId, transactionData, false, datatableName,
                    columnNames);
        }

        awaitExecutorCompletion(executor, startLatch, doneLatch, "Concurrent savings batch transactions", failures);
    }

    private void submitConcurrentSavingsBatch(ExecutorService executor, CountDownLatch startLatch, CountDownLatch doneLatch,
            List<String> failures, Integer savingsId, SavingsTransactionData transactionData, boolean enclosingTransaction,
            String datatableName, List<String> columnNames) {
        executor.execute(() -> {
            try {
                startLatch.await();
                executeSavingsBatchTransaction(savingsId, transactionData, enclosingTransaction, datatableName, columnNames);
            } catch (AssertionError | Exception e) {
                String batchType = enclosingTransaction ? "enclosing-transaction" : "non-enclosing-transaction";
                String failure = batchType + " batch failed for note " + transactionData.getNote() + ": " + e.getMessage();
                failures.add(failure);
                log.error(failure, e);
            } finally {
                doneLatch.countDown();
            }
        });
    }

    private void executeSavingsBatchTransaction(Integer savingsId, SavingsTransactionData transactionData, boolean enclosingTransaction,
            String datatableName, List<String> columnNames) {
        final BatchRequest depositRequest = BatchHelper.depositSavingAccount(1L, savingsId.longValue(), transactionData);
        Set<Header> headers = Optional.ofNullable(depositRequest.getHeaders()).orElse(new HashSet<>(1));
        headers.add(new Header("Idempotency-Key", UUID.randomUUID().toString()));
        depositRequest.setHeaders(headers);

        BatchRequest addEntryRequest = BatchHelper.createDatatableEntryRequest("$.resourceId", datatableName, columnNames);
        addEntryRequest.setReference(1L);
        BatchRequest deleteEntryRequest = BatchHelper.deleteDatatableEntryRequest("$.transactionId", datatableName, null);

        final BatchRequest withdrawRequest = BatchHelper.withdrawSavingAccount(2L, savingsId.longValue(), transactionData);
        headers = Optional.ofNullable(withdrawRequest.getHeaders()).orElse(new HashSet<>(1));
        headers.add(new Header("Idempotency-Key", UUID.randomUUID().toString()));
        withdrawRequest.setHeaders(headers);

        String json = BatchHelper.toJsonString(Arrays.asList(depositRequest, addEntryRequest, deleteEntryRequest, withdrawRequest));
        SavingsAccountHelper savingsHelper = new SavingsAccountHelper(newConcurrentRequestSpecification(),
                enclosingTransaction ? concurrentResponseSpec : deadlockResponseSpec);
        final List<BatchResponse> responses = enclosingTransaction
                ? BatchHelper.postBatchRequestsWithEnclosingTransaction(savingsHelper.getRequestSpec(), savingsHelper.getResponseSpec(),
                        json)
                : BatchHelper.postBatchRequestsWithoutEnclosingTransaction(savingsHelper.getRequestSpec(), savingsHelper.getResponseSpec(),
                        json);

        assertNotNull(responses, "Responses");
        if (enclosingTransaction) {
            assertEnclosingTransactionBatchResponses(responses);
        } else {
            assertNonEnclosingTransactionBatchResponses(responses);
        }
    }

    private RequestSpecification newConcurrentRequestSpecification() {
        return new RequestSpecBuilder().setContentType(ContentType.JSON).addHeader("Authorization", "Basic " + authenticationKey).build();
    }

    private void runConcurrentDeadlockSavingsBatchTransactions(Integer savingsId1, Integer savingsId2) {
        ExecutorService executor = Executors.newFixedThreadPool(DEADLOCK_BATCH_THREAD_COUNT);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(DEADLOCK_BATCH_THREAD_COUNT);
        List<String> failures = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < DEADLOCK_BATCH_ITERATIONS; i++) {
            SavingsTransactionData transactionData1 = SavingsTransactionData.builder()
                    .transactionDate(SavingsAccountHelper.TRANSACTION_DATE).transactionAmount("10").paymentTypeId(PAYMENT_TYPE_ID)
                    .note("note1_" + i).build();
            submitDeadlockBatch(executor, startLatch, doneLatch, failures, savingsId1, savingsId2, transactionData1);

            SavingsTransactionData transactionData2 = SavingsTransactionData.builder()
                    .transactionDate(SavingsAccountHelper.TRANSACTION_DATE).transactionAmount("10").paymentTypeId(PAYMENT_TYPE_ID)
                    .note("note2_" + i).build();
            submitDeadlockBatch(executor, startLatch, doneLatch, failures, savingsId2, savingsId1, transactionData2);
        }

        awaitExecutorCompletion(executor, startLatch, doneLatch, "Concurrent deadlock savings batch transactions", failures);
    }

    private void submitDeadlockBatch(ExecutorService executor, CountDownLatch startLatch, CountDownLatch doneLatch, List<String> failures,
            Integer savingsId1, Integer savingsId2, SavingsTransactionData transactionData) {
        executor.execute(() -> {
            try {
                startLatch.await();
                runDeadlockBatch(new SavingsAccountHelper(newConcurrentRequestSpecification(), deadlockResponseSpec), savingsId1,
                        savingsId2, transactionData);
            } catch (AssertionError | Exception e) {
                String failure = "deadlock batch failed for note " + transactionData.getNote() + ": " + e.getMessage();
                failures.add(failure);
                log.error(failure, e);
            } finally {
                doneLatch.countDown();
            }
        });
    }

    private void awaitExecutorCompletion(ExecutorService executor, CountDownLatch startLatch, CountDownLatch doneLatch, String description,
            List<String> failures) {
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

    private void assertEnclosingTransactionBatchResponses(List<BatchResponse> responses) {
        Integer statusCode1 = responses.get(0).getStatusCode();
        assertNotNull(statusCode1, "First enclosingTransaction response status code");
        assertTrue(SC_OK == statusCode1 || SC_CONFLICT == statusCode1, "Status code: " + statusCode1);
        if (SC_OK == statusCode1) {
            assertEquals(4, responses.size(), "Response size for enclosingTransaction OK response");
            Integer statusCode4 = responses.get(3).getStatusCode();
            assertNotNull(statusCode4, "Last enclosingTransaction OK response status code");
            assertEquals(SC_OK, statusCode4, "Last enclosingTransaction OK response status code");
        } else {
            assertEquals(1, responses.size(), "Response size for enclosingTransaction failed response");
        }
    }

    private void assertNonEnclosingTransactionBatchResponses(List<BatchResponse> responses) {
        assertEquals(4, responses.size(), "Response size for non-enclosingTransaction response");
        Integer statusCode1 = responses.get(0).getStatusCode();
        assertNotNull(statusCode1, "First non-enclosingTransaction response status code");
        assertTrue(SC_OK == statusCode1 || SC_CONFLICT == statusCode1,
                "First non-enclosingTransaction response status code: " + statusCode1);

        Integer statusCode4 = responses.get(3).getStatusCode();
        assertNotNull(statusCode4, "Last non-enclosingTransaction response status code");
        assertTrue(
                SC_OK == statusCode1 ? (SC_OK == statusCode4 || SC_CONFLICT == statusCode4)
                        : (SC_FORBIDDEN == statusCode4 || SC_CONFLICT == statusCode4),
                "Last non-enclosingTransaction response status code: " + statusCode4);
    }

    public static class TransactionExecutor implements Runnable {

        private final SavingsAccountHelper savingsHelper;
        private final Integer savingsId;
        SavingsTransactionData transactionData;

        TransactionExecutor(SavingsAccountHelper savingsAccountHelper, Integer savingsId, SavingsTransactionData transactionData) {
            this.savingsId = savingsId;
            this.savingsHelper = savingsAccountHelper;
            this.transactionData = transactionData;
        }

        @Override
        public void run() {
            log.info("Details of passed concurrent transaction, details (date, amount, note, savingsId) are {},{},{},{}",
                    transactionData.getTransactionDate(), transactionData.getTransactionAmount(), transactionData.getNote(), savingsId);
            String json = transactionData.getJson();
            String response = (String) this.savingsHelper.depositToSavingsAccount(savingsId, json, null);
            boolean success = checkConcurrentResponse(response);
            if (success) {
                response = (String) this.savingsHelper.withdrawalFromSavingsAccount(savingsId, json, null);
                checkConcurrentResponse(response);
            }
        }

        private static boolean checkConcurrentResponse(String response) {
            assertNotNull(response, "Single response");
            JsonPath res = JsonPath.from(response);
            String statusCode = res.get("httpStatusCode");
            if (statusCode == null) {
                assertNotNull(res.get(RESPONSE_RESOURCE_ID), "Single response " + RESPONSE_RESOURCE_ID);
                return true;
            }
            assertEquals(String.valueOf(SC_CONFLICT), statusCode, "Single response status code");
            return false;
        }
    }

    private void runDeadlockBatch(SavingsAccountHelper savingsHelper, Integer savingsId1, Integer savingsId2,
            SavingsTransactionData transactionData) {
        final BatchRequest depositRequest1 = BatchHelper.depositSavingAccount(1L, savingsId1.longValue(), transactionData);
        final BatchRequest withdrawRequest1 = BatchHelper.withdrawSavingAccount(2L, savingsId1.longValue(), transactionData);
        final BatchRequest depositRequest2 = BatchHelper.depositSavingAccount(3L, savingsId2.longValue(), transactionData);
        final BatchRequest withdrawRequest2 = BatchHelper.withdrawSavingAccount(4L, savingsId2.longValue(), transactionData);
        String json = BatchHelper.toJsonString(Arrays.asList(depositRequest1, withdrawRequest1, depositRequest2, withdrawRequest2));
        RequestSpecification requestSpec = savingsHelper.getRequestSpec();
        ResponseSpecification responseSpec = savingsHelper.getResponseSpec();
        final List<BatchResponse> responses = BatchHelper.postBatchRequestsWithEnclosingTransaction(requestSpec, responseSpec, json);
        assertNotNull(responses, "Responses");
        BatchResponse response1 = responses.get(0);
        Integer statusCode = response1.getStatusCode();
        String msg = Strings.nullToEmpty(response1.getBody());
        assertNotNull(statusCode, "First response status code");
        assertTrue(
                SC_OK == statusCode || SC_CONFLICT == statusCode
                        || (SC_FORBIDDEN == statusCode && msg.contains("Cannot add or update a child row")),
                "Status code: " + statusCode + ", message: " + msg);
        if (SC_OK == statusCode) {
            assertEquals(4, responses.size(), "Response size for OK response");
            Integer statusCode4 = responses.get(3).getStatusCode();
            assertNotNull(statusCode4, "Last OK response status code");
            assertEquals(SC_OK, statusCode4, "Last OK response status code");
        } else {
            assertEquals(1, responses.size(), "Response size for failed response");
        }
    }

    // Reset configuration fields
    @AfterEach
    public void tearDown() {
        globalConfigurationHelper.resetAllDefaultGlobalConfigurations();
        globalConfigurationHelper.verifyAllDefaultGlobalConfigurations();
    }
}
