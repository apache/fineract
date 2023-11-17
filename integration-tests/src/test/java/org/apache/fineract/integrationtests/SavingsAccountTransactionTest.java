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

import static org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper.PAYMENT_TYPE_ID;
import static org.apache.fineract.integrationtests.common.system.DatatableHelper.addDatatableColumn;
import static org.apache.http.HttpStatus.SC_CONFLICT;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.apache.fineract.batch.domain.Header;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.integrationtests.common.BatchHelper;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsProductHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsStatusChecker;
import org.apache.fineract.integrationtests.common.savings.SavingsTransactionData;
import org.apache.fineract.integrationtests.common.system.DatatableHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({ "rawtypes" })
public class SavingsAccountTransactionTest {

    private static final Logger log = LoggerFactory.getLogger(SavingsAccountTransactionTest.class);

    public static final String ACCOUNT_TYPE_INDIVIDUAL = "INDIVIDUAL";
    final String startDateString = "03 June 2023";
    final String depositDateString = "05 June 2023";
    final String withdrawDateString = "10 June 2023";

    private ResponseSpecification responseSpec;
    private ResponseSpecification concurrentResponseSpec;
    private ResponseSpecification deadlockResponseSpec;
    private RequestSpecification requestSpec;
    private SavingsProductHelper savingsProductHelper;
    private SavingsAccountHelper savingsAccountHelper;
    private DatatableHelper datatableHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(SC_OK).build();
        this.concurrentResponseSpec = new ResponseSpecBuilder().expectStatusCode(anyOf(is(SC_OK), is(SC_CONFLICT))).build();
        this.deadlockResponseSpec = new ResponseSpecBuilder().expectStatusCode(anyOf(is(SC_OK), is(SC_CONFLICT), is(SC_FORBIDDEN))).build();
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.savingsProductHelper = new SavingsProductHelper();
        this.datatableHelper = new DatatableHelper(this.requestSpec, this.responseSpec);
    }

    @Test
    public void verifySavingsTransactionSubmittedOnDateAndTransactionDate() throws JsonProcessingException {
        LocalDate today = Utils.getLocalDateOfTenant();
        try {
            enableBusinessDate(requestSpec, responseSpec, true);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, today);

            LocalDate depositDate = Utils.getDateAsLocalDate(depositDateString);
            LocalDate withdrawDate = Utils.getDateAsLocalDate(withdrawDateString);

            final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, startDateString);
            assertNotNull(clientID);

            final Integer savingsId = createSavingsAccountDailyPosting(clientID, startDateString);
            assertNotNull(savingsId);

            performSavingsTransaction(savingsId, "100", depositDate, true);
            performSavingsTransaction(savingsId, "50", withdrawDate, false);
        } finally {
            enableBusinessDate(requestSpec, responseSpec, false);
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

        SavingsAccountHelper concurrentHelper = new SavingsAccountHelper(requestSpec, concurrentResponseSpec);
        String transactionDate = SavingsAccountHelper.TRANSACTION_DATE;
        String transactionAmount = "10";
        ExecutorService executor = Executors.newFixedThreadPool(30);
        for (int i = 0; i < 10; i++) {
            log.info("Starting concurrent transaction number {}", i);
            SavingsTransactionData transactionData = SavingsTransactionData.builder().transactionDate(transactionDate)
                    .transactionAmount(transactionAmount).paymentTypeId(PAYMENT_TYPE_ID).note("note_" + i).build();
            Runnable worker = new TransactionExecutor(concurrentHelper, savingsId, transactionData);
            executor.execute(worker);
        }

        executor.shutdown();
        // Wait until all threads are finish
        while (!executor.isTerminated()) {

        }
        log.info("\nFinished all threads");
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
        String datatableName = Utils.uniqueRandomStringGenerator("savings_transaction" + "_", 5).toLowerCase();
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

        SavingsAccountHelper batchWithTransactionHelper = new SavingsAccountHelper(requestSpec, concurrentResponseSpec);
        SavingsAccountHelper batchWithoutTransactionHelper = new SavingsAccountHelper(requestSpec,
                new ResponseSpecBuilder().expectStatusCode(anyOf(is(SC_OK), is(SC_CONFLICT), is(SC_FORBIDDEN))).build());
        String transactionDate = SavingsAccountHelper.TRANSACTION_DATE;
        String transactionAmount = "10";
        ExecutorService executor = Executors.newFixedThreadPool(30);
        ArrayList<Future<?>> results = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            log.info("Starting concurrent transaction number {}", i);
            SavingsTransactionData transactionData = SavingsTransactionData.builder().transactionDate(transactionDate)
                    .transactionAmount(transactionAmount).paymentTypeId(PAYMENT_TYPE_ID).note("note_" + i).build();
            Runnable workerWithTransaction = new TransactionExecutor(batchWithTransactionHelper, savingsId, transactionData, true,
                    datatableName, columnNames);
            results.add(executor.submit(workerWithTransaction));
            Runnable workerWithoutTransaction = new TransactionExecutor(batchWithoutTransactionHelper, savingsId, transactionData, false,
                    datatableName, columnNames);
            results.add(executor.submit(workerWithoutTransaction));
        }

        executor.shutdown();
        // Wait until all threads are finish
        while (!executor.isTerminated()) {

        }
        this.datatableHelper.deleteDatatable(datatableName);
        try {
            for (Future<?> result : results) {
                assertNull(result.get());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        log.info("\nFinished all threads");
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

        SavingsAccountHelper batchWithTransactionHelper = new SavingsAccountHelper(requestSpec, deadlockResponseSpec);
        String transactionDate = SavingsAccountHelper.TRANSACTION_DATE;
        String transactionAmount = "10";

        ExecutorService executor = Executors.newFixedThreadPool(30);
        ArrayList<Future<?>> results = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            log.info("Starting concurrent transaction number {}", i);
            SavingsTransactionData transactionData1 = SavingsTransactionData.builder().transactionDate(transactionDate)
                    .transactionAmount(transactionAmount).paymentTypeId(PAYMENT_TYPE_ID).note("note1_" + i).build();
            results.add(executor.submit(() -> {
                runDeadlockBatch(batchWithTransactionHelper, savingsId1, savingsId2, transactionData1);
            }));
            SavingsTransactionData transactionData2 = SavingsTransactionData.builder().transactionDate(transactionDate)
                    .transactionAmount(transactionAmount).paymentTypeId(PAYMENT_TYPE_ID).note("note2_" + i).build();
            results.add(executor.submit(() -> {
                runDeadlockBatch(batchWithTransactionHelper, savingsId2, savingsId1, transactionData2);
            }));
        }

        executor.shutdown();
        // Wait until all threads are finish
        while (!executor.isTerminated()) {

        }
        try {
            for (Future<?> result : results) {
                assertNull(result.get());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        log.info("\nFinished all threads");
    }

    private void enableBusinessDate(RequestSpecification requestSpec, ResponseSpecification responseSpec, boolean enable) {
        GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, enable);
    }

    private void performSavingsTransaction(Integer savingsId, String amount, LocalDate transactionDate, boolean isDeposit) {
        String transactionType = isDeposit ? "Deposit" : "Withdrawal";
        Integer transactionId = isDeposit
                ? (Integer) this.savingsAccountHelper.depositToSavingsAccount(savingsId, amount, depositDateString,
                        CommonConstants.RESPONSE_RESOURCE_ID)
                : (Integer) this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, amount, withdrawDateString,
                        CommonConstants.RESPONSE_RESOURCE_ID);

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

    private Integer createSavingsAccountDailyPosting(final Integer clientID, final String startDate) {
        final Integer savingsProductID = createSavingsProductDailyPosting();
        assertNotNull(savingsProductID);
        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplicationOnDate(clientID, savingsProductID,
                ACCOUNT_TYPE_INDIVIDUAL, startDate);
        assertNotNull(savingsId);
        HashMap savingsStatusHashMap = this.savingsAccountHelper.approveSavingsOnDate(savingsId, startDate);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);
        savingsStatusHashMap = this.savingsAccountHelper.activateSavingsAccount(savingsId, startDate);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);
        return savingsId;
    }

    private Integer createSavingsProductDailyPosting() {
        final String savingsProductJSON = this.savingsProductHelper.withInterestCompoundingPeriodTypeAsDaily()
                .withInterestPostingPeriodTypeAsDaily().withInterestCalculationPeriodTypeAsDailyBalance().build();
        return SavingsProductHelper.createSavingsProduct(savingsProductJSON, requestSpec, responseSpec);
    }

    public static class TransactionExecutor implements Runnable {

        private final SavingsAccountHelper savingsHelper;
        private final Integer savingsId;
        SavingsTransactionData transactionData;
        private final boolean batch;
        private final boolean enclosingTransaction;
        private final String datatableName;
        private final List<String> columnNames;

        private TransactionExecutor(SavingsAccountHelper savingsHelper, Integer savingsId, SavingsTransactionData transactionData,
                boolean batch, boolean enclosingTransaction, String datatableName, List<String> columnNames) {
            this.savingsId = savingsId;
            this.savingsHelper = savingsHelper;
            this.transactionData = transactionData;
            this.batch = batch;
            this.enclosingTransaction = enclosingTransaction;
            this.datatableName = datatableName;
            this.columnNames = columnNames;
        }

        TransactionExecutor(SavingsAccountHelper savingsAccountHelper, Integer savingsId, SavingsTransactionData transactionData) {
            this(savingsAccountHelper, savingsId, transactionData, false, false, null, null);
        }

        TransactionExecutor(SavingsAccountHelper batchHelper, Integer savingsId, SavingsTransactionData transactionData,
                boolean enclosingTransaction, String datatableName, List<String> columnNames) {
            this(batchHelper, savingsId, transactionData, true, enclosingTransaction, datatableName, columnNames);
        }

        @Override
        public void run() {
            log.info("Details of passed concurrent transaction, details (date, amount, note, savingsId) are {},{},{},{}",
                    transactionData.getTransactionDate(), transactionData.getTransactionAmount(), transactionData.getNote(), savingsId);
            if (batch) {
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
                RequestSpecification requestSpec = savingsHelper.getRequestSpec();
                ResponseSpecification responseSpec = savingsHelper.getResponseSpec();
                final List<BatchResponse> responses = enclosingTransaction
                        ? BatchHelper.postBatchRequestsWithEnclosingTransaction(requestSpec, responseSpec, json)
                        : BatchHelper.postBatchRequestsWithoutEnclosingTransaction(requestSpec, responseSpec, json);
                assertNotNull(responses);
                if (enclosingTransaction) {
                    Integer statusCode1 = responses.get(0).getStatusCode();
                    assertNotNull(statusCode1);
                    assertTrue(SC_OK == statusCode1 || SC_CONFLICT == statusCode1, "Status code: " + statusCode1);
                    if (SC_OK == statusCode1) {
                        assertEquals(4, responses.size());
                        Integer statusCode4 = responses.get(3).getStatusCode();
                        assertNotNull(statusCode4);
                        assertEquals(SC_OK, statusCode4);
                    } else {
                        assertEquals(1, responses.size());
                    }
                } else {
                    assertEquals(4, responses.size());
                    Integer statusCode1 = responses.get(0).getStatusCode();
                    assertNotNull(statusCode1);
                    assertTrue(SC_OK == statusCode1 || SC_CONFLICT == statusCode1, "Status code: " + statusCode1);
                    Integer statusCode4 = responses.get(3).getStatusCode();
                    assertNotNull(statusCode4);
                    assertTrue(SC_OK == statusCode1 ? (SC_OK == statusCode4 || SC_CONFLICT == statusCode4)
                            : (SC_FORBIDDEN == statusCode4 || SC_CONFLICT == statusCode4), "Status code: " + statusCode4);
                }
            } else {
                String json = transactionData.getJson();
                String response = (String) this.savingsHelper.depositToSavingsAccount(savingsId, json, null);
                boolean success = checkConcurrentResponse(response);
                if (success) {
                    response = (String) this.savingsHelper.withdrawalFromSavingsAccount(savingsId, json, null);
                    checkConcurrentResponse(response);
                }
            }
        }

        private static boolean checkConcurrentResponse(String response) {
            assertNotNull(response);
            JsonPath res = JsonPath.from(response);
            String statusCode = res.get("httpStatusCode");
            if (statusCode == null) {
                assertNotNull(res.get(CommonConstants.RESPONSE_RESOURCE_ID));
                return true;
            }
            assertEquals(String.valueOf(SC_CONFLICT), statusCode);
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
        assertNotNull(responses);
        BatchResponse response1 = responses.get(0);
        Integer statusCode = response1.getStatusCode();
        String msg = Strings.nullToEmpty(response1.getBody());
        assertNotNull(statusCode);
        assertTrue(
                SC_OK == statusCode || SC_CONFLICT == statusCode
                        || (SC_FORBIDDEN == statusCode && msg.contains("Cannot add or update a child row")),
                "Status code: " + statusCode + ", message: " + msg);
        if (SC_OK == statusCode) {
            assertEquals(4, responses.size());
            Integer statusCode4 = responses.get(3).getStatusCode();
            assertNotNull(statusCode4);
            assertEquals(SC_OK, statusCode4);
        } else {
            assertEquals(1, responses.size());
        }
    }

    // Reset configuration fields
    @AfterEach
    public void tearDown() {
        GlobalConfigurationHelper.resetAllDefaultGlobalConfigurations(this.requestSpec, this.responseSpec);
        GlobalConfigurationHelper.verifyAllDefaultGlobalConfigurations(this.requestSpec, this.responseSpec);
    }
}
