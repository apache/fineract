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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetWorkingCapitalLoanTransactionIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.PostWorkingCapitalLoansLoanIdRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansRequest;
import org.apache.fineract.client.models.ProjectedAmortizationScheduleData;
import org.apache.fineract.client.models.ProjectedAmortizationSchedulePaymentData;
import org.apache.fineract.infrastructure.event.external.data.ExternalEventResponse;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignExternalEventHelper;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanDisbursementTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class WorkingCapitalLoanRepaymentTest {

    private static final String WC_REPAYMENT_TXN_EVENT = "WorkingCapitalLoanRepaymentTransactionBusinessEvent";
    private static final PostWorkingCapitalLoansLoanIdRequest CLEANUP_EMPTY_COMMAND_REQUEST = WorkingCapitalLoanApplicationTestBuilder
            .buildUndoApproveRequest();

    private final WorkingCapitalLoanHelper loanHelper = new WorkingCapitalLoanHelper();
    private final WorkingCapitalLoanProductHelper productHelper = new WorkingCapitalLoanProductHelper();
    private final FeignExternalEventHelper externalEventHelper = new FeignExternalEventHelper(
            FineractFeignClientHelper.getFineractFeignClient());
    private final List<Long> createdLoanIds = new ArrayList<>();
    private final List<Long> createdProductIds = new ArrayList<>();
    private final Long createdClientId = createClient();

    @AfterEach
    void cleanupEntities() {
        for (final Long loanId : createdLoanIds) {
            if (loanId == null) {
                continue;
            }
            try {
                loanHelper.undoDisbursalById(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildUndoDisburseRequest());
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup
            }
            try {
                loanHelper.undoApprovalById(loanId, CLEANUP_EMPTY_COMMAND_REQUEST);
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup
            }
            try {
                loanHelper.deleteById(loanId);
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup
            }
        }
        createdLoanIds.clear();
        for (final Long productId : createdProductIds) {
            if (productId == null) {
                continue;
            }
            try {
                productHelper.deleteWorkingCapitalLoanProductById(productId);
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup
            }
        }
        createdProductIds.clear();
    }

    @Test
    public void testRepaymentUpdatesTransactionAllocationBalanceAndStatus() {
        final Long productId = createProductWithDiscountAllowed();
        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder().withClientId(createdClientId)
                .withProductId(productId).withPrincipal(BigDecimal.valueOf(5000))
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT)
                .withTotalPaymentVolume(BigDecimal.valueOf(100000)).withDiscount(BigDecimal.valueOf(100)).buildSubmitRequest());
        final LocalDate approvedOnDate = Utils.getLocalDateOfTenant();
        loanHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(approvedOnDate,
                BigDecimal.valueOf(5000), BigDecimal.valueOf(100)));
        final LocalDate disbursementDate = Utils.getLocalDateOfTenant();
        loanHelper.disburseById(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(disbursementDate,
                BigDecimal.valueOf(5000), BigDecimal.valueOf(100), null, null, null, null, null, null, null));
        final LocalDate repaymentDate = disbursementDate.plusDays(1);
        BusinessDateHelper.runAt(repaymentDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                () -> loanHelper.makeRepaymentByLoanId(loanId, WorkingCapitalLoanDisbursementTestBuilder
                        .buildRepaymentRequest(repaymentDate, BigDecimal.valueOf(5200), null, "repayment", 1, "repayment-account")));

        final GetWorkingCapitalLoansLoanIdResponse loanData = loanHelper.retrieveById(loanId);
        assertStatus(loanData, "loanStatusType.overpaid");
        assert loanData.getBalance() != null;
        assertEqualBigDecimal(BigDecimal.ZERO, loanData.getBalance().getPrincipalOutstanding());
        assertEqualBigDecimal(BigDecimal.valueOf(100), loanData.getBalance().getOverpaymentAmount());
        // Overpaying settles the loan, and settling it recognizes the whole discount fee - the borrower has paid all of
        // it - so alongside the disbursement, the discount fee and the repayment there is a closing amortization.
        final List<GetWorkingCapitalLoanTransactionIdResponse> transactions = Objects
                .requireNonNull(loanHelper.retrieveTransactionsByLoanId(loanId).getContent());
        assertEquals(4, transactions.size(), () -> "unexpected transactions: " + transactions.stream()
                .map(txn -> (txn.getType() == null ? "?" : txn.getType().getCode()) + ' ' + txn.getTransactionAmount()).toList());
        final GetWorkingCapitalLoanTransactionIdResponse amortization = transactions.stream()
                .filter(txn -> txn.getType() != null && "loanTransactionType.discountFeeAmortization".equals(txn.getType().getCode()))
                .findFirst().orElseThrow(() -> new AssertionError("a settled loan must recognize its discount fee"));
        assertEqualBigDecimal(BigDecimal.valueOf(100), amortization.getTransactionAmount());
        assertEqualBigDecimal(BigDecimal.valueOf(100), loanData.getBalance().getRealizedIncomeFromDiscountFee());
        assertEqualBigDecimal(BigDecimal.ZERO, loanData.getBalance().getUnrealizedIncomeFromDiscountFee());
    }

    @Test
    public void testRepaymentRaisesExternalBusinessEvent() {
        externalEventHelper.enableBusinessEvent(WC_REPAYMENT_TXN_EVENT);
        final Long productId = createProduct();
        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder().withClientId(createdClientId)
                .withProductId(productId).withPrincipal(BigDecimal.valueOf(5000))
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT)
                .withTotalPaymentVolume(BigDecimal.valueOf(100000)).buildSubmitRequest());
        final LocalDate approvedOnDate = Utils.getLocalDateOfTenant();
        loanHelper.approveById(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(approvedOnDate, BigDecimal.valueOf(5000), null));
        loanHelper.disburseById(loanId,
                WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(approvedOnDate, BigDecimal.valueOf(5000)));
        final LocalDate repaymentDate = approvedOnDate.plusDays(1);
        BusinessDateHelper.runAt(repaymentDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")), () -> {
            externalEventHelper.deleteAllExternalEvents();
            loanHelper.makeRepaymentByLoanId(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildRepaymentRequest(repaymentDate,
                    BigDecimal.valueOf(100), null, "repayment", 1, "repayment-account"));
        });
        final List<ExternalEventResponse> events = externalEventHelper.getExternalEventsByType(WC_REPAYMENT_TXN_EVENT);
        assertFalse(events.isEmpty());
        final Map<?, ?> payload = events.getFirst().getPayLoad();
        assertEquals(loanId.doubleValue(), ((Number) payload.get("wcLoanId")).doubleValue());
        externalEventHelper.disableBusinessEvent(WC_REPAYMENT_TXN_EVENT);
    }

    @Test
    public void testRepaymentWithMissingTransactionDateFails() {
        final Long loanId = createApprovedAndDisbursedLoan(createProduct(), BigDecimal.valueOf(5000), BigDecimal.valueOf(5000));
        final CallFailedRuntimeException ex = loanHelper.runRepaymentByLoanIdExpectingFailure(loanId,
                WorkingCapitalLoanDisbursementTestBuilder.buildRepaymentRequest(null, BigDecimal.valueOf(100), null, null, null, null));
        assertEquals(400, ex.getStatus());
    }

    @Test
    public void testRepaymentWithMissingTransactionAmountFails() {
        final LocalDate approvedOnDate = Utils.getLocalDateOfTenant();
        final Long loanId = createApprovedAndDisbursedLoan(createProduct(), BigDecimal.valueOf(5000), BigDecimal.valueOf(5000),
                approvedOnDate);
        final CallFailedRuntimeException[] exHolder = new CallFailedRuntimeException[1];
        BusinessDateHelper.runAt(approvedOnDate.plusDays(1).format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                () -> exHolder[0] = loanHelper.runRepaymentByLoanIdExpectingFailure(loanId, WorkingCapitalLoanDisbursementTestBuilder
                        .buildRepaymentRequest(approvedOnDate.plusDays(1), null, null, null, null, null)));
        assertEquals(400, exHolder[0].getStatus());
    }

    @Test
    public void testRepaymentWithFutureDateFails() {
        final Long loanId = createApprovedAndDisbursedLoan(createProduct(), BigDecimal.valueOf(5000), BigDecimal.valueOf(5000));
        final CallFailedRuntimeException ex = loanHelper.runRepaymentByLoanIdExpectingFailure(loanId,
                WorkingCapitalLoanDisbursementTestBuilder.buildRepaymentRequest(Utils.getLocalDateOfTenant().plusDays(30),
                        BigDecimal.valueOf(100), null, null, null, null));
        assertEquals(400, ex.getStatus());
    }

    @Test
    public void testRepaymentWithInvalidClassificationIdFails() {
        final LocalDate approvedOnDate = Utils.getLocalDateOfTenant();
        final Long loanId = createApprovedAndDisbursedLoan(createProduct(), BigDecimal.valueOf(5000), BigDecimal.valueOf(5000),
                approvedOnDate);
        final CallFailedRuntimeException[] exHolder = new CallFailedRuntimeException[1];
        BusinessDateHelper.runAt(approvedOnDate.plusDays(1).format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                () -> exHolder[0] = loanHelper.runRepaymentByLoanIdExpectingFailure(loanId, WorkingCapitalLoanDisbursementTestBuilder
                        .buildRepaymentRequest(approvedOnDate.plusDays(1), BigDecimal.valueOf(100), 0L, null, null, null)));
        assertEquals(400, exHolder[0].getStatus());
    }

    @Test
    public void testRepaymentWhenLoanNotDisbursedFails() {
        final Long productId = createProduct();
        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder().withClientId(createdClientId)
                .withProductId(productId).withPrincipal(BigDecimal.valueOf(5000))
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT).buildSubmitRequest());
        final LocalDate approvedOnDate = Utils.getLocalDateOfTenant();
        loanHelper.approveById(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(approvedOnDate, BigDecimal.valueOf(5000), null));
        final CallFailedRuntimeException ex = loanHelper.runRepaymentByLoanIdExpectingFailure(loanId,
                WorkingCapitalLoanDisbursementTestBuilder.buildRepaymentRequest(approvedOnDate, BigDecimal.valueOf(100), null, null, null,
                        null));
        assertEquals(400, ex.getStatus());
    }

    @Test
    public void testRepaymentWithDateBeforeDisbursementFails() {
        final LocalDate approvedOnDate = Utils.getLocalDateOfTenant();
        final Long productId = createProduct();
        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder().withClientId(createdClientId)
                .withProductId(productId).withPrincipal(BigDecimal.valueOf(5000))
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT).buildSubmitRequest());
        loanHelper.approveById(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(approvedOnDate, BigDecimal.valueOf(5000), null));
        loanHelper.disburseById(loanId,
                WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(approvedOnDate, BigDecimal.valueOf(5000)));
        final CallFailedRuntimeException[] exHolder = new CallFailedRuntimeException[1];
        BusinessDateHelper.runAt(approvedOnDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                () -> exHolder[0] = loanHelper.runRepaymentByLoanIdExpectingFailure(loanId, WorkingCapitalLoanDisbursementTestBuilder
                        .buildRepaymentRequest(approvedOnDate.minusDays(1), BigDecimal.valueOf(100), null, null, null, null)));
        assertEquals(400, exHolder[0].getStatus());
    }

    @Test
    public void testRepaymentWithNegativeAmountFails() {
        final LocalDate approvedOnDate = Utils.getLocalDateOfTenant();
        final Long loanId = createApprovedAndDisbursedLoan(createProduct(), BigDecimal.valueOf(5000), BigDecimal.valueOf(5000),
                approvedOnDate);
        final CallFailedRuntimeException[] exHolder = new CallFailedRuntimeException[1];
        BusinessDateHelper.runAt(approvedOnDate.plusDays(1).format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                () -> exHolder[0] = loanHelper.runRepaymentByLoanIdExpectingFailure(loanId, WorkingCapitalLoanDisbursementTestBuilder
                        .buildRepaymentRequest(approvedOnDate.plusDays(1), BigDecimal.valueOf(-100), null, null, null, null)));
        assertEquals(400, exHolder[0].getStatus());
    }

    @Test
    public void testRepaymentByExternalId() {
        final Long productId = createProduct();
        final String loanExternalId = "wcl-loan-ext-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder().withClientId(createdClientId)
                .withProductId(productId).withPrincipal(BigDecimal.valueOf(5000))
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT)
                .withExternalId(loanExternalId).buildSubmitRequest());
        final LocalDate approvedOnDate = Utils.getLocalDateOfTenant();
        loanHelper.approveById(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(approvedOnDate, BigDecimal.valueOf(5000), null));
        loanHelper.disburseById(loanId,
                WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(approvedOnDate, BigDecimal.valueOf(5000)));
        final LocalDate repaymentDate = approvedOnDate.plusDays(1);
        BusinessDateHelper.runAt(repaymentDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                () -> loanHelper.makeRepaymentByLoanExternalId(loanExternalId, WorkingCapitalLoanDisbursementTestBuilder
                        .buildRepaymentRequest(repaymentDate, BigDecimal.valueOf(100), null, "repayment", 1, "repayment-account")));
        assertEquals(2, Objects.requireNonNull(loanHelper.retrieveTransactionsByLoanId(loanId).getContent()).size());
    }

    @Test
    public void testRepaymentWithDuplicateTransactionExternalIdFails() {
        final Long productId = createProduct();
        final String sharedExternalId = "wcl-repay-ext-" + UUID.randomUUID();
        final LocalDate approvedOnDate = Utils.getLocalDateOfTenant();

        final Long loanId1 = createApprovedAndDisbursedLoan(productId, BigDecimal.valueOf(5000), BigDecimal.valueOf(5000), approvedOnDate);
        final Long loanId2 = createApprovedAndDisbursedLoan(productId, BigDecimal.valueOf(3000), BigDecimal.valueOf(3000), approvedOnDate);
        final LocalDate repaymentDate = approvedOnDate.plusDays(1);

        BusinessDateHelper.runAt(repaymentDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                () -> loanHelper.makeRepaymentByLoanId(loanId1, WorkingCapitalLoanDisbursementTestBuilder
                        .buildTransactionRequest(repaymentDate, BigDecimal.valueOf(100), null, null, null, null, sharedExternalId)));
        final CallFailedRuntimeException ex = loanHelper.runRepaymentByLoanIdExpectingFailure(loanId2,
                WorkingCapitalLoanDisbursementTestBuilder.buildTransactionRequest(repaymentDate, BigDecimal.valueOf(100), null, null, null,
                        null, sharedExternalId));
        assertEquals(400, ex.getStatus());
    }

    @Test
    public void testRepaymentExactPayoffSetsClosedObligationsMet() {
        final LocalDate approvedOnDate = Utils.getLocalDateOfTenant();
        final Long loanId = createApprovedAndDisbursedLoan(createProduct(), BigDecimal.valueOf(5000), BigDecimal.valueOf(5000),
                approvedOnDate);
        final LocalDate repaymentDate = approvedOnDate.plusDays(1);
        BusinessDateHelper.runAt(repaymentDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                () -> loanHelper.makeRepaymentByLoanId(loanId, WorkingCapitalLoanDisbursementTestBuilder
                        .buildRepaymentRequest(repaymentDate, BigDecimal.valueOf(5000), null, "full payoff", 1, "repayment-account")));
        final GetWorkingCapitalLoansLoanIdResponse loanData = loanHelper.retrieveById(loanId);
        assertStatus(loanData, "loanStatusType.closed.obligations.met");
        assert loanData.getBalance() != null;
        assertEqualBigDecimal(BigDecimal.ZERO, loanData.getBalance().getPrincipalOutstanding());
    }

    @Test
    public void testRepaymentAmortizationMatchesProvidedReferenceSchedule() {
        final Long productId = createProductForReferenceSchedule();
        final LocalDate disbursementDate = LocalDate.of(2019, 1, 1);
        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder().withClientId(createdClientId)
                .withProductId(productId).withPrincipal(BigDecimal.valueOf(9000))
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT)
                .withTotalPaymentVolume(BigDecimal.valueOf(100000)).withDiscount(BigDecimal.valueOf(1000))
                .withSubmittedOnDate(disbursementDate).buildSubmitRequest());
        loanHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(disbursementDate,
                BigDecimal.valueOf(9000), BigDecimal.valueOf(1000)));
        loanHelper.disburseById(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(disbursementDate,
                BigDecimal.valueOf(9000), BigDecimal.valueOf(1000), null, null, null, null, null, null, null));

        for (int day = 1; day <= 3; day++) {
            final int repaymentDay = day;
            final LocalDate repaymentDate = disbursementDate.plusDays(day);
            BusinessDateHelper.runAt(repaymentDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                    () -> loanHelper.makeRepaymentByLoanId(loanId,
                            WorkingCapitalLoanDisbursementTestBuilder.buildRepaymentRequest(repaymentDate, BigDecimal.valueOf(50), null,
                                    "reference-schedule-day-" + repaymentDay, 1, "repayment-account")));
        }

        final ProjectedAmortizationScheduleData schedule = loanHelper.retrieveAmortizationScheduleByLoanIdRaw(loanId);
        assertEqualBigDecimal(BigDecimal.valueOf(1000), schedule.getDiscountFeeAmount());
        assertEqualBigDecimal(BigDecimal.valueOf(9000), schedule.getNetDisbursementAmount());
        assertEqualBigDecimal(BigDecimal.valueOf(100000), schedule.getTotalPaymentVolume());
        assertEqualBigDecimal(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT, schedule.getPeriodPaymentRate());
        assertEquals(360, schedule.getNpvDayCount());
        assert schedule.getExpectedPaymentAmount() != null;
        assertTrue(schedule.getExpectedPaymentAmount().compareTo(BigDecimal.ZERO) > 0, "expectedPaymentAmount should be positive");

        final Map<LocalDate, ProjectedAmortizationSchedulePaymentData> paymentByDate = new HashMap<>();
        assert schedule.getPayments() != null;
        for (final ProjectedAmortizationSchedulePaymentData payment : schedule.getPayments()) {
            paymentByDate.put(payment.getPaymentDate(), payment);
        }
        final List<ExpectedScheduleRow> expectedRows = buildExpectedScheduleRows();
        assertEquals(expectedRows.size(), paymentByDate.size(), "Fixture and API schedule row count differ");
        for (ExpectedScheduleRow row : expectedRows) {
            assertScheduleRow(paymentByDate.get(row.paymentDate()), row.expectedPaymentAmount(), row.actualPaymentAmount(),
                    row.expectedBalance(), row.expectedAmortization(), row.actualAmortization(), row.expectedDiscountFeeBalance(),
                    row.actualBalance(), row.actualDiscountFeeBalance(), row.paymentDate().toString());
        }
    }

    private Long createApprovedAndDisbursedLoan(final Long productId, final BigDecimal principal, final BigDecimal disburseAmount) {
        return createApprovedAndDisbursedLoan(productId, principal, disburseAmount, Utils.getLocalDateOfTenant());
    }

    private Long createApprovedAndDisbursedLoan(final Long productId, final BigDecimal principal, final BigDecimal disburseAmount,
            final LocalDate approvedOnDate) {
        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder().withClientId(createdClientId)
                .withProductId(productId).withPrincipal(principal)
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT).buildSubmitRequest());
        loanHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(approvedOnDate, principal, null));
        loanHelper.disburseById(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(approvedOnDate, disburseAmount));
        return loanId;
    }

    private static void assertStatus(final GetWorkingCapitalLoansLoanIdResponse data, final String expectedStatusCode) {
        assertNotNull(data.getStatus());
        assertEquals(expectedStatusCode, data.getStatus().getCode());
    }

    private static void assertEqualBigDecimal(final BigDecimal expected, final BigDecimal actual) {
        assertNotNull(actual);
        assertEquals(0, expected.compareTo(actual));
    }

    private static void assertScheduleRow(final ProjectedAmortizationSchedulePaymentData payment, final String expectedPaymentAmount,
            final String expectedActualPaymentAmount, final String expectedBalance, final String expectedExpectedAmortization,
            final String expectedActualAmortization, final String expectedDiscountFeeBalance, final String expectedActualBalance,
            final String expectedActualDiscountFeeBalance, final String rowDateLabel) {
        assertNotNull(payment, "Expected payment row to exist for date " + rowDateLabel);
        assertAmount(payment, ProjectedAmortizationSchedulePaymentData::getExpectedPaymentAmount, "expectedPaymentAmount",
                expectedPaymentAmount, rowDateLabel);
        assertAmountOrNull(payment, ProjectedAmortizationSchedulePaymentData::getActualPaymentAmount, "actualPaymentAmount",
                expectedActualPaymentAmount, rowDateLabel);
        assertAmount(payment, ProjectedAmortizationSchedulePaymentData::getExpectedBalance, "expectedBalance", expectedBalance,
                rowDateLabel);
        assertAmount(payment, ProjectedAmortizationSchedulePaymentData::getExpectedAmortizationAmount, "expectedAmortizationAmount",
                expectedExpectedAmortization, rowDateLabel);
        assertAmountOrNull(payment, ProjectedAmortizationSchedulePaymentData::getActualAmortizationAmount, "actualAmortizationAmount",
                expectedActualAmortization, rowDateLabel);
        assertAmount(payment, ProjectedAmortizationSchedulePaymentData::getExpectedDiscountFeeBalance, "expectedDiscountFeeBalance",
                expectedDiscountFeeBalance, rowDateLabel);
        assertAmountOrNull(payment, ProjectedAmortizationSchedulePaymentData::getActualBalance, "actualBalance", expectedActualBalance,
                rowDateLabel);
        assertAmountOrNull(payment, ProjectedAmortizationSchedulePaymentData::getActualDiscountFeeBalance, "actualDiscountFeeBalance",
                expectedActualDiscountFeeBalance, rowDateLabel);
    }

    private static void assertAmount(final ProjectedAmortizationSchedulePaymentData payment,
            final java.util.function.Function<ProjectedAmortizationSchedulePaymentData, BigDecimal> fieldGetter, final String field,
            final String expectedValue, final String rowDateLabel) {
        if (expectedValue == null) {
            assertNull(fieldGetter.apply(payment), "Expected null for " + field + " at " + rowDateLabel);
            return;
        }
        final BigDecimal actual = fieldGetter.apply(payment);
        assertNotNull(actual, "Expected non-null for " + field + " at " + rowDateLabel);
        assertAmountValue(actual.setScale(2, RoundingMode.HALF_UP), new BigDecimal(expectedValue).setScale(2, RoundingMode.HALF_UP), field,
                rowDateLabel);
    }

    private static void assertAmountValue(final BigDecimal actual, final BigDecimal expected, final String field,
            final String rowDateLabel) {
        assertEquals(0, expected.compareTo(actual),
                "Mismatch for " + field + " at " + rowDateLabel + ": expected=" + expected + ", actual=" + actual);
    }

    private static void assertAmountOrNull(final ProjectedAmortizationSchedulePaymentData payment,
            final Function<ProjectedAmortizationSchedulePaymentData, BigDecimal> fieldGetter, final String field,
            final String expectedValue, final String rowDateLabel) {
        if (expectedValue == null) {
            assertNull(fieldGetter.apply(payment), "Expected null for " + field + " at " + rowDateLabel);
            return;
        }
        assertAmount(payment, fieldGetter, field, expectedValue, rowDateLabel);
    }

    private static List<ExpectedScheduleRow> buildExpectedScheduleRows() {
        return List.of(expectedRow("1/1/2019", "-9000", null, "9000", null, null, "1000", "9000", "1000"),
                expectedRow("1/2/2019", "50", "50", "8959.61", "9.61", "9.61", "990.39", "8959.61", "990.39"),
                expectedRow("1/3/2019", "50", "50", "8919.18", "9.57", "9.57", "980.82", "8919.18", "980.82"),
                expectedRow("1/4/2019", "50", "50", "8878.70", "9.52", "9.52", "971.30", "8878.70", "971.30"),
                expectedRow("1/5/2019", "50", null, "8838.18", "9.48", null, "961.82", null, null),
                expectedRow("1/6/2019", "50", null, "8797.62", "9.44", null, "952.38", null, null),
                expectedRow("1/7/2019", "50", null, "8757.01", "9.39", null, "942.99", null, null),
                expectedRow("1/8/2019", "50", null, "8716.36", "9.35", null, "933.64", null, null),
                expectedRow("1/9/2019", "50", null, "8675.67", "9.31", null, "924.33", null, null),
                expectedRow("1/10/2019", "50", null, "8634.94", "9.26", null, "915.07", null, null),
                expectedRow("1/11/2019", "50", null, "8594.16", "9.22", null, "905.85", null, null),
                expectedRow("1/12/2019", "50", null, "8553.33", "9.18", null, "896.67", null, null),
                expectedRow("1/13/2019", "50", null, "8512.47", "9.13", null, "887.54", null, null),
                expectedRow("1/14/2019", "50", null, "8471.56", "9.09", null, "878.45", null, null),
                expectedRow("1/15/2019", "50", null, "8430.60", "9.05", null, "869.40", null, null),
                expectedRow("1/16/2019", "50", null, "8389.61", "9.00", null, "860.40", null, null),
                expectedRow("1/17/2019", "50", null, "8348.56", "8.96", null, "851.44", null, null),
                expectedRow("1/18/2019", "50", null, "8307.48", "8.91", null, "842.53", null, null),
                expectedRow("1/19/2019", "50", null, "8266.35", "8.87", null, "833.66", null, null),
                expectedRow("1/20/2019", "50", null, "8225.18", "8.83", null, "824.83", null, null),
                expectedRow("1/21/2019", "50", null, "8183.96", "8.78", null, "816.05", null, null),
                expectedRow("1/22/2019", "50", null, "8142.70", "8.74", null, "807.31", null, null),
                expectedRow("1/23/2019", "50", null, "8101.39", "8.69", null, "798.62", null, null),
                expectedRow("1/24/2019", "50", null, "8060.04", "8.65", null, "789.97", null, null),
                expectedRow("1/25/2019", "50", null, "8018.65", "8.61", null, "781.36", null, null),
                expectedRow("1/26/2019", "50", null, "7977.21", "8.56", null, "772.80", null, null),
                expectedRow("1/27/2019", "50", null, "7935.73", "8.52", null, "764.28", null, null),
                expectedRow("1/28/2019", "50", null, "7894.21", "8.47", null, "755.81", null, null),
                expectedRow("1/29/2019", "50", null, "7852.63", "8.43", null, "747.38", null, null),
                expectedRow("1/30/2019", "50", null, "7811.02", "8.39", null, "738.99", null, null),
                expectedRow("1/31/2019", "50", null, "7769.36", "8.34", null, "730.65", null, null),
                expectedRow("2/1/2019", "50", null, "7727.66", "8.30", null, "722.35", null, null),
                expectedRow("2/2/2019", "50", null, "7685.91", "8.25", null, "714.10", null, null),
                expectedRow("2/3/2019", "50", null, "7644.12", "8.21", null, "705.89", null, null),
                expectedRow("2/4/2019", "50", null, "7602.28", "8.16", null, "697.73", null, null),
                expectedRow("2/5/2019", "50", null, "7560.40", "8.12", null, "689.61", null, null),
                expectedRow("2/6/2019", "50", null, "7518.47", "8.07", null, "681.54", null, null),
                expectedRow("2/7/2019", "50", null, "7476.50", "8.03", null, "673.51", null, null),
                expectedRow("2/8/2019", "50", null, "7434.48", "7.98", null, "665.53", null, null),
                expectedRow("2/9/2019", "50", null, "7392.42", "7.94", null, "657.59", null, null),
                expectedRow("2/10/2019", "50", null, "7350.31", "7.89", null, "649.70", null, null),
                expectedRow("2/11/2019", "50", null, "7308.16", "7.85", null, "641.85", null, null),
                expectedRow("2/12/2019", "50", null, "7265.97", "7.80", null, "634.05", null, null),
                expectedRow("2/13/2019", "50", null, "7223.72", "7.76", null, "626.29", null, null),
                expectedRow("2/14/2019", "50", null, "7181.44", "7.71", null, "618.58", null, null),
                expectedRow("2/15/2019", "50", null, "7139.11", "7.67", null, "610.91", null, null),
                expectedRow("2/16/2019", "50", null, "7096.73", "7.62", null, "603.29", null, null),
                expectedRow("2/17/2019", "50", null, "7054.31", "7.58", null, "595.71", null, null),
                expectedRow("2/18/2019", "50", null, "7011.84", "7.53", null, "588.18", null, null),
                expectedRow("2/19/2019", "50", null, "6969.33", "7.49", null, "580.69", null, null),
                expectedRow("2/20/2019", "50", null, "6926.77", "7.44", null, "573.25", null, null),
                expectedRow("2/21/2019", "50", null, "6884.17", "7.40", null, "565.85", null, null),
                expectedRow("2/22/2019", "50", null, "6841.52", "7.35", null, "558.50", null, null),
                expectedRow("2/23/2019", "50", null, "6798.82", "7.31", null, "551.19", null, null),
                expectedRow("2/24/2019", "50", null, "6756.08", "7.26", null, "543.93", null, null),
                expectedRow("2/25/2019", "50", null, "6713.30", "7.21", null, "536.72", null, null),
                expectedRow("2/26/2019", "50", null, "6670.47", "7.17", null, "529.55", null, null),
                expectedRow("2/27/2019", "50", null, "6627.59", "7.12", null, "522.43", null, null),
                expectedRow("2/28/2019", "50", null, "6584.67", "7.08", null, "515.35", null, null),
                expectedRow("3/1/2019", "50", null, "6541.70", "7.03", null, "508.32", null, null),
                expectedRow("3/2/2019", "50", null, "6498.68", "6.99", null, "501.33", null, null),
                expectedRow("3/3/2019", "50", null, "6455.62", "6.94", null, "494.39", null, null),
                expectedRow("3/4/2019", "50", null, "6412.51", "6.89", null, "487.50", null, null),
                expectedRow("3/5/2019", "50", null, "6369.36", "6.85", null, "480.65", null, null),
                expectedRow("3/6/2019", "50", null, "6326.16", "6.80", null, "473.85", null, null),
                expectedRow("3/7/2019", "50", null, "6282.92", "6.76", null, "467.09", null, null),
                expectedRow("3/8/2019", "50", null, "6239.63", "6.71", null, "460.38", null, null),
                expectedRow("3/9/2019", "50", null, "6196.29", "6.66", null, "453.72", null, null),
                expectedRow("3/10/2019", "50", null, "6152.91", "6.62", null, "447.10", null, null),
                expectedRow("3/11/2019", "50", null, "6109.48", "6.57", null, "440.53", null, null),
                expectedRow("3/12/2019", "50", null, "6066.00", "6.52", null, "434.01", null, null),
                expectedRow("3/13/2019", "50", null, "6022.48", "6.48", null, "427.53", null, null),
                expectedRow("3/14/2019", "50", null, "5978.91", "6.43", null, "421.10", null, null),
                expectedRow("3/15/2019", "50", null, "5935.29", "6.38", null, "414.72", null, null),
                expectedRow("3/16/2019", "50", null, "5891.63", "6.34", null, "408.38", null, null),
                expectedRow("3/17/2019", "50", null, "5847.92", "6.29", null, "402.09", null, null),
                expectedRow("3/18/2019", "50", null, "5804.17", "6.24", null, "395.85", null, null),
                expectedRow("3/19/2019", "50", null, "5760.36", "6.20", null, "389.65", null, null),
                expectedRow("3/20/2019", "50", null, "5716.52", "6.15", null, "383.50", null, null),
                expectedRow("3/21/2019", "50", null, "5672.62", "6.10", null, "377.40", null, null),
                expectedRow("3/22/2019", "50", null, "5628.68", "6.06", null, "371.34", null, null),
                expectedRow("3/23/2019", "50", null, "5584.69", "6.01", null, "365.33", null, null),
                expectedRow("3/24/2019", "50", null, "5540.65", "5.96", null, "359.37", null, null),
                expectedRow("3/25/2019", "50", null, "5496.57", "5.92", null, "353.45", null, null),
                expectedRow("3/26/2019", "50", null, "5452.44", "5.87", null, "347.58", null, null),
                expectedRow("3/27/2019", "50", null, "5408.26", "5.82", null, "341.76", null, null),
                expectedRow("3/28/2019", "50", null, "5364.03", "5.78", null, "335.98", null, null),
                expectedRow("3/29/2019", "50", null, "5319.76", "5.73", null, "330.25", null, null),
                expectedRow("3/30/2019", "50", null, "5275.44", "5.68", null, "324.57", null, null),
                expectedRow("3/31/2019", "50", null, "5231.08", "5.63", null, "318.94", null, null),
                expectedRow("4/1/2019", "50", null, "5186.66", "5.59", null, "313.35", null, null),
                expectedRow("4/2/2019", "50", null, "5142.20", "5.54", null, "307.81", null, null),
                expectedRow("4/3/2019", "50", null, "5097.69", "5.49", null, "302.32", null, null),
                expectedRow("4/4/2019", "50", null, "5053.13", "5.44", null, "296.88", null, null),
                expectedRow("4/5/2019", "50", null, "5008.53", "5.40", null, "291.48", null, null),
                expectedRow("4/6/2019", "50", null, "4963.88", "5.35", null, "286.13", null, null),
                expectedRow("4/7/2019", "50", null, "4919.18", "5.30", null, "280.83", null, null),
                expectedRow("4/8/2019", "50", null, "4874.43", "5.25", null, "275.58", null, null),
                expectedRow("4/9/2019", "50", null, "4829.64", "5.20", null, "270.38", null, null),
                expectedRow("4/10/2019", "50", null, "4784.79", "5.16", null, "265.22", null, null),
                expectedRow("4/11/2019", "50", null, "4739.90", "5.11", null, "260.11", null, null),
                expectedRow("4/12/2019", "50", null, "4694.96", "5.06", null, "255.05", null, null),
                expectedRow("4/13/2019", "50", null, "4649.98", "5.01", null, "250.04", null, null),
                expectedRow("4/14/2019", "50", null, "4604.94", "4.97", null, "245.07", null, null),
                expectedRow("4/15/2019", "50", null, "4559.86", "4.92", null, "240.15", null, null),
                expectedRow("4/16/2019", "50", null, "4514.73", "4.87", null, "235.28", null, null),
                expectedRow("4/17/2019", "50", null, "4469.55", "4.82", null, "230.46", null, null),
                expectedRow("4/18/2019", "50", null, "4424.32", "4.77", null, "225.69", null, null),
                expectedRow("4/19/2019", "50", null, "4379.05", "4.72", null, "220.97", null, null),
                expectedRow("4/20/2019", "50", null, "4333.72", "4.68", null, "216.29", null, null),
                expectedRow("4/21/2019", "50", null, "4288.35", "4.63", null, "211.66", null, null),
                expectedRow("4/22/2019", "50", null, "4242.93", "4.58", null, "207.08", null, null),
                expectedRow("4/23/2019", "50", null, "4197.46", "4.53", null, "202.55", null, null),
                expectedRow("4/24/2019", "50", null, "4151.94", "4.48", null, "198.07", null, null),
                expectedRow("4/25/2019", "50", null, "4106.38", "4.43", null, "193.64", null, null),
                expectedRow("4/26/2019", "50", null, "4060.76", "4.38", null, "189.26", null, null),
                expectedRow("4/27/2019", "50", null, "4015.10", "4.34", null, "184.92", null, null),
                expectedRow("4/28/2019", "50", null, "3969.38", "4.29", null, "180.63", null, null),
                expectedRow("4/29/2019", "50", null, "3923.62", "4.24", null, "176.39", null, null),
                expectedRow("4/30/2019", "50", null, "3877.81", "4.19", null, "172.20", null, null),
                expectedRow("5/1/2019", "50", null, "3831.95", "4.14", null, "168.06", null, null),
                expectedRow("5/2/2019", "50", null, "3786.04", "4.09", null, "163.97", null, null),
                expectedRow("5/3/2019", "50", null, "3740.09", "4.04", null, "159.93", null, null),
                expectedRow("5/4/2019", "50", null, "3694.08", "3.99", null, "155.94", null, null),
                expectedRow("5/5/2019", "50", null, "3648.03", "3.94", null, "152.00", null, null),
                expectedRow("5/6/2019", "50", null, "3601.92", "3.90", null, "148.10", null, null),
                expectedRow("5/7/2019", "50", null, "3555.77", "3.85", null, "144.25", null, null),
                expectedRow("5/8/2019", "50", null, "3509.56", "3.80", null, "140.45", null, null),
                expectedRow("5/9/2019", "50", null, "3463.31", "3.75", null, "136.70", null, null),
                expectedRow("5/10/2019", "50", null, "3417.01", "3.70", null, "133.00", null, null),
                expectedRow("5/11/2019", "50", null, "3370.66", "3.65", null, "129.35", null, null),
                expectedRow("5/12/2019", "50", null, "3324.26", "3.60", null, "125.75", null, null),
                expectedRow("5/13/2019", "50", null, "3277.81", "3.55", null, "122.20", null, null),
                expectedRow("5/14/2019", "50", null, "3231.31", "3.50", null, "118.70", null, null),
                expectedRow("5/15/2019", "50", null, "3184.76", "3.45", null, "115.25", null, null),
                expectedRow("5/16/2019", "50", null, "3138.16", "3.40", null, "111.85", null, null),
                expectedRow("5/17/2019", "50", null, "3091.51", "3.35", null, "108.50", null, null),
                expectedRow("5/18/2019", "50", null, "3044.81", "3.30", null, "105.20", null, null),
                expectedRow("5/19/2019", "50", null, "2998.06", "3.25", null, "101.95", null, null),
                expectedRow("5/20/2019", "50", null, "2951.26", "3.20", null, "98.75", null, null),
                expectedRow("5/21/2019", "50", null, "2904.42", "3.15", null, "95.60", null, null),
                expectedRow("5/22/2019", "50", null, "2857.52", "3.10", null, "92.50", null, null),
                expectedRow("5/23/2019", "50", null, "2810.57", "3.05", null, "89.45", null, null),
                expectedRow("5/24/2019", "50", null, "2763.57", "3.00", null, "86.45", null, null),
                expectedRow("5/25/2019", "50", null, "2716.52", "2.95", null, "83.50", null, null),
                expectedRow("5/26/2019", "50", null, "2669.42", "2.90", null, "80.60", null, null),
                expectedRow("5/27/2019", "50", null, "2622.27", "2.85", null, "77.75", null, null),
                expectedRow("5/28/2019", "50", null, "2575.07", "2.80", null, "74.95", null, null),
                expectedRow("5/29/2019", "50", null, "2527.82", "2.75", null, "72.20", null, null),
                expectedRow("5/30/2019", "50", null, "2480.52", "2.70", null, "69.50", null, null),
                expectedRow("5/31/2019", "50", null, "2433.17", "2.65", null, "66.85", null, null),
                expectedRow("6/1/2019", "50", null, "2385.77", "2.60", null, "64.25", null, null),
                expectedRow("6/2/2019", "50", null, "2338.31", "2.55", null, "61.70", null, null),
                expectedRow("6/3/2019", "50", null, "2290.81", "2.50", null, "59.20", null, null),
                expectedRow("6/4/2019", "50", null, "2243.26", "2.45", null, "56.75", null, null),
                expectedRow("6/5/2019", "50", null, "2195.65", "2.40", null, "54.35", null, null),
                expectedRow("6/6/2019", "50", null, "2148.00", "2.34", null, "52.01", null, null),
                expectedRow("6/7/2019", "50", null, "2100.29", "2.29", null, "49.72", null, null),
                expectedRow("6/8/2019", "50", null, "2052.53", "2.24", null, "47.48", null, null),
                expectedRow("6/9/2019", "50", null, "2004.73", "2.19", null, "45.29", null, null),
                expectedRow("6/10/2019", "50", null, "1956.87", "2.14", null, "43.15", null, null),
                expectedRow("6/11/2019", "50", null, "1908.96", "2.09", null, "41.06", null, null),
                expectedRow("6/12/2019", "50", null, "1860.99", "2.04", null, "39.02", null, null),
                expectedRow("6/13/2019", "50", null, "1812.98", "1.99", null, "37.03", null, null),
                expectedRow("6/14/2019", "50", null, "1764.92", "1.94", null, "35.09", null, null),
                expectedRow("6/15/2019", "50", null, "1716.80", "1.88", null, "33.21", null, null),
                expectedRow("6/16/2019", "50", null, "1668.64", "1.83", null, "31.38", null, null),
                expectedRow("6/17/2019", "50", null, "1620.42", "1.78", null, "29.60", null, null),
                expectedRow("6/18/2019", "50", null, "1572.15", "1.73", null, "27.87", null, null),
                expectedRow("6/19/2019", "50", null, "1523.83", "1.68", null, "26.19", null, null),
                expectedRow("6/20/2019", "50", null, "1475.45", "1.63", null, "24.56", null, null),
                expectedRow("6/21/2019", "50", null, "1427.03", "1.58", null, "22.98", null, null),
                expectedRow("6/22/2019", "50", null, "1378.55", "1.52", null, "21.46", null, null),
                expectedRow("6/23/2019", "50", null, "1330.02", "1.47", null, "19.99", null, null),
                expectedRow("6/24/2019", "50", null, "1281.45", "1.42", null, "18.57", null, null),
                expectedRow("6/25/2019", "50", null, "1232.81", "1.37", null, "17.20", null, null),
                expectedRow("6/26/2019", "50", null, "1184.13", "1.32", null, "15.88", null, null),
                expectedRow("6/27/2019", "50", null, "1135.39", "1.26", null, "14.62", null, null),
                expectedRow("6/28/2019", "50", null, "1086.61", "1.21", null, "13.41", null, null),
                expectedRow("6/29/2019", "50", null, "1037.77", "1.16", null, "12.25", null, null),
                expectedRow("6/30/2019", "50", null, "988.88", "1.11", null, "11.14", null, null),
                expectedRow("7/1/2019", "50", null, "939.93", "1.06", null, "10.08", null, null),
                expectedRow("7/2/2019", "50", null, "890.93", "1.00", null, "9.08", null, null),
                expectedRow("7/3/2019", "50", null, "841.89", "0.95", null, "8.13", null, null),
                expectedRow("7/4/2019", "50", null, "792.79", "0.90", null, "7.23", null, null),
                expectedRow("7/5/2019", "50", null, "743.63", "0.85", null, "6.38", null, null),
                expectedRow("7/6/2019", "50", null, "694.43", "0.79", null, "5.59", null, null),
                expectedRow("7/7/2019", "50", null, "645.17", "0.74", null, "4.85", null, null),
                expectedRow("7/8/2019", "50", null, "595.86", "0.69", null, "4.16", null, null),
                expectedRow("7/9/2019", "50", null, "546.49", "0.64", null, "3.52", null, null),
                expectedRow("7/10/2019", "50", null, "497.08", "0.58", null, "2.94", null, null),
                expectedRow("7/11/2019", "50", null, "447.61", "0.53", null, "2.41", null, null),
                expectedRow("7/12/2019", "50", null, "398.08", "0.48", null, "1.93", null, null),
                expectedRow("7/13/2019", "50", null, "348.51", "0.43", null, "1.50", null, null),
                expectedRow("7/14/2019", "50", null, "298.88", "0.37", null, "1.13", null, null),
                expectedRow("7/15/2019", "50", null, "249.20", "0.32", null, "0.81", null, null),
                expectedRow("7/16/2019", "50", null, "199.47", "0.27", null, "0.54", null, null),
                expectedRow("7/17/2019", "50", null, "149.68", "0.21", null, "0.33", null, null),
                expectedRow("7/18/2019", "50", null, "99.84", "0.16", null, "0.17", null, null),
                expectedRow("7/19/2019", "50", null, "49.95", "0.11", null, "0.06", null, null),
                expectedRow("7/20/2019", "50", null, "0.00", "0.06", null, "0.00", null, null));
    }

    private static ExpectedScheduleRow expectedRow(final String paymentDate, final String expectedPaymentAmount,
            final String actualPaymentAmount, final String expectedBalance, final String expectedAmortization,
            final String actualAmortization, final String expectedDiscountFeeBalance, final String actualBalance,
            final String actualDiscountFeeBalance) {
        return new ExpectedScheduleRow(parseDateString(paymentDate), expectedPaymentAmount, actualPaymentAmount, expectedBalance,
                expectedAmortization, actualAmortization, expectedDiscountFeeBalance, actualBalance, actualDiscountFeeBalance);
    }

    private static LocalDate parseDateString(final String dateText) {
        return LocalDate.parse(dateText, DateTimeFormatter.ofPattern("M/d/yyyy"));
    }

    private record ExpectedScheduleRow(LocalDate paymentDate, String expectedPaymentAmount, String actualPaymentAmount,
            String expectedBalance, String expectedAmortization, String actualAmortization, String expectedDiscountFeeBalance,
            String actualBalance, String actualDiscountFeeBalance) {
    }

    private Long createProduct() {
        final String uniqueName = "WCL Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = UUID.randomUUID().toString().replace("-", "").substring(0, 4);
        final Long productId = productHelper
                .createWorkingCapitalLoanProduct(
                        new WorkingCapitalLoanProductTestBuilder().withName(uniqueName).withShortName(uniqueShortName).build())
                .getResourceId();
        createdProductIds.add(productId);
        return productId;
    }

    private Long createProductWithDiscountAllowed() {
        final String uniqueName = "WCL Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = UUID.randomUUID().toString().replace("-", "").substring(0, 4);
        final Long productId = productHelper.createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder().withName(uniqueName)
                .withShortName(uniqueShortName).withAllowAttributeOverrides(java.util.Map.of("discountDefault", Boolean.TRUE)).build())
                .getResourceId();
        createdProductIds.add(productId);
        return productId;
    }

    private Long createProductForReferenceSchedule() {
        final String uniqueName = "WCL Excel Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = UUID.randomUUID().toString().replace("-", "").substring(0, 4);
        final Long productId = productHelper.createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder().withName(uniqueName)
                .withShortName(uniqueShortName).withRepaymentEvery(1).withRepaymentFrequencyType("DAYS")
                .withAllowAttributeOverrides(java.util.Map.of("discountDefault", Boolean.TRUE)).build()).getResourceId();
        createdProductIds.add(productId);
        return productId;
    }

    private Long createClient() {
        return ClientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
    }

    private Long submitAndTrack(final PostWorkingCapitalLoansRequest submitJson) {
        final Long loanId = loanHelper.submit(submitJson);
        createdLoanIds.add(loanId);
        return loanId;
    }
}
