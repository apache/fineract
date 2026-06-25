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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetCodesResponse;
import org.apache.fineract.client.models.GetDisbursementDetail;
import org.apache.fineract.client.models.GetWorkingCapitalLoanTransactionIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoanTransactionsResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.PostCodeValueDataResponse;
import org.apache.fineract.client.models.PostCodeValuesDataRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansLoanIdRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansRequest;
import org.apache.fineract.client.models.ProjectedAmortizationScheduleData;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.infrastructure.event.external.data.ExternalEventResponse;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignExternalEventHelper;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.system.CodeHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanDisbursementTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.apache.fineract.portfolio.workingcapitalloan.WorkingCapitalLoanConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class WorkingCapitalLoanDisbursementTest {

    private final WorkingCapitalLoanHelper applicationHelper = new WorkingCapitalLoanHelper();
    private final WorkingCapitalLoanProductHelper productHelper = new WorkingCapitalLoanProductHelper();
    private final CodeHelper codeHelper = new CodeHelper();
    private final GlobalConfigurationHelper globalConfigurationHelper = new GlobalConfigurationHelper();
    private final FeignExternalEventHelper externalEventHelper = new FeignExternalEventHelper(
            FineractFeignClientHelper.getFineractFeignClient());

    private final List<Long> createdLoanIds = new ArrayList<>();
    private final List<Long> createdProductIds = new ArrayList<>();
    private final Long createdClientId = createClient();

    private static final PostWorkingCapitalLoansLoanIdRequest CLEANUP_EMPTY_COMMAND_REQUEST = WorkingCapitalLoanApplicationTestBuilder
            .buildUndoApproveRequest();
    private static final DateTimeFormatter BUSINESS_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy");
    private static final String WC_DISBURSAL_TXN_EVENT = "WorkingCapitalLoanDisbursalTransactionBusinessEvent";
    private static final String WC_UNDO_DISBURSAL_TXN_EVENT = "WorkingCapitalLoanUndoDisbursalTransactionBusinessEvent";

    @AfterEach
    void cleanupEntities() {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                new PutGlobalConfigurationsRequest().enabled(false));

        // Loans: undo disbursal -> undo approval -> delete
        for (final Long loanId : createdLoanIds) {
            if (loanId == null) {
                continue;
            }
            try {
                applicationHelper.undoDisbursalById(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildUndoDisburseRequest());
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup (loan may not be disbursed / client inactive / loan already removed)
            }
            try {
                applicationHelper.undoApprovalById(loanId, CLEANUP_EMPTY_COMMAND_REQUEST);
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup (loan may not be approved / already removed)
            }
            try {
                applicationHelper.deleteById(loanId);
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup (loan may be in non-deletable state / already removed)
            }
        }
        createdLoanIds.clear();

        // Products
        for (final Long productId : createdProductIds) {
            if (productId == null) {
                continue;
            }
            try {
                productHelper.deleteWorkingCapitalLoanProductById(productId);
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup (product may be already removed)
            }
        }
        createdProductIds.clear();
    }

    @Test
    public void testDisburseWorkingCapitalLoan() {
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        runAtBusinessDate(currentDate, () -> {
            final Long productId = createProduct();
            final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                    .withClientId(createdClientId) //
                    .withProductId(productId) //
                    .withPrincipal(BigDecimal.valueOf(5000)) //
                    .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                    .buildSubmitRequest());

            applicationHelper.approveById(loanId,
                    WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(currentDate, BigDecimal.valueOf(5000), null));

            final var disburseJson = WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(currentDate, BigDecimal.valueOf(5000));
            applicationHelper.disburseById(loanId, disburseJson);

            final GetWorkingCapitalLoansLoanIdResponse data = applicationHelper.retrieveById(loanId);
            assertNotNull(data);
            assertStatus(data, "loanStatusType.active");
            assertNotNull(data.getBalance(), "GET loan after disburse should include balance");
            assertEqualBigDecimal(BigDecimal.valueOf(5000), data.getBalance().getPrincipalOutstanding());

            assertNotNull(data.getDisbursementDetails(), "GET loan after disburse should include disbursementDetails array");
            assertFalse(data.getDisbursementDetails().isEmpty(), "disbursementDetails should not be empty");
            final GetDisbursementDetail disbursement = data.getDisbursementDetails().getFirst();
            assertNotNull(disbursement.getActualDisbursementDate());
            assertDateEquals(currentDate, disbursement.getActualDisbursementDate());
            assertNotNull(disbursement.getActualAmount());
            assertEqualBigDecimal(BigDecimal.valueOf(5000), disbursement.getActualAmount());

            GetWorkingCapitalLoanTransactionsResponse loanTransactionsResponse = applicationHelper.retrieveTransactionsByLoanId(loanId);
            assertEquals(1, loanTransactionsResponse.getContent().size(), "After disburse there should be one transaction");
            final GetWorkingCapitalLoanTransactionIdResponse txn = loanTransactionsResponse.getContent().getFirst();
            assertNotNull(txn.getType());
            assertEquals("loanTransactionType.disbursement", txn.getType().getCode());
            assertEqualBigDecimal(BigDecimal.valueOf(5000), txn.getTransactionAmount());
            assertNotEquals(Boolean.TRUE, txn.getReversed(), "Disbursement transaction should not be reversed");
            assertEqualBigDecimal(BigDecimal.valueOf(5000), txn.getPrincipalPortion());
            assertEqualBigDecimal(BigDecimal.ZERO, txn.getFeeChargesPortion());
            assertEqualBigDecimal(BigDecimal.ZERO, txn.getPenaltyChargesPortion());
        });
    }

    @Test
    public void testDisburseWithClassificationIdStoredOnTransaction() {
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        runAtBusinessDate(currentDate, () -> {
            final GetCodesResponse code = codeHelper.retrieveCodeByName(WorkingCapitalLoanConstants.DISBURSEMENT_CLASSIFICATION_CODE_NAME);
            final PostCodeValueDataResponse classificationCode = codeHelper.createCodeValue(code.getId(),
                    new PostCodeValuesDataRequest().name(Utils.uniqueRandomStringGenerator("WCL_CLS_", 8)).isActive(true).position(0));
            final Long classificationId = classificationCode.getSubResourceId();

            final Long productId = createProduct();
            final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                    .withClientId(createdClientId) //
                    .withProductId(productId) //
                    .withPrincipal(BigDecimal.valueOf(5000)) //
                    .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                    .buildSubmitRequest());

            applicationHelper.approveById(loanId,
                    WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(currentDate, BigDecimal.valueOf(5000), null));

            applicationHelper.disburseById(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(currentDate,
                    BigDecimal.valueOf(5000), classificationId));

            GetWorkingCapitalLoanTransactionsResponse loanTransactionsResponse = applicationHelper.retrieveTransactionsByLoanId(loanId);
            assertEquals(1, loanTransactionsResponse.getContent().size(), "After disburse there should be one transaction");
            final GetWorkingCapitalLoanTransactionIdResponse txn = loanTransactionsResponse.getContent().getFirst();
            assertNotNull(txn);
            assertNotNull(txn.getClassification(), "Disbursement transaction should include classification");
            assert classificationId != null;
            assertEquals(classificationId.longValue(), txn.getClassification().getId());
            final long transactionId = txn.getId();
            final GetWorkingCapitalLoanTransactionIdResponse txnById = applicationHelper.retrieveTransactionByLoanIdAndTransactionId(loanId,
                    transactionId);
            assertNotNull(txnById.getClassification());
            assertEquals(classificationId.longValue(), txnById.getClassification().getId());
        });
    }

    @Test
    public void testDisburseWithNonExistentClassificationIdFails() {
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        runAtBusinessDate(currentDate, () -> {
            final Long productId = createProduct();
            final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                    .withClientId(createdClientId) //
                    .withProductId(productId) //
                    .withPrincipal(BigDecimal.valueOf(5000)) //
                    .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                    .buildSubmitRequest());

            applicationHelper.approveById(loanId,
                    WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(currentDate, BigDecimal.valueOf(5000), null));

            final var disburseJson = WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(currentDate, BigDecimal.valueOf(5000),
                    9_999_999_999L);
            final CallFailedRuntimeException ex = applicationHelper.runDisburseExpectingFailure(loanId, disburseJson);
            assertEquals(400, ex.getStatus());
            assertNotNull(ex.getDeveloperMessage());
            final String msg = ex.getDeveloperMessage();
            assertTrue(msg.contains("classificationId") || msg.contains("Code value") || msg.toLowerCase().contains("code value"),
                    "Expected validation message for invalid classificationId: " + msg);
        });
    }

    @Test
    public void testDisburseWithClassificationIdFromWrongCodeBookFails() {
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        runAtBusinessDate(currentDate, () -> {
            final GetCodesResponse loanPurposeCode = codeHelper.retrieveCodeByName("LoanPurpose");
            final PostCodeValueDataResponse wrongBookValue = codeHelper.createCodeValue(loanPurposeCode.getId(),
                    new PostCodeValuesDataRequest().name(Utils.uniqueRandomStringGenerator("WCL_WRG_", 8)).isActive(true).position(0));

            final Long productId = createProduct();
            final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                    .withClientId(createdClientId) //
                    .withProductId(productId) //
                    .withPrincipal(BigDecimal.valueOf(5000)) //
                    .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                    .buildSubmitRequest());

            applicationHelper.approveById(loanId,
                    WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(currentDate, BigDecimal.valueOf(5000), null));

            final var disburseJson = WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(currentDate, BigDecimal.valueOf(5000),
                    wrongBookValue.getSubResourceId());
            final CallFailedRuntimeException ex = applicationHelper.runDisburseExpectingFailure(loanId, disburseJson);
            assertEquals(400, ex.getStatus());
            assertNotNull(ex.getDeveloperMessage());
            final String developerMessage = ex.getDeveloperMessage();
            assertTrue(developerMessage.contains("code.value.classification.not.exists") || developerMessage.contains("classificationId"),
                    "Expected classification validation error: " + developerMessage);
        });
    }

    @Test
    public void testDisburseWithAllRequestFieldsAndVerifyResponse() {
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        runAtBusinessDate(currentDate, () -> {
            final Long productId = createProductWithDiscountAllowed();

            final BigDecimal approvedPrincipal = BigDecimal.valueOf(10000);
            final BigDecimal approvedDiscount = BigDecimal.valueOf(50);
            final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                    .withClientId(createdClientId) //
                    .withProductId(productId) //
                    .withPrincipal(approvedPrincipal) //
                    .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                    .buildSubmitRequest());

            final BigDecimal transactionAmount = BigDecimal.valueOf(8000);
            final BigDecimal discountAmount = BigDecimal.valueOf(30);
            final String note = "Disbursal note for test";
            final Integer paymentTypeId = 1;
            final String accountNumber = "acc-" + UUID.randomUUID().toString().substring(0, 8);
            final String checkNumber = "chk-123";
            final String routingCode = "rte-456";
            final String receiptNumber = "rec-789";
            final String bankNumber = "bnk-001";

            applicationHelper.approveById(loanId,
                    WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(currentDate, approvedPrincipal, approvedDiscount));

            final var disburseJson = WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(currentDate, transactionAmount,
                    discountAmount, note, paymentTypeId, accountNumber, checkNumber, routingCode, receiptNumber, bankNumber);
            applicationHelper.disburseById(loanId, disburseJson);

            final GetWorkingCapitalLoansLoanIdResponse data = applicationHelper.retrieveById(loanId);
            assertNotNull(data);

            assertStatus(data, "loanStatusType.active");
            assertNotNull(data.getBalance(), "GET loan after disburse should include balance");
            assertEqualBigDecimal(transactionAmount.add(discountAmount), data.getBalance().getPrincipalOutstanding());
            assertEqualBigDecimal(discountAmount, data.getDiscountFee());
            assertEquals(loanId, data.getId());
            assertNotNull(data.getClientId());
            assertNotNull(data.getLoanProductId());

            if (data.getTimeline() != null) {
                assertNotNull(data.getTimeline().getActualDisbursementDate());
                assertDateEquals(currentDate, data.getTimeline().getActualDisbursementDate());
                assertNotNull(data.getTimeline().getApprovedOnDate());
                assertNull(data.getTimeline().getActualMaturityDate(), "Expected actualMaturityDate to be null after disbursement");
            }
            assertNotNull(data.getDisbursementDetails(), "GET loan after disburse should include disbursementDetails array");
            assertFalse(data.getDisbursementDetails().isEmpty(), "disbursementDetails should not be empty");
            final GetDisbursementDetail disbursement = data.getDisbursementDetails().getFirst();
            assertNotNull(disbursement.getExpectedDisbursementDate(), "disbursementDetails should include expectedDisbursementDate");
            assertNotNull(disbursement.getPrincipal(), "disbursementDetails should include principal");
            assertDateEquals(currentDate, disbursement.getActualDisbursementDate());
            assertEqualBigDecimal(transactionAmount, disbursement.getActualAmount());

            GetWorkingCapitalLoanTransactionsResponse loanTransactionsResponse = applicationHelper.retrieveTransactionsByLoanId(loanId);
            assertEquals(2, loanTransactionsResponse.getContent().size(), "After disburse there should be one transaction");
            final GetWorkingCapitalLoanTransactionIdResponse txn = loanTransactionsResponse.getContent().getFirst();
            assertEqualBigDecimal(transactionAmount, txn.getTransactionAmount());
            assertEqualBigDecimal(transactionAmount, txn.getPrincipalPortion());
            assertEqualBigDecimal(BigDecimal.ZERO, txn.getFeeChargesPortion());
            assertEqualBigDecimal(BigDecimal.ZERO, txn.getPenaltyChargesPortion());
            assertNotNull(txn.getPaymentDetailData(), "Transaction should include paymentDetailData");
            assertEquals(accountNumber, txn.getPaymentDetailData().getAccountNumber());
            assertEquals(checkNumber, txn.getPaymentDetailData().getCheckNumber());
            assertEquals(routingCode, txn.getPaymentDetailData().getRoutingCode());
            assertEquals(receiptNumber, txn.getPaymentDetailData().getReceiptNumber());
            assertEquals(bankNumber, txn.getPaymentDetailData().getBankNumber());
        });
    }

    @Test
    public void testUndoDisburseWorkingCapitalLoan() {
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        runAtBusinessDate(currentDate, () -> {
            final Long productId = createProduct();

            final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                    .withClientId(createdClientId) //
                    .withProductId(productId) //
                    .withPrincipal(BigDecimal.valueOf(5000)) //
                    .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                    .buildSubmitRequest());

            applicationHelper.approveById(loanId,
                    WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(currentDate, BigDecimal.valueOf(5000), null));

            applicationHelper.disburseById(loanId,
                    WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(currentDate, BigDecimal.valueOf(5000)));

            applicationHelper.undoDisbursalById(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildUndoDisburseRequest());

            final GetWorkingCapitalLoansLoanIdResponse data = applicationHelper.retrieveById(loanId);
            assertNotNull(data);
            assertStatus(data, "loanStatusType.approved");
            assertEqualBigDecimal(BigDecimal.valueOf(5000), data.getApprovedPrincipal());
            assertNotNull(data.getBalance(), "GET loan after undo should include balance");
            assertEqualBigDecimal(BigDecimal.valueOf(5000), data.getBalance().getPrincipalOutstanding());

            assertNotNull(data.getDisbursementDetails(), "GET loan after undo should include disbursementDetails array");
            assertFalse(data.getDisbursementDetails().isEmpty(), "disbursementDetails should not be empty");
            final GetDisbursementDetail disbursement = data.getDisbursementDetails().getFirst();
            assertNull(disbursement.getActualDisbursementDate(), "Expected actualDisbursementDate to be null after undo");
            assertNull(disbursement.getActualAmount(), "Expected actualAmount to be null after undo");
            assertNotNull(data.getTimeline(), "GET loan after undo should include timeline");
            assertNull(data.getTimeline().getActualMaturityDate(), "Expected actualMaturityDate to be null after undo");

            GetWorkingCapitalLoanTransactionsResponse loanTransactionsResponse = applicationHelper.retrieveTransactionsByLoanId(loanId);
            assertEquals(1, loanTransactionsResponse.getContent().size(), "After disburse there should be one transaction");
            final GetWorkingCapitalLoanTransactionIdResponse txn = loanTransactionsResponse.getContent().getFirst();
            assertEquals(Boolean.TRUE, txn.getReversed(), "Expected transaction to be reversed");
        });
    }

    @Test
    public void testDisbursementExternalBusinessEventPublished() {
        externalEventHelper.enableBusinessEvent(WC_DISBURSAL_TXN_EVENT);
        try {
            final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
            runAtBusinessDate(currentDate, () -> {
                final Long productId = createProduct();
                final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                        .withClientId(createdClientId) //
                        .withProductId(productId) //
                        .withPrincipal(BigDecimal.valueOf(5000)) //
                        .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                        .buildSubmitRequest());

                applicationHelper.approveById(loanId,
                        WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(currentDate, BigDecimal.valueOf(5000), null));

                externalEventHelper.deleteAllExternalEvents();
                applicationHelper.disburseById(loanId,
                        WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(currentDate, BigDecimal.valueOf(5000)));

                GetWorkingCapitalLoanTransactionsResponse loanTransactionsResponse = applicationHelper.retrieveTransactionsByLoanId(loanId);
                assertEquals(1, loanTransactionsResponse.getContent().size(), "After disburse there should be one transaction");
                final GetWorkingCapitalLoanTransactionIdResponse txn = loanTransactionsResponse.getContent().getFirst();

                final List<ExternalEventResponse> events = externalEventHelper.getExternalEventsByType(WC_DISBURSAL_TXN_EVENT);
                final ExternalEventResponse event = events.stream().filter(e -> loanId.equals(e.getAggregateRootId())).findFirst()
                        .orElse(null);
                assertNotNull(event, "Expected disbursal transaction external event for loan");
                assertEquals(WC_DISBURSAL_TXN_EVENT, event.getType());
                assertEquals(loanId, event.getAggregateRootId());
                assertEquals(txn.getId(), ((Number) event.getPayLoad().get("id")).longValue());
                assertEquals(loanId, ((Number) event.getPayLoad().get("wcLoanId")).longValue());
                assertEquals(Boolean.FALSE, event.getPayLoad().get("reversed"));
            });
        } finally {
            externalEventHelper.disableBusinessEvent(WC_DISBURSAL_TXN_EVENT);
        }
    }

    @Test
    public void testUndoDisbursementExternalBusinessEventPublished() {

        externalEventHelper.enableBusinessEvent(WC_UNDO_DISBURSAL_TXN_EVENT);
        try {
            final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
            runAtBusinessDate(currentDate, () -> {
                final Long productId = createProduct();
                final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                        .withClientId(createdClientId) //
                        .withProductId(productId) //
                        .withPrincipal(BigDecimal.valueOf(5000)) //
                        .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                        .buildSubmitRequest());

                applicationHelper.approveById(loanId,
                        WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(currentDate, BigDecimal.valueOf(5000), null));
                applicationHelper.disburseById(loanId,
                        WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(currentDate, BigDecimal.valueOf(5000)));

                GetWorkingCapitalLoanTransactionsResponse loanTransactionsResponse = applicationHelper.retrieveTransactionsByLoanId(loanId);
                assertEquals(1, loanTransactionsResponse.getContent().size(), "After disburse there should be one transaction");
                final GetWorkingCapitalLoanTransactionIdResponse txn = loanTransactionsResponse.getContent().getFirst();

                externalEventHelper.deleteAllExternalEvents();
                applicationHelper.undoDisbursalById(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildUndoDisburseRequest());

                final List<ExternalEventResponse> events = externalEventHelper.getExternalEventsByType(WC_UNDO_DISBURSAL_TXN_EVENT);
                final ExternalEventResponse event = events.stream().filter(e -> loanId.equals(e.getAggregateRootId())).findFirst()
                        .orElse(null);
                assertNotNull(event, "Expected undo disbursal transaction external event for loan");
                assertEquals(WC_UNDO_DISBURSAL_TXN_EVENT, event.getType());
                assertEquals(loanId, event.getAggregateRootId());
                assertEquals(txn.getId(), ((Number) event.getPayLoad().get("id")).longValue());
                assertEquals(loanId, ((Number) event.getPayLoad().get("wcLoanId")).longValue());
                assertEquals(Boolean.TRUE, event.getPayLoad().get("reversed"));
            });
        } finally {
            externalEventHelper.disableBusinessEvent(WC_UNDO_DISBURSAL_TXN_EVENT);
        }
    }

    @Test
    public void testUndoDisbursalWithNote() {
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        runAtBusinessDate(currentDate, () -> {
            final Long productId = createProduct();

            final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                    .withClientId(createdClientId) //
                    .withProductId(productId) //
                    .withPrincipal(BigDecimal.valueOf(5000)) //
                    .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                    .buildSubmitRequest());

            applicationHelper.approveById(loanId,
                    WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(currentDate, BigDecimal.valueOf(5000), null));
            applicationHelper.disburseById(loanId,
                    WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(currentDate, BigDecimal.valueOf(5000)));

            applicationHelper.undoDisbursalById(loanId,
                    WorkingCapitalLoanDisbursementTestBuilder.buildUndoDisburseRequest("Undo disbursal note"));

            final GetWorkingCapitalLoansLoanIdResponse data = applicationHelper.retrieveById(loanId);
            assertNotNull(data);
            assertStatus(data, "loanStatusType.approved");
        });
    }

    @Test
    public void testDisburseWithMissingActualDisbursementDate() {
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        runAtBusinessDate(currentDate, () -> {
            final Long productId = createProduct();

            final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                    .withClientId(createdClientId) //
                    .withProductId(productId) //
                    .withPrincipal(BigDecimal.valueOf(5000)) //
                    .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                    .buildSubmitRequest());

            applicationHelper.approveById(loanId,
                    WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(currentDate, BigDecimal.valueOf(5000), null));

            final var disburseJson = WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(null, BigDecimal.valueOf(5000));
            final CallFailedRuntimeException ex = applicationHelper.runDisburseExpectingFailure(loanId, disburseJson);
            assertEquals(400, ex.getStatus());
            assertNotNull(ex.getDeveloperMessage());
            assertTrue(ex.getDeveloperMessage().contains("actualDisbursementDate")
                    && (ex.getDeveloperMessage().contains("mandatory") || ex.getDeveloperMessage().contains("null")));
        });
    }

    @Test
    public void testDisburseWithMissingTransactionAmount() {
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        runAtBusinessDate(currentDate, () -> {
            final Long productId = createProduct();

            final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                    .withClientId(createdClientId) //
                    .withProductId(productId) //
                    .withPrincipal(BigDecimal.valueOf(5000)) //
                    .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                    .buildSubmitRequest());

            applicationHelper.approveById(loanId,
                    WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(currentDate, BigDecimal.valueOf(5000), null));

            final var disburseJson = WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(currentDate, null);
            final CallFailedRuntimeException ex = applicationHelper.runDisburseExpectingFailure(loanId, disburseJson);
            assertEquals(400, ex.getStatus());
            assertNotNull(ex.getDeveloperMessage());
            assertTrue(
                    ex.getDeveloperMessage().contains("transactionAmount")
                            && (ex.getDeveloperMessage().contains("mandatory") || ex.getDeveloperMessage().contains("null")),
                    "Expected message about mandatory transactionAmount: " + ex.getDeveloperMessage());
        });
    }

    @Test
    public void testDisburseWithTransactionAmountExceedingApproved() {
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        runAtBusinessDate(currentDate, () -> {
            final Long productId = createProduct();

            final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                    .withClientId(createdClientId) //
                    .withProductId(productId) //
                    .withPrincipal(BigDecimal.valueOf(5000)) //
                    .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                    .buildSubmitRequest());

            applicationHelper.approveById(loanId,
                    WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(currentDate, BigDecimal.valueOf(5000), null));

            final var disburseJson = WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(currentDate, BigDecimal.valueOf(6000));
            final CallFailedRuntimeException ex = applicationHelper.runDisburseExpectingFailure(loanId, disburseJson);
            assertEquals(400, ex.getStatus());
            assertNotNull(ex.getDeveloperMessage());
            assertTrue(ex.getDeveloperMessage().contains("amount.cannot.exceed.approved.principal"));
        });
    }

    @Test
    public void testDisburseWithNegativeTransactionAmount() {
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        runAtBusinessDate(currentDate, () -> {
            final Long productId = createProduct();

            final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                    .withClientId(createdClientId) //
                    .withProductId(productId) //
                    .withPrincipal(BigDecimal.valueOf(5000)) //
                    .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                    .buildSubmitRequest());

            applicationHelper.approveById(loanId,
                    WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(currentDate, BigDecimal.valueOf(5000), null));

            final var disburseJson = WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(currentDate, BigDecimal.valueOf(-100));
            final CallFailedRuntimeException ex = applicationHelper.runDisburseExpectingFailure(loanId, disburseJson);
            assertEquals(400, ex.getStatus());
            assertNotNull(ex.getDeveloperMessage());
            assertTrue(ex.getDeveloperMessage().toLowerCase().contains("transactionamount") || ex.getDeveloperMessage().contains("positive")
                    || ex.getDeveloperMessage().contains("greater"));
        });
    }

    @Test
    public void testDisburseWithFutureDate() {
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        runAtBusinessDate(currentDate, () -> {
            final Long productId = createProduct();

            final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                    .withClientId(createdClientId) //
                    .withProductId(productId) //
                    .withPrincipal(BigDecimal.valueOf(5000)) //
                    .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                    .buildSubmitRequest());

            applicationHelper.approveById(loanId,
                    WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(currentDate, BigDecimal.valueOf(5000), null));

            final LocalDate futureDate = currentDate.plusDays(30);
            final var disburseJson = WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(futureDate, BigDecimal.valueOf(5000));
            final CallFailedRuntimeException ex = applicationHelper.runDisburseExpectingFailure(loanId, disburseJson);
            assertEquals(400, ex.getStatus());
            assertNotNull(ex.getDeveloperMessage());
            assertTrue(ex.getDeveloperMessage().contains("future.date") || ex.getDeveloperMessage().contains("actualDisbursementDate"));
        });
    }

    @Test
    public void testDisburseWithDateBeforeApproval() {
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        runAtBusinessDate(currentDate, () -> {
            final Long productId = createProduct();

            final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                    .withClientId(createdClientId) //
                    .withProductId(productId) //
                    .withPrincipal(BigDecimal.valueOf(5000)) //
                    .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                    .buildSubmitRequest());

            applicationHelper.approveById(loanId,
                    WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(currentDate, BigDecimal.valueOf(5000), null));

            final LocalDate beforeApproval = currentDate.minusDays(1);
            final var disburseJson = WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(beforeApproval,
                    BigDecimal.valueOf(5000));
            final CallFailedRuntimeException ex = applicationHelper.runDisburseExpectingFailure(loanId, disburseJson);
            assertEquals(400, ex.getStatus());
            assertNotNull(ex.getDeveloperMessage());
            assertTrue(ex.getDeveloperMessage().contains("before.approval") || ex.getDeveloperMessage().contains("actualDisbursementDate"));
        });
    }

    @Test
    public void testDisburseWithActualDateBeforeSubmittedDate() {
        final Long productId = createProduct();

        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        runAtBusinessDate(currentDate, () -> {
            final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                    .withClientId(createdClientId) //
                    .withProductId(productId) //
                    .withPrincipal(BigDecimal.valueOf(5000)) //
                    .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                    .withSubmittedOnDate(currentDate) //
                    .buildSubmitRequest());

            applicationHelper.approveById(loanId,
                    WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(currentDate, BigDecimal.valueOf(5000), null));

            final LocalDate actualDateBeforeSubmitted = currentDate.minusDays(1);
            final var disburseJson = WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(actualDateBeforeSubmitted,
                    BigDecimal.valueOf(5000));
            final CallFailedRuntimeException ex = applicationHelper.runDisburseExpectingFailure(loanId, disburseJson);
            assertEquals(400, ex.getStatus());
            assertNotNull(ex.getDeveloperMessage());
            assertTrue(ex.getDeveloperMessage().contains("submitted") || ex.getDeveloperMessage().contains("actualDisbursementDate"),
                    "Expected message about actual date before submitted: " + ex.getDeveloperMessage());
        });
    }

    @Test
    public void testDisburseWithNoteExceedingLength() {
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        runAtBusinessDate(currentDate, () -> {
            final Long productId = createProduct();

            final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                    .withClientId(createdClientId) //
                    .withProductId(productId) //
                    .withPrincipal(BigDecimal.valueOf(5000)) //
                    .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                    .buildSubmitRequest());

            applicationHelper.approveById(loanId,
                    WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(currentDate, BigDecimal.valueOf(5000), null));

            final String longNote = "a".repeat(1001);
            final var disburseJson = WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(currentDate, BigDecimal.valueOf(5000),
                    null, longNote, null, null, null, null, null, null, null);
            final CallFailedRuntimeException ex = applicationHelper.runDisburseExpectingFailure(loanId, disburseJson);
            assertEquals(400, ex.getStatus());
            assertNotNull(ex.getDeveloperMessage());
            assertTrue(ex.getDeveloperMessage().contains("note") || ex.getDeveloperMessage().toLowerCase().contains("length"),
                    "Expected message about note length: " + ex.getDeveloperMessage());
        });
    }

    @Test
    public void testDisburseWithDiscountExceedingCreated() {
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        runAtBusinessDate(currentDate, () -> {
            final Long productId = createProductWithDiscountAllowed();

            final BigDecimal approvedPrincipal = BigDecimal.valueOf(5000);
            final BigDecimal approvedDiscount = BigDecimal.valueOf(20);
            final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                    .withClientId(createdClientId) //
                    .withProductId(productId) //
                    .withPrincipal(approvedPrincipal) //
                    .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                    .buildSubmitRequest());

            applicationHelper.approveById(loanId,
                    WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(currentDate, approvedPrincipal, approvedDiscount));

            final BigDecimal discountAmountExceeding = BigDecimal.valueOf(25);
            final var disburseJson = WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(currentDate, approvedPrincipal,
                    discountAmountExceeding, null, null, null, null, null, null, null);
            final CallFailedRuntimeException ex = applicationHelper.runDisburseExpectingFailure(loanId, disburseJson);
            assertEquals(400, ex.getStatus());
            assertNotNull(ex.getDeveloperMessage());
            assertTrue(ex.getDeveloperMessage().contains("discount") && ex.getDeveloperMessage().contains("exceed"));
        });
    }

    @Test
    public void testDisburseWithDiscountFailsWhenProductDisallowsDiscountOverride() {
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        runAtBusinessDate(currentDate, () -> {
            final Long productId = createProduct();

            final BigDecimal approvedPrincipal = BigDecimal.valueOf(5000);
            final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                    .withClientId(createdClientId) //
                    .withProductId(productId) //
                    .withPrincipal(approvedPrincipal) //
                    .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                    .buildSubmitRequest());

            applicationHelper.approveById(loanId,
                    WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(currentDate, approvedPrincipal, null));

            final var disburseJson = WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(currentDate, approvedPrincipal,
                    BigDecimal.valueOf(10), null, null, null, null, null, null, null);
            final CallFailedRuntimeException ex = applicationHelper.runDisburseExpectingFailure(loanId, disburseJson);
            assertEquals(400, ex.getStatus());
            assertNotNull(ex.getDeveloperMessage());
            assertTrue(ex.getDeveloperMessage().contains("override.not.allowed.by.product"));
        });
    }

    @Test
    public void testDisburseWithDuplicateTransactionExternalId() {
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        runAtBusinessDate(currentDate, () -> {
            final Long productId = createProduct();

            final String sharedExternalId = "wcl-txn-ext-" + UUID.randomUUID();

            final Long loanId1 = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                    .withClientId(createdClientId) //
                    .withProductId(productId) //
                    .withPrincipal(BigDecimal.valueOf(5000)) //
                    .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                    .buildSubmitRequest());
            final Long loanId2 = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                    .withClientId(createdClientId) //
                    .withProductId(productId) //
                    .withPrincipal(BigDecimal.valueOf(3000)) //
                    .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                    .buildSubmitRequest());

            applicationHelper.approveById(loanId1,
                    WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(currentDate, BigDecimal.valueOf(5000), null));
            applicationHelper.disburseById(loanId1, WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(currentDate,
                    BigDecimal.valueOf(5000), null, null, null, null, null, null, null, null, sharedExternalId));

            applicationHelper.approveById(loanId2,
                    WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(currentDate, BigDecimal.valueOf(3000), null));

            final var disburseJson2 = WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(currentDate, BigDecimal.valueOf(3000),
                    null, null, null, null, null, null, null, null, sharedExternalId);
            final CallFailedRuntimeException ex = applicationHelper.runDisburseExpectingFailure(loanId2, disburseJson2);
            assertEquals(400, ex.getStatus());
            assertTrue(ex.getDeveloperMessage().contains("externalId") && ex.getDeveloperMessage().toLowerCase().contains("already"),
                    "Expected duplicate transaction externalId error: " + ex.getDeveloperMessage());
        });
    }

    @Test
    public void testDisburseWhenLoanNotApproved() {
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        runAtBusinessDate(currentDate, () -> {
            final Long productId = createProduct();

            final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                    .withClientId(createdClientId) //
                    .withProductId(productId) //
                    .withPrincipal(BigDecimal.valueOf(5000)) //
                    .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                    .buildSubmitRequest());

            final var disburseJson = WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(currentDate, BigDecimal.valueOf(5000));
            final CallFailedRuntimeException ex = applicationHelper.runDisburseExpectingFailure(loanId, disburseJson);
            assertEquals(400, ex.getStatus());
            assertNotNull(ex.getDeveloperMessage());
            assertTrue(ex.getDeveloperMessage().contains("Transition") || ex.getDeveloperMessage().contains("not allowed")
                    || ex.getDeveloperMessage().contains("status"));
        });
    }

    @Test
    public void testDisburseNonExistentLoan() {
        final long nonExistentLoanId = 999_999_999L;
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        runAtBusinessDate(currentDate, () -> {
            final var disburseJson = WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(currentDate, BigDecimal.valueOf(5000));
            final CallFailedRuntimeException ex = assertThrows(CallFailedRuntimeException.class,
                    () -> applicationHelper.disburseById(nonExistentLoanId, disburseJson));
            assertEquals(404, ex.getStatus());
            assertNotNull(ex.getDeveloperMessage());
        });
    }

    @Test
    public void testUndoDisbursalWhenLoanNotDisbursed() {
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        runAtBusinessDate(currentDate, () -> {
            final Long productId = createProduct();

            final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                    .withClientId(createdClientId) //
                    .withProductId(productId) //
                    .withPrincipal(BigDecimal.valueOf(5000)) //
                    .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                    .buildSubmitRequest());

            applicationHelper.approveById(loanId,
                    WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(currentDate, BigDecimal.valueOf(5000), null));

            final CallFailedRuntimeException ex = applicationHelper.runUndoDisbursalExpectingFailure(loanId,
                    WorkingCapitalLoanDisbursementTestBuilder.buildUndoDisburseRequest());
            assertEquals(400, ex.getStatus());
            assertNotNull(ex.getDeveloperMessage());
            assertTrue(ex.getDeveloperMessage().contains("Transition") || ex.getDeveloperMessage().contains("not allowed")
                    || ex.getDeveloperMessage().contains("status"));
        });
    }

    @Test
    public void testUndoDisbursalNonExistentLoan() {
        final long nonExistentLoanId = 999_999_999L;
        final CallFailedRuntimeException ex = assertThrows(CallFailedRuntimeException.class, () -> applicationHelper
                .undoDisbursalById(nonExistentLoanId, WorkingCapitalLoanDisbursementTestBuilder.buildUndoDisburseRequest()));
        assertEquals(404, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
    }

    @Test
    public void testUndoDisbursalWithNoteExceedingLength() {
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        runAtBusinessDate(currentDate, () -> {
            final Long productId = createProduct();

            final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                    .withClientId(createdClientId) //
                    .withProductId(productId) //
                    .withPrincipal(BigDecimal.valueOf(5000)) //
                    .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                    .buildSubmitRequest());

            applicationHelper.approveById(loanId,
                    WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(currentDate, BigDecimal.valueOf(5000), null));
            applicationHelper.disburseById(loanId,
                    WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(currentDate, BigDecimal.valueOf(5000)));

            final String longNote = "a".repeat(1001);
            final CallFailedRuntimeException ex = applicationHelper.runUndoDisbursalExpectingFailure(loanId,
                    WorkingCapitalLoanDisbursementTestBuilder.buildUndoDisburseRequest(longNote));
            assertEquals(400, ex.getStatus());
            assertNotNull(ex.getDeveloperMessage());
            assertTrue(ex.getDeveloperMessage().contains("note") || ex.getDeveloperMessage().toLowerCase().contains("length"),
                    "Expected message about note length: " + ex.getDeveloperMessage());
        });
    }

    @Test
    public void testGetTransactionsListAfterDisburse() {
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        runAtBusinessDate(currentDate, () -> {
            final Long productId = createProduct();

            final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                    .withClientId(createdClientId) //
                    .withProductId(productId) //
                    .withPrincipal(BigDecimal.valueOf(5000)) //
                    .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                    .buildSubmitRequest());

            applicationHelper.approveById(loanId,
                    WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(currentDate, BigDecimal.valueOf(5000), null));
            applicationHelper.disburseById(loanId,
                    WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(currentDate, BigDecimal.valueOf(5000)));

            final GetWorkingCapitalLoanTransactionsResponse page = applicationHelper.retrieveTransactionsByLoanId(loanId);
            assertNotNull(page);
            assertNotNull(page.getContent(), "Response should have content array");
            assertEquals(1, page.getContent().size(), "After one disburse there should be one transaction");
            assertEquals(1L, page.getTotalElements());
            final GetWorkingCapitalLoanTransactionIdResponse txn = page.getContent().getFirst();
            assertNotNull(txn.getId());
            assertNotNull(txn.getType());
            assertEquals("loanTransactionType.disbursement", txn.getType().getCode());
            assertEqualBigDecimal(BigDecimal.valueOf(5000), txn.getTransactionAmount());
            assertEqualBigDecimal(BigDecimal.valueOf(5000), txn.getPrincipalPortion());
            assertEqualBigDecimal(BigDecimal.ZERO, txn.getFeeChargesPortion());
            assertEqualBigDecimal(BigDecimal.ZERO, txn.getPenaltyChargesPortion());
            assertFalse(Boolean.TRUE.equals(txn.getReversed()));
        });
    }

    @Test
    public void testGetTransactionByIdAfterDisburse() {
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        runAtBusinessDate(currentDate, () -> {
            final Long productId = createProduct();

            final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                    .withClientId(createdClientId) //
                    .withProductId(productId) //
                    .withPrincipal(BigDecimal.valueOf(6000)) //
                    .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                    .buildSubmitRequest());

            applicationHelper.approveById(loanId,
                    WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(currentDate, BigDecimal.valueOf(6000), null));
            applicationHelper.disburseById(loanId,
                    WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(currentDate, BigDecimal.valueOf(6000)));

            final GetWorkingCapitalLoanTransactionsResponse list = applicationHelper.retrieveTransactionsByLoanId(loanId);
            assert list.getContent() != null;
            assertEquals(1, list.getContent().size());
            final long transactionId = list.getContent().getFirst().getId();

            final GetWorkingCapitalLoanTransactionIdResponse txn = applicationHelper.retrieveTransactionByLoanIdAndTransactionId(loanId,
                    transactionId);
            assertEquals(transactionId, txn.getId());
            assertEqualBigDecimal(BigDecimal.valueOf(6000), txn.getTransactionAmount());
            assertEqualBigDecimal(BigDecimal.valueOf(6000), txn.getPrincipalPortion());
            assertNotNull(txn.getTransactionDate());
            assertNotNull(txn.getReversed());
            assertNotNull(txn.getType(), "GET transaction should include type");
            assertEquals("loanTransactionType.disbursement", txn.getType().getCode());
            assertNotNull(txn.getSubmittedOnDate(), "GET transaction should include submittedOnDate");
            assertEqualBigDecimal(BigDecimal.ZERO, txn.getFeeChargesPortion());
            assertEqualBigDecimal(BigDecimal.ZERO, txn.getPenaltyChargesPortion());
        });

    }

    @Test
    public void testGetTransactionsListEmptyWhenNotDisbursed() {
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        runAtBusinessDate(currentDate, () -> {
            final Long productId = createProduct();

            final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                    .withClientId(createdClientId) //
                    .withProductId(productId) //
                    .withPrincipal(BigDecimal.valueOf(5000)) //
                    .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                    .buildSubmitRequest());

            applicationHelper.approveById(loanId,
                    WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(currentDate, BigDecimal.valueOf(5000), null));

            final GetWorkingCapitalLoanTransactionsResponse page = applicationHelper.retrieveTransactionsByLoanId(loanId);
            assertNotNull(page.getContent());
            assertTrue(page.getContent().isEmpty(), "Before disburse transactions list should be empty");
        });

    }

    @Test
    public void testGetTransactionByNonExistentIdReturns404() {
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        runAtBusinessDate(currentDate, () -> {
            final Long productId = createProduct();

            final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                    .withClientId(createdClientId) //
                    .withProductId(productId) //
                    .withPrincipal(BigDecimal.valueOf(5000)) //
                    .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                    .buildSubmitRequest());

            applicationHelper.approveById(loanId,
                    WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(currentDate, BigDecimal.valueOf(5000), null));
            applicationHelper.disburseById(loanId,
                    WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(currentDate, BigDecimal.valueOf(5000)));

            final long nonExistentTransactionId = 999_999L;
            final CallFailedRuntimeException ex = applicationHelper.runRetrieveTransactionByLoanIdAndTransactionIdExpectingFailure(loanId,
                    nonExistentTransactionId);
            assertEquals(404, ex.getStatus());
        });
    }

    @Test
    public void testGetTransactionsByLoanExternalId() {
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        runAtBusinessDate(currentDate, () -> {
            final Long productId = createProduct();

            final String loanExternalId = "wcl-loan-ext-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                    .withClientId(createdClientId) //
                    .withProductId(productId) //
                    .withPrincipal(BigDecimal.valueOf(5000)) //
                    .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                    .withExternalId(loanExternalId) //
                    .buildSubmitRequest());

            applicationHelper.approveById(loanId,
                    WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(currentDate, BigDecimal.valueOf(5000), null));
            applicationHelper.disburseById(loanId,
                    WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(currentDate, BigDecimal.valueOf(5000)));

            final GetWorkingCapitalLoanTransactionsResponse page = applicationHelper.retrieveTransactionsByLoanExternalId(loanExternalId);
            assertNotNull(page);
            assertNotNull(page.getContent());
            assertEquals(1, page.getContent().size());
            final GetWorkingCapitalLoanTransactionIdResponse txn = page.getContent().getFirst();
            assertEqualBigDecimal(BigDecimal.valueOf(5000), txn.getTransactionAmount());
            assertEqualBigDecimal(BigDecimal.valueOf(5000), txn.getPrincipalPortion());
        });
    }

    @Test
    public void testGetTransactionByLoanIdAndTransactionExternalId() {
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        runAtBusinessDate(currentDate, () -> {
            final Long productId = createProduct();

            final String txnExternalId = "wcl-txn-ext-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                    .withClientId(createdClientId) //
                    .withProductId(productId) //
                    .withPrincipal(BigDecimal.valueOf(7000)) //
                    .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                    .buildSubmitRequest());

            applicationHelper.approveById(loanId,
                    WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(currentDate, BigDecimal.valueOf(7000), null));
            applicationHelper.disburseById(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(currentDate,
                    BigDecimal.valueOf(7000), null, null, null, null, null, null, null, null, txnExternalId));

            final GetWorkingCapitalLoanTransactionIdResponse txn = applicationHelper
                    .retrieveTransactionByLoanIdAndTransactionExternalId(loanId, txnExternalId);
            assertEqualBigDecimal(BigDecimal.valueOf(7000), txn.getTransactionAmount());
            assertEqualBigDecimal(BigDecimal.valueOf(7000), txn.getPrincipalPortion());
            assertEquals(txnExternalId, txn.getExternalId());
        });
    }

    @Test
    public void testStateTransitionByLoanExternalId_ApproveAndDisburse() {
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        runAtBusinessDate(currentDate, () -> {
            final Long productId = createProduct();

            final String loanExternalId = "wcl-ext-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                    .withClientId(createdClientId) //
                    .withProductId(productId) //
                    .withPrincipal(BigDecimal.valueOf(5000)) //
                    .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                    .withExternalId(loanExternalId) //
                    .buildSubmitRequest());

            final var approveJson = WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(currentDate, BigDecimal.valueOf(5000),
                    null);
            applicationHelper.approveByExternalId(loanExternalId, approveJson);

            final var disburseJson = WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(currentDate, BigDecimal.valueOf(5000));
            applicationHelper.disburseByExternalId(loanExternalId, disburseJson);

            final GetWorkingCapitalLoansLoanIdResponse data = applicationHelper.retrieveById(loanId);
            assertStatus(data, "loanStatusType.active");
            assertNotNull(data.getBalance(), "GET loan after disburse should include balance");
            assertEqualBigDecimal(BigDecimal.valueOf(5000), data.getBalance().getPrincipalOutstanding());
        });
    }

    @Test
    public void testGetTransactionByExternalLoanIdAndTransactionId() {
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        runAtBusinessDate(currentDate, () -> {
            final Long productId = createProduct();

            final String loanExternalId = "wcl-lext-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                    .withClientId(createdClientId) //
                    .withProductId(productId) //
                    .withPrincipal(BigDecimal.valueOf(8000)) //
                    .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                    .withExternalId(loanExternalId) //
                    .buildSubmitRequest());

            applicationHelper.approveById(loanId,
                    WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(currentDate, BigDecimal.valueOf(8000), null));
            applicationHelper.disburseById(loanId,
                    WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(currentDate, BigDecimal.valueOf(8000)));

            final long transactionId = applicationHelper.retrieveTransactionsByLoanId(loanId).getContent().getFirst().getId();

            final GetWorkingCapitalLoanTransactionIdResponse txn = applicationHelper
                    .retrieveTransactionByExternalLoanIdAndTransactionId(loanExternalId, transactionId);
            assertEquals(transactionId, txn.getId());
            assertEqualBigDecimal(BigDecimal.valueOf(8000), txn.getTransactionAmount());
        });
    }

    @Test
    public void testGetTransactionByExternalLoanIdAndTransactionExternalId() {
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        runAtBusinessDate(currentDate, () -> {
            final Long productId = createProduct();

            final String loanExternalId = "wcl-lext2-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            final String txnExternalId = "wcl-text-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                    .withClientId(createdClientId) //
                    .withProductId(productId) //
                    .withPrincipal(BigDecimal.valueOf(9000)) //
                    .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                    .withExternalId(loanExternalId) //
                    .buildSubmitRequest());

            applicationHelper.approveById(loanId,
                    WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(currentDate, BigDecimal.valueOf(9000), null));
            applicationHelper.disburseById(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(currentDate,
                    BigDecimal.valueOf(9000), null, null, null, null, null, null, null, null, txnExternalId));

            final GetWorkingCapitalLoanTransactionIdResponse txn = applicationHelper
                    .retrieveTransactionByExternalLoanIdAndTransactionExternalId(loanExternalId, txnExternalId);
            assertEqualBigDecimal(BigDecimal.valueOf(9000), txn.getTransactionAmount());
            assertEquals(txnExternalId, txn.getExternalId());
        });
    }

    @Test
    public void testDisburseWithInvalidPaymentTypeId() {
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        runAtBusinessDate(currentDate, () -> {
            final Long productId = createProduct();

            final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                    .withClientId(createdClientId) //
                    .withProductId(productId) //
                    .withPrincipal(BigDecimal.valueOf(5000)) //
                    .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                    .buildSubmitRequest());

            applicationHelper.approveById(loanId,
                    WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(currentDate, BigDecimal.valueOf(5000), null));

            final var disburseJson = WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(currentDate, BigDecimal.valueOf(5000),
                    null, null, 0, null, null, null, null, null, null);
            final CallFailedRuntimeException ex = applicationHelper.runDisburseExpectingFailure(loanId, disburseJson);
            assertEquals(400, ex.getStatus());
            assertNotNull(ex.getDeveloperMessage());
            assertTrue(ex.getDeveloperMessage().contains("paymentTypeId") || ex.getDeveloperMessage().toLowerCase().contains("payment"),
                    "Expected message about invalid paymentTypeId: " + ex.getDeveloperMessage());
        });
    }

    @Test
    public void testDisburseWithPaymentDetailsStringExceedingLength() {
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        runAtBusinessDate(currentDate, () -> {
            final Long productId = createProduct();

            final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                    .withClientId(createdClientId) //
                    .withProductId(productId) //
                    .withPrincipal(BigDecimal.valueOf(5000)) //
                    .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                    .buildSubmitRequest());

            applicationHelper.approveById(loanId,
                    WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(currentDate, BigDecimal.valueOf(5000), null));

            final String longAccountNumber = "a".repeat(51);
            final var disburseJson = WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(currentDate, BigDecimal.valueOf(5000),
                    null, null, null, longAccountNumber, null, null, null, null, null);
            final CallFailedRuntimeException ex = applicationHelper.runDisburseExpectingFailure(loanId, disburseJson);
            assertEquals(400, ex.getStatus());
            assertNotNull(ex.getDeveloperMessage());
            assertTrue(ex.getDeveloperMessage().contains("accountNumber") || ex.getDeveloperMessage().toLowerCase().contains("length"),
                    "Expected message about accountNumber length: " + ex.getDeveloperMessage());
        });
    }

    @Test
    public void testDisburseGeneratesAmortizationSchedule() {
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        runAtBusinessDate(currentDate, () -> {
            final Long productId = createProductWithDiscountAllowed();
            final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                    .withClientId(createdClientId) //
                    .withProductId(productId) //
                    .withPrincipal(BigDecimal.valueOf(5000)) //
                    .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                    .buildSubmitRequest());

            final BigDecimal disbursementAmount = BigDecimal.valueOf(5000);
            final BigDecimal discountAmount = BigDecimal.valueOf(25);

            applicationHelper.approveById(loanId,
                    WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(currentDate, BigDecimal.valueOf(5000), null));

            applicationHelper.disburseById(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(currentDate,
                    disbursementAmount, discountAmount, null, null, null, null, null, null, null));

            final ProjectedAmortizationScheduleData schedule = retrieveAmortizationScheduleByLoanId(loanId);
            assertDateEquals(currentDate, schedule.getExpectedDisbursementDate());
            assertEqualBigDecimal(disbursementAmount, schedule.getNetDisbursementAmount());
            assertEqualBigDecimal(discountAmount, schedule.getDiscountFeeAmount());
            assertNotNull(schedule.getPayments(), "Schedule should contain payments");
            assertFalse(schedule.getPayments().isEmpty(), "Schedule payments should not be empty after disburse");
        });
    }

    @Test
    public void testUndoDisbursalRegeneratesAmortizationScheduleToExpectedDate() {
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        runAtBusinessDate(currentDate, () -> {
            final Long productId = createProduct();
            final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder() //
                    .withClientId(createdClientId) //
                    .withProductId(productId) //
                    .withPrincipal(BigDecimal.valueOf(5000)) //
                    .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                    .buildSubmitRequest());

            applicationHelper.approveById(loanId,
                    WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(currentDate, BigDecimal.valueOf(5000), null));

            final GetDisbursementDetail firstDetailAfterApprove = applicationHelper.retrieveById(loanId).getDisbursementDetails()
                    .getFirst();
            final LocalDate expectedDateAfterApprove = firstDetailAfterApprove.getExpectedDisbursementDate();

            final ProjectedAmortizationScheduleData scheduleAfterApprove = retrieveAmortizationScheduleByLoanId(loanId);
            assertDateEquals(expectedDateAfterApprove, scheduleAfterApprove.getExpectedDisbursementDate());

            applicationHelper.disburseById(loanId,
                    WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(currentDate, BigDecimal.valueOf(5000)));
            final ProjectedAmortizationScheduleData scheduleAfterDisburse = retrieveAmortizationScheduleByLoanId(loanId);
            assertDateEquals(currentDate, scheduleAfterDisburse.getExpectedDisbursementDate());

            applicationHelper.undoDisbursalById(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildUndoDisburseRequest());

            final ProjectedAmortizationScheduleData scheduleAfterUndo = retrieveAmortizationScheduleByLoanId(loanId);
            assertDateEquals(expectedDateAfterApprove, scheduleAfterUndo.getExpectedDisbursementDate());
            assertNotNull(scheduleAfterUndo.getPayments(), "Schedule should still exist after undo");
            assertFalse(scheduleAfterUndo.getPayments().isEmpty(), "Schedule payments should not be empty after undo");
        });
    }

    private static void runAtBusinessDate(final LocalDate currentDate, final Runnable action) {
        BusinessDateHelper.runAt(currentDate.format(BUSINESS_DATE_FORMATTER), action);
    }

    private static void assertStatus(final GetWorkingCapitalLoansLoanIdResponse data, final String expectedStatusCode) {
        assertNotNull(data.getStatus());
        assertEquals(expectedStatusCode, data.getStatus().getCode());
    }

    private static void assertEqualBigDecimal(final BigDecimal expected, final BigDecimal actual) {
        assertNotNull(actual, "Expected value for field");
        assertEquals(0, expected.compareTo(actual), "Expected " + expected + " but got " + actual);
    }

    private static void assertDateEquals(final LocalDate expected, final LocalDate actual) {
        assertEquals(expected, actual);
    }

    private Long createProduct() {
        final String uniqueName = "WCL Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = Utils.uniqueRandomStringGenerator("", 4);
        final Long productId = productHelper
                .createWorkingCapitalLoanProduct(
                        new WorkingCapitalLoanProductTestBuilder().withName(uniqueName).withShortName(uniqueShortName).build())
                .getResourceId();
        createdProductIds.add(productId);
        return productId;
    }

    private Long createProductWithDiscountAllowed() {
        final String uniqueName = "WCL Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = Utils.uniqueRandomStringGenerator("", 4);
        final Long productId = productHelper.createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder() //
                .withName(uniqueName) //
                .withShortName(uniqueShortName) //
                .withAllowAttributeOverrides(java.util.Map.of("discountDefault", Boolean.TRUE)) //
                .build()) //
                .getResourceId();
        createdProductIds.add(productId);
        return productId;
    }

    private Long createClient() {
        return ClientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
    }

    private ProjectedAmortizationScheduleData retrieveAmortizationScheduleByLoanId(final Long loanId) {
        return applicationHelper.retrieveAmortizationScheduleByLoanIdRaw(loanId);
    }

    private Long submitAndTrack(final PostWorkingCapitalLoansRequest submitJson) {
        final Long loanId = applicationHelper.submit(submitJson);
        createdLoanIds.add(loanId);
        return loanId;
    }
}
