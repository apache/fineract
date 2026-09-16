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

import static org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper.assertEqualBigDecimal;
import static org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper.errorCodesOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.DelinquencyRangeRequest;
import org.apache.fineract.client.models.GetClientsClientIdAccountsResponse;
import org.apache.fineract.client.models.GetClientsWorkingCapitalLoanAccounts;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdTimeline;
import org.apache.fineract.client.models.PostDelinquencyBucketResponse;
import org.apache.fineract.client.models.PostDelinquencyRangeResponse;
import org.apache.fineract.client.models.ProjectedAmortizationScheduleData;
import org.apache.fineract.client.models.WorkingCapitalLoanBreachScheduleData;
import org.apache.fineract.client.models.WorkingCapitalLoanDelinquencyRangeScheduleData;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessDateHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalLoanRequestBuilders;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.products.DelinquencyRangesHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanDelinquencyRangeScheduleHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanbreach.WorkingCapitalBreachHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Locks the API-observable behaviour of every {@code WorkingCapitalLoan} status / disbursement predicate that the
 * entity's derived getters now serve.
 *
 * <p>
 * These are <b>not</b> feature tests: the inline {@code getLoanStatus().isActive()} checks and the five byte-identical
 * {@code disbursementDetails ... anyMatch(Objects::nonNull)} blocks moved onto the entity as {@code isOpen()} and
 * {@code isDisbursed()} without anything new becoming observable through the API. The date accessor deliberately
 * normalizes the previously inconsistent row-selection rules to the earliest non-null actual date; the multi-row case
 * is pinned by focused unit tests because this suite creates only one actual-disbursement row. The assertions below are
 * the API regression contract for the behavior common to the old and new implementations.
 *
 * <p>
 * Because Fineract's {@code DataValidatorBuilder} accumulates all failures of a request before throwing, several of
 * these rejections carry more than one code. The tests assert the <b>exact</b> code list rather than "contains", since
 * a predicate that starts short-circuiting (or stops null-guarding) would show up precisely as a change in which codes
 * accumulate.
 */
public class FeignWorkingCapitalLoanStatusHelpersTest extends FeignIntegrationTest {

    private static final String LOAN_DATE = "02 March 2026";
    private static final String BUSINESS_DATE = "2026-03-02";
    private static final String COB_DATE = "2026-03-10";
    private static final String COB_DATE_2 = "2026-03-11";
    private static final LocalDate DISBURSEMENT_DATE = LocalDate.of(2026, 3, 2);

    private static final BigDecimal PRINCIPAL = BigDecimal.valueOf(10000);
    private static final BigDecimal PERIOD_PAYMENT_RATE = BigDecimal.valueOf(18);

    // The client-accounts summary serialises LocalDate as Fineract's legacy [year, month, day] array
    // (LocalDateAdapter), which Jackson hands back as a List of Integer.
    private static final List<Integer> DISBURSEMENT_DATE_ARRAY = List.of(2026, 3, 2);

    // The isApproved() discount branch. Numbers chosen so every derived schedule figure is exact:
    // dailyPayment = totalPaymentVolume x rate / npvDayCount / 100 = 100000 x 18 / 360 / 100 = 50.00, and
    // originalPaymentNumber = roundUp((netDisbursement + discount) / dailyPayment) = roundUp(9400 / 50) = 188.
    private static final BigDecimal DISCOUNT_PRINCIPAL = new BigDecimal("9000");
    private static final BigDecimal DISCOUNT_PROPOSED = new BigDecimal("1000");
    private static final BigDecimal DISCOUNT_APPROVED = new BigDecimal("400");
    private static final BigDecimal EXPECTED_DAILY_PAYMENT = new BigDecimal("50.00");
    private static final int EXPECTED_PAYMENT_NUMBER = 188;

    // Delinquency bucket: a single 1..30 day range on a 30-day period, no grace, so period 1 opens on the actual
    // disbursement date.
    private static final int DELINQUENCY_PERIOD_DAYS = 30;
    private static final int DELINQUENCY_GRACE_DAYS = 0;

    // Breach configuration: monthly period, flat minimum payment, no grace days, so period 1 also opens on the
    // actual disbursement date.
    private static final int BREACH_FREQUENCY = 1;
    private static final String BREACH_FREQUENCY_TYPE = "MONTHS";
    private static final String BREACH_AMOUNT_CALCULATION_TYPE = "FLAT";
    private static final BigDecimal BREACH_MIN_PAYMENT_AMOUNT = new BigDecimal("30");
    private static final int BREACH_GRACE_DAYS = 0;

    private FeignWorkingCapitalLoanHelper wcLoanHelper;
    private FeignClientHelper clientHelper;
    private FeignBusinessDateHelper businessDateHelper;
    private WorkingCapitalLoanProductHelper productHelper;
    private WorkingCapitalBreachHelper breachHelper;

    private final List<Long> createdLoanIds = new ArrayList<>();

    private Long sharedProductId;
    private Long discountOverridableProductId;

    @BeforeAll
    void setupHelpers() {
        final var feignClient = fineractClient();
        wcLoanHelper = new FeignWorkingCapitalLoanHelper(feignClient);
        clientHelper = new FeignClientHelper(feignClient);
        businessDateHelper = new FeignBusinessDateHelper(feignClient);
        productHelper = new WorkingCapitalLoanProductHelper();
        breachHelper = new WorkingCapitalBreachHelper();
    }

    @AfterAll
    void cleanupEntities() {
        createdLoanIds.forEach(wcLoanHelper::cleanupLoan);
        createdLoanIds.clear();
    }

    // ---------------------------------------------------------------------------------------------------------
    // "is active" on the delinquency-action path (WorkingCapitalLoanDelinquencyActionParseAndValidator)
    // ---------------------------------------------------------------------------------------------------------

    /**
     * A delinquency action on a loan that was only submitted is rejected by the "loan is active" guard. This is the
     * single most-used predicate in the module (11 of the 21 inline status checks) and the one now spelled
     * {@code isOpen()}; the pause path adds no further failures on a submitted loan, so exactly one code comes back.
     */
    @Test
    @DisplayName("delinquency action on a submitted (not approved) WC loan is rejected as not active")
    void delinquencyActionOnSubmittedLoan_rejectedAsNotActive() {
        businessDateHelper.runAt(BUSINESS_DATE, () -> {
            final Long loanId = submitLoan();

            final CallFailedRuntimeException exception = wcLoanHelper.createDelinquencyActionExpectingFailure(loanId,
                    WorkingCapitalLoanRequestBuilders.delinquencyPause(LOAN_DATE, "05 March 2026"));

            assertEquals(400, exception.getStatus(), "accumulating validation rejections are reported as 400 by Fineract");
            assertEquals(List.of("validation.msg.workingCapitalLoanDelinquencyAction.invalid.loan.state"), errorCodesOf(exception),
                    "a submitted WC loan must fail only the active-loan guard");
        });
    }

    // ---------------------------------------------------------------------------------------------------------
    // "is disbursed" on the delinquency-action path (WorkingCapitalLoanDelinquencyActionParseAndValidator)
    // ---------------------------------------------------------------------------------------------------------

    /**
     * A reschedule delinquency action on an approved-but-never-disbursed loan fails the disbursement guard - the
     * duplicated {@code disbursementDetails ... anyMatch(Objects::nonNull)} block now served by {@code isDisbursed()}.
     * The approved loan is also not active and has no schedule yet, so all three guards accumulate; pinning the whole
     * list is what proves the disbursement guard fires rather than being short-circuited away by the earlier ones.
     */
    @Test
    @DisplayName("delinquency reschedule on an approved, never-disbursed WC loan reports the not-disbursed code")
    void delinquencyRescheduleOnApprovedNotDisbursedLoan_rejectedAsNotDisbursed() {
        businessDateHelper.runAt(BUSINESS_DATE, () -> {
            final Long loanId = submitAndApproveLoan();

            final CallFailedRuntimeException exception = wcLoanHelper.createDelinquencyActionExpectingFailure(loanId,
                    WorkingCapitalLoanRequestBuilders.delinquencyReschedule(DELINQUENCY_PERIOD_DAYS, "DAYS"));

            assertEquals(400, exception.getStatus(), "accumulating validation rejections are reported as 400 by Fineract");
            assertEquals(
                    List.of("validation.msg.workingCapitalLoanDelinquencyAction.invalid.loan.state",
                            "validation.msg.workingCapitalLoanDelinquencyAction.loan.not.disbursed",
                            "validation.msg.workingCapitalLoanDelinquencyAction.no.schedule"),
                    errorCodesOf(exception),
                    "an approved, never-disbursed WC loan must fail the active, disbursed and schedule guards, in that order");
        });
    }

    // ---------------------------------------------------------------------------------------------------------
    // "is active" on the breach-action path (WorkingCapitalLoanBreachActionParseAndValidator)
    // ---------------------------------------------------------------------------------------------------------

    /**
     * The breach-action path carries its own copy of the active-loan guard, with a different error code from the
     * delinquency one. Both are spelled {@code isOpen()} now, so both codes have to stay distinct and unchanged.
     */
    @Test
    @DisplayName("breach action on an approved, never-disbursed WC loan is rejected as not active")
    void breachActionOnApprovedNotDisbursedLoan_rejectedAsNotActive() {
        businessDateHelper.runAt(BUSINESS_DATE, () -> {
            final Long loanId = submitAndApproveLoan();

            final CallFailedRuntimeException exception = wcLoanHelper.createBreachActionExpectingFailure(loanId,
                    WorkingCapitalLoanRequestBuilders.breachPause(LOAN_DATE, "05 March 2026"));

            assertEquals(400, exception.getStatus(), "accumulating validation rejections are reported as 400 by Fineract");
            assertEquals(List.of("validation.msg.workingCapitalLoanBreachAction.loan.is.not.active"), errorCodesOf(exception),
                    "an approved WC loan must fail only the breach active-loan guard: the pause path anchors its start-date check on "
                            + "the first actual disbursement date, which a never-disbursed loan does not have");
        });
    }

    // ---------------------------------------------------------------------------------------------------------
    // "is disbursed" on the breach-action path (WorkingCapitalLoanBreachActionParseAndValidator)
    // ---------------------------------------------------------------------------------------------------------

    /**
     * The breach reset path is the second of the five duplicated disbursement checks. Its code differs from the
     * delinquency one only by the resource segment, which is exactly the kind of detail a careless de-duplication
     * flattens.
     */
    @Test
    @DisplayName("breach reset on an approved, never-disbursed WC loan reports the not-disbursed code")
    void breachResetOnApprovedNotDisbursedLoan_rejectedAsNotDisbursed() {
        businessDateHelper.runAt(BUSINESS_DATE, () -> {
            final Long loanId = submitAndApproveLoan();

            final CallFailedRuntimeException exception = wcLoanHelper.createBreachActionExpectingFailure(loanId,
                    WorkingCapitalLoanRequestBuilders.breachReset());

            assertEquals(400, exception.getStatus(), "accumulating validation rejections are reported as 400 by Fineract");
            assertEquals(
                    List.of("validation.msg.workingCapitalLoanBreachAction.loan.is.not.active",
                            "validation.msg.workingCapitalLoanBreachAction.loan.not.disbursed",
                            "validation.msg.workingCapitalLoanBreachAction.no.breach.schedule",
                            "validation.msg.workingCapitalLoanBreachAction.no.breach.evaluation.period"),
                    errorCodesOf(exception),
                    "an approved, never-disbursed WC loan must fail the active, disbursed, schedule and evaluation-period guards, "
                            + "in that order");
        });
    }

    // ---------------------------------------------------------------------------------------------------------
    // "is disbursed" in the COB business steps (DelinquencyRangeScheduleBusinessStep, BreachScheduleBusinessStep)
    // ---------------------------------------------------------------------------------------------------------

    /**
     * COB on a loan that was never disbursed must produce no schedule at all. Both business steps open with the same
     * copy-pasted disbursement check and return early; if the migrated {@code isDisbursed()} were status-based instead
     * of disbursement-details-based, this loan (APPROVED) would still be skipped - but the two rows below (COB after
     * disbursement, and COB after undoing the disbursal) would break, which is why the three run as a set.
     */
    @Test
    @DisplayName("inline WC COB on an approved, never-disbursed loan creates neither schedule")
    void inlineCobOnApprovedNotDisbursedLoan_createsNoSchedules() {
        businessDateHelper.runAt(BUSINESS_DATE, () -> {
            final Long loanId = submitAndApproveLoan();

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", COB_DATE);
            wcLoanHelper.executeInlineWCCOB(loanId);

            assertEquals(0, wcLoanHelper.getDelinquencyRangeSchedule(loanId).size(),
                    "a never-disbursed WC loan must get no delinquency range periods from COB");
            assertEquals(0, wcLoanHelper.getBreachSchedule(loanId).size(), "a never-disbursed WC loan must get no breach periods from COB");
        });
    }

    // ---------------------------------------------------------------------------------------------------------
    // "is disbursed" positive branch + the canonical actual disbursement date
    // ---------------------------------------------------------------------------------------------------------

    /**
     * The same loan, disbursed, gets both schedules on the next COB, and the first delinquency period opens on the
     * actual disbursement date. That start date is derived by
     * {@code WorkingCapitalLoanDelinquencyRangeScheduleServiceImpl}, one of the nine sites collapsed into a single
     * {@code getFirstActualDisbursementDate()}; with a single disbursement detail row, first-in-list-order and
     * earliest-date agree, and this assertion is what pins that they still agree afterwards.
     */
    @Test
    @DisplayName("inline WC COB after disbursement creates both schedules, starting on the actual disbursement date")
    void inlineCobAfterDisbursement_createsSchedulesFromActualDisbursementDate() {
        businessDateHelper.runAt(BUSINESS_DATE, () -> {
            final Long loanId = submitApproveAndDisburseLoan();

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", COB_DATE);
            wcLoanHelper.executeInlineWCCOB(loanId);

            final List<WorkingCapitalLoanDelinquencyRangeScheduleData> delinquencyPeriods = wcLoanHelper
                    .getDelinquencyRangeSchedule(loanId);
            assertEquals(1, delinquencyPeriods.size(),
                    "a 30-day delinquency period disbursed on 02 March and COB'd on 10 March yields exactly the first period");
            assertEquals(DISBURSEMENT_DATE, delinquencyPeriods.getFirst().getFromDate(),
                    "delinquency period 1 must start on the actual disbursement date posted on disburse");

            final List<WorkingCapitalLoanBreachScheduleData> breachPeriods = wcLoanHelper.getBreachSchedule(loanId);
            assertEquals(1, breachPeriods.size(),
                    "a monthly breach period disbursed on 02 March and COB'd on 10 March yields exactly the first period");
            assertEquals(DISBURSEMENT_DATE, breachPeriods.getFirst().getFromDate(),
                    "breach period 1 must start on the actual disbursement date posted on disburse");
        });
    }

    // ---------------------------------------------------------------------------------------------------------
    // undo disbursal clears the actual disbursement date, so "is disbursed" goes false again
    // ---------------------------------------------------------------------------------------------------------

    /**
     * Undoing the disbursal nulls {@code actualDisbursementDate} on the detail row
     * ({@code WorkingCapitalLoanWritePlatformServiceImpl}), which is what makes the disbursement-details-based
     * predicate - and not a status-based one - the correct semantics: the loan returns to APPROVED and COB must stop
     * adding periods again. A status-based {@code isDisbursed()} would give the same answer here by coincidence, but
     * only because APPROVED happens to line up; the discriminating part is that the schedule count stays frozen.
     */
    @Test
    @DisplayName("undo disbursal clears the actual disbursement date and COB stops adding periods")
    void undoDisbursal_clearsActualDisbursementDateAndFreezesSchedule() {
        businessDateHelper.runAt(BUSINESS_DATE, () -> {
            final Long loanId = submitApproveAndDisburseLoan();

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", COB_DATE);
            wcLoanHelper.executeInlineWCCOB(loanId);
            final int delinquencyPeriodsBeforeUndo = wcLoanHelper.getDelinquencyRangeSchedule(loanId).size();
            final int breachPeriodsBeforeUndo = wcLoanHelper.getBreachSchedule(loanId).size();
            assertEquals(1, delinquencyPeriodsBeforeUndo, "COB on the disbursed loan must have created delinquency period 1");
            assertEquals(1, breachPeriodsBeforeUndo, "COB on the disbursed loan must have created breach period 1");

            wcLoanHelper.undoDisbursal(loanId, WorkingCapitalLoanRequestBuilders.undoDisbursal());

            final GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
            assertNotNull(loan.getStatus(), "loan status must be present");
            assertEquals("Approved", loan.getStatus().getValue(), "undo disbursal must return the loan to Approved");
            final GetWorkingCapitalLoansLoanIdTimeline timeline = requireTimeline(loan);
            assertNull(timeline.getActualDisbursementDate(), "undo disbursal must clear the actual disbursement date");

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", COB_DATE_2);
            wcLoanHelper.executeInlineWCCOB(loanId);

            assertEquals(delinquencyPeriodsBeforeUndo, wcLoanHelper.getDelinquencyRangeSchedule(loanId).size(),
                    "COB must add no delinquency period once the actual disbursement date is cleared");
            assertEquals(breachPeriodsBeforeUndo, wcLoanHelper.getBreachSchedule(loanId).size(),
                    "COB must add no breach period once the actual disbursement date is cleared");
        });
    }

    // ---------------------------------------------------------------------------------------------------------
    // "is active" on the mark-as-fraud path (WorkingCapitalLoanFraudWriteServiceImpl)
    // ---------------------------------------------------------------------------------------------------------

    /**
     * Mark-as-fraud guarded on {@code getLoanStatus() != LoanStatus.ACTIVE} rather than
     * {@code getLoanStatus().isActive()}. The two are not interchangeable on a null status, so this row exists to catch
     * a swap of one for the other that would quietly turn a rejection into a 500.
     */
    @Test
    @DisplayName("mark-as-fraud on an approved, never-disbursed WC loan is rejected")
    void markAsFraudOnApprovedNotDisbursedLoan_rejected() {
        businessDateHelper.runAt(BUSINESS_DATE, () -> {
            final Long loanId = submitAndApproveLoan();

            final CallFailedRuntimeException exception = wcLoanHelper.markAsFraudExpectingFailure(loanId,
                    WorkingCapitalLoanRequestBuilders.markAsFraud(true));

            assertEquals(400, exception.getStatus(), "accumulating validation rejections are reported as 400 by Fineract");
            assertEquals(List.of("validation.msg.wc.loan.mark.as.fraud.not.allowed"), errorCodesOf(exception),
                    "marking a non-active WC loan as fraud must keep reporting the fraud-not-allowed code");
        });
    }

    // ---------------------------------------------------------------------------------------------------------
    // "is submitted and pending approval" (WorkingCapitalLoanApplicationDataValidator)
    // ---------------------------------------------------------------------------------------------------------

    /**
     * Modifying an application after approval is the observable face of the submitted-and-pending-approval predicate.
     * It is a domain-rule exception rather than an accumulating validation error, so the body carries a single
     * top-level code.
     */
    @Test
    @DisplayName("modifying a WC loan application after approval is rejected")
    void modifyApplicationAfterApproval_rejected() {
        businessDateHelper.runAt(BUSINESS_DATE, () -> {
            final Long loanId = submitAndApproveLoan();

            final CallFailedRuntimeException exception = wcLoanHelper.modifyApplicationExpectingFailure(loanId,
                    WorkingCapitalLoanRequestBuilders.modifyPrincipal(BigDecimal.valueOf(12000)));

            assertEquals(403, exception.getStatus(), "domain-rule rejections are reported as 403 by Fineract");
            assertEquals(List.of("error.msg.wc.loan.cannot.modify.in.present.state"), errorCodesOf(exception),
                    "modifying a non-submitted WC application must keep reporting the cannot-modify code");
        });
    }

    // ---------------------------------------------------------------------------------------------------------
    // "is closed written off" (WorkingCapitalLoanDataValidator)
    // ---------------------------------------------------------------------------------------------------------

    /**
     * Undo write-off on a loan that was never written off is the only API-observable use of the written-off predicate.
     * Note the guard this replaced was {@code loanStatus == null || !loanStatus.isClosedWrittenOff()} - the null branch
     * is why the helpers must be null-safe rather than a bare delegation.
     */
    @Test
    @DisplayName("undo write-off on an active, never-written-off WC loan is rejected")
    void undoWriteOffOnActiveLoan_rejected() {
        businessDateHelper.runAt(BUSINESS_DATE, () -> {
            final Long loanId = submitApproveAndDisburseLoan();

            final CallFailedRuntimeException exception = wcLoanHelper.undoWriteOffExpectingFailure(loanId,
                    WorkingCapitalLoanRequestBuilders.undoWriteOff());

            assertEquals(400, exception.getStatus(), "accumulating validation rejections are reported as 400 by Fineract");
            assertEquals(List.of("validation.msg.WORKINGCAPITALLOAN.loanStatus.error.msg.wc.loan.is.not.written.off"),
                    errorCodesOf(exception), "undoing a write-off on a non-written-off WC loan must keep reporting the same code");
        });
    }

    // ---------------------------------------------------------------------------------------------------------
    // "is submitted and pending approval" on the delete path (WorkingCapitalLoanApplicationWritePlatformServiceImpl)
    // ---------------------------------------------------------------------------------------------------------

    /**
     * The second call site of the submitted-and-pending-approval predicate. The modify path
     * ({@code WorkingCapitalLoanApplicationDataValidator}) is covered above; the delete path carries its own copy of
     * the same comparison in the write service and raises a different exception, so migrating one and not the other -
     * or flattening both onto the same error - would go unnoticed without this row.
     */
    @Test
    @DisplayName("deleting a WC loan application after approval is rejected")
    void deleteApplicationAfterApproval_rejected() {
        businessDateHelper.runAt(BUSINESS_DATE, () -> {
            final Long loanId = submitAndApproveLoan();

            final CallFailedRuntimeException exception = wcLoanHelper.deleteApplicationExpectingFailure(loanId);

            assertEquals(403, exception.getStatus(), "domain-rule rejections are reported as 403 by Fineract");
            assertEquals(List.of("error.msg.wc.loan.cannot.delete.in.present.state"), errorCodesOf(exception),
                    "deleting a non-submitted WC application must keep reporting the cannot-delete code, distinct from the "
                            + "cannot-modify code of the modify path");
        });
    }

    // ---------------------------------------------------------------------------------------------------------
    // the actual/expected disbursement dates as WorkingCapitalLoanMapper reports them
    // ---------------------------------------------------------------------------------------------------------

    /**
     * Pins the dates {@code GET working-capital-loans/{id}} reports. That endpoint is served by
     * {@code WorkingCapitalLoanApplicationReadPlatformServiceImpl.retrieveOne} through
     * {@code WorkingCapitalLoanMapper}. The mapper gets the timeline date from {@code getFirstActualDisbursementDate()}
     * and independently selects the earliest disbursed detail row for {@code getDisbursedBy()}. This single-row API
     * test guards the mapping; the focused unit test covers the ordering rule. The summary mapping is reachable through
     * {@code retrieveLoanSummaryData(clientId)}, i.e. {@code GET clients/{id}/accounts}, covered by the row below.
     */
    @Test
    @DisplayName("GET working-capital-loans/{id} reports the exact posted expected and actual disbursement dates")
    void loanDetailsTimeline_reportsExactDisbursementDates() {
        businessDateHelper.runAt(BUSINESS_DATE, () -> {
            final Long loanId = submitApproveAndDisburseLoan();

            final GetWorkingCapitalLoansLoanIdTimeline timeline = requireTimeline(wcLoanHelper.getLoanDetails(loanId));
            assertEquals(DISBURSEMENT_DATE, timeline.getExpectedDisbursementDate(),
                    "expected disbursement date must be the date posted on the application");
            assertEquals(DISBURSEMENT_DATE, timeline.getActualDisbursementDate(),
                    "actual disbursement date must be the date posted on disburse");
        });
    }

    // ---------------------------------------------------------------------------------------------------------
    // the summary read-model line, in WorkingCapitalLoanSummaryMapper, reachable only via
    // retrieveLoanSummaryData(clientId) -> GET clients/{id}/accounts
    // ---------------------------------------------------------------------------------------------------------

    /**
     * {@code WorkingCapitalLoanSummaryMapper} also uses {@code getFirstActualDisbursementDate()}. It is <b>not</b> on
     * the {@code GET working-capital-loans/{id}} path the row above exercises: {@code retrieveOne} in
     * {@code WorkingCapitalLoanApplicationReadPlatformServiceImpl} uses {@code WorkingCapitalLoanMapper}, while the
     * summary mapper is reached only from {@code retrieveLoanSummaryData(clientId)}, which
     * {@code AccountDetailsReadPlatformServiceJpaRepositoryImpl} calls for the client-accounts overview. This row is
     * therefore the only guard the migrated line has.
     *
     * <p>
     * The summary timeline is untyped in the generated client ({@code Object}), and the legacy client-accounts endpoint
     * serialises dates through Fineract's {@code LocalDateAdapter}, i.e. as a {@code [year, month, day]} array; the
     * literal below was captured from a run rather than assumed.
     */
    @Test
    @DisplayName("GET clients/{id}/accounts reports the exact actual disbursement date for the WC loan")
    void clientAccountsSummary_reportsExactActualDisbursementDate() {
        businessDateHelper.runAt(BUSINESS_DATE, () -> {
            final Long clientId = clientHelper.createClient(LOAN_DATE);
            final Long loanId = submitApproveAndDisburseLoan(clientId);

            final GetClientsClientIdAccountsResponse accounts = clientHelper.getClientAccounts(clientId);
            final Set<GetClientsWorkingCapitalLoanAccounts> wcAccounts = accounts.getWorkingCapitalLoanAccounts();
            assertNotNull(wcAccounts, "the client-accounts overview must carry a workingCapitalLoanAccounts collection");
            assertEquals(1, wcAccounts.size(), "a client created by this test owns exactly one WC loan");

            final GetClientsWorkingCapitalLoanAccounts account = wcAccounts.iterator().next();
            assertEquals(loanId, account.getId(), "the summarised account must be the loan this test disbursed");

            final Map<String, Object> timeline = timelineOf(account);
            assertEquals(DISBURSEMENT_DATE_ARRAY, timeline.get("actualDisbursementDate"),
                    "WorkingCapitalLoanSummaryMapper must report the date posted on disburse");
            assertEquals(DISBURSEMENT_DATE_ARRAY, timeline.get("expectedDisbursementDate"),
                    "the summary's expected disbursement date must be the date posted on the application");
        });
    }

    // ---------------------------------------------------------------------------------------------------------
    // the isApproved() branch of the discount selection (WorkingCapitalLoanAmortizationScheduleWriteServiceImpl)
    // ---------------------------------------------------------------------------------------------------------

    /**
     * {@code WorkingCapitalLoanAmortizationScheduleWriteServiceImpl.getWorkingCapitalLoanDiscountAmount} picks which
     * discount drives amortization from the loan's state: submitted -&gt; {@code getDiscountProposed()}, approved -&gt;
     * {@code getDiscountApproved()}, open -&gt; {@code getDiscount()}. The approved branch is the only
     * {@code isApproved()} call site in the module, so a mis-wired predicate there would ship a wrong discount into the
     * schedule with everything else still green.
     *
     * <p>
     * The loan is approved with a <b>reduced</b> discount (400 against a proposed 1000) and deliberately left
     * undisbursed, so the stored schedule is exactly the one generated by
     * {@code generateAndSaveAmortizationScheduleOnApproval} while the loan is APPROVED. Each of the three branches
     * yields a different, checkable schedule: approved-&gt;400 gives 188 payments, proposed-&gt;1000 would give 200,
     * and no branch matching (discount 0) would give 180.
     */
    @Test
    @DisplayName("approving with a reduced discount amortizes the approved discount, not the proposed one")
    void approvalWithReducedDiscount_amortizesTheApprovedDiscount() {
        businessDateHelper.runAt(BUSINESS_DATE, () -> {
            final Long clientId = clientHelper.createClient(LOAN_DATE);
            final Long loanId = wcLoanHelper.submitApplication(WorkingCapitalLoanRequestBuilders.submitApplicationWithDiscount(clientId,
                    discountOverridableProduct(), DISCOUNT_PRINCIPAL, PERIOD_PAYMENT_RATE, LOAN_DATE, LOAN_DATE, DISCOUNT_PROPOSED));
            createdLoanIds.add(loanId);

            wcLoanHelper.approve(loanId,
                    WorkingCapitalLoanRequestBuilders.approveWithDiscount(LOAN_DATE, DISCOUNT_PRINCIPAL, LOAN_DATE, DISCOUNT_APPROVED));

            final ProjectedAmortizationScheduleData schedule = wcLoanHelper.getAmortizationSchedule(loanId);
            assertEqualBigDecimal(DISCOUNT_APPROVED, schedule.getDiscountFeeAmount(),
                    "the schedule generated on approval must amortize the approved discount (400), not the proposed one (1000)");
            assertEqualBigDecimal(DISCOUNT_PRINCIPAL, schedule.getNetDisbursementAmount(),
                    "the net disbursement amount must be the approved principal");
            assertEqualBigDecimal(EXPECTED_DAILY_PAYMENT, schedule.getExpectedPaymentAmount(),
                    "daily payment = totalPaymentVolume x rate / npvDayCount / 100 = 100000 x 18 / 360 / 100");
            assertEquals(EXPECTED_PAYMENT_NUMBER, schedule.getOriginalPaymentNumber(),
                    "payment count = roundUp((9000 + 400) / 50) = 188; the proposed discount would give 200 and a zero discount 180");
        });
    }

    // ---------------------------------------------------------------------------------------------------------
    // setup
    // ---------------------------------------------------------------------------------------------------------

    private Long submitLoan() {
        return submitLoan(clientHelper.createClient(LOAN_DATE));
    }

    private Long submitLoan(final Long clientId) {
        final Long productId = sharedProduct();
        final Long loanId = wcLoanHelper.submitApplication(WorkingCapitalLoanRequestBuilders.submitApplication(clientId, productId,
                PRINCIPAL, PERIOD_PAYMENT_RATE, LOAN_DATE, LOAN_DATE));
        createdLoanIds.add(loanId);
        return loanId;
    }

    private Long submitAndApproveLoan() {
        return submitAndApproveLoan(clientHelper.createClient(LOAN_DATE));
    }

    private Long submitAndApproveLoan(final Long clientId) {
        final Long loanId = submitLoan(clientId);
        wcLoanHelper.approve(loanId, WorkingCapitalLoanRequestBuilders.approve(LOAN_DATE, PRINCIPAL, LOAN_DATE));
        return loanId;
    }

    private Long submitApproveAndDisburseLoan() {
        return submitApproveAndDisburseLoan(clientHelper.createClient(LOAN_DATE));
    }

    private Long submitApproveAndDisburseLoan(final Long clientId) {
        final Long loanId = submitAndApproveLoan(clientId);
        wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburse(LOAN_DATE, PRINCIPAL));
        return loanId;
    }

    /**
     * One product for the whole class: it needs both a delinquency bucket and a breach configuration, because the
     * delinquency and breach paths each carry their own copy of the guards under test and both must be reachable on the
     * same loan.
     */
    private Long sharedProduct() {
        if (sharedProductId == null) {
            sharedProductId = createProduct();
        }
        return sharedProductId;
    }

    private Long createProduct() {
        final Long bucketId = createDelinquencyBucket();
        final Long breachId = breachHelper.create(breachHelper.createBreachRequest(Utils.randomStringGenerator("WC_BREACH_", 8),
                BREACH_FREQUENCY, BREACH_FREQUENCY_TYPE, BREACH_AMOUNT_CALCULATION_TYPE, BREACH_MIN_PAYMENT_AMOUNT));
        return productHelper.createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder()
                .withName("WCL Parity " + Utils.uniqueRandomStringGenerator("", 8)).withShortName(Utils.uniqueRandomStringGenerator("", 4))
                .withDelinquencyBucketId(bucketId).withDelinquencyGraceDays(DELINQUENCY_GRACE_DAYS).withBreachId(breachId)
                .withBreachGraceDays(BREACH_GRACE_DAYS).build()).getResourceId();
    }

    private Long createDelinquencyBucket() {
        final PostDelinquencyRangeResponse range = DelinquencyRangesHelper.createRange(new DelinquencyRangeRequest()
                .classification(Utils.randomStringGenerator("DLQ_R_", 10)).minimumAgeDays(1).maximumAgeDays(30).locale("en"));
        assertNotNull(range, "delinquency range creation must succeed");
        final PostDelinquencyBucketResponse bucket = WorkingCapitalLoanDelinquencyRangeScheduleHelper
                .createWorkingCapitalLoanDelinquencyBucket(List.of(range.getResourceId()), DELINQUENCY_PERIOD_DAYS, 0, new BigDecimal("3"),
                        1);
        assertNotNull(bucket, "delinquency bucket creation must succeed");
        return bucket.getResourceId();
    }

    private static GetWorkingCapitalLoansLoanIdTimeline requireTimeline(final GetWorkingCapitalLoansLoanIdResponse loan) {
        final GetWorkingCapitalLoansLoanIdTimeline timeline = loan.getTimeline();
        assertNotNull(timeline, "loan timeline must be present");
        return timeline;
    }

    /**
     * The approved-discount row needs a product whose {@code discountDefault} attribute may be overridden per loan:
     * without it the approval validator rejects a {@code discountAmount} that differs from the proposed one
     * ({@code WorkingCapitalLoanDataValidator}), and the approved-versus-proposed distinction the test turns on could
     * not be set up at all. Kept separate from {@link #sharedProduct()} so the other rows run against an unchanged
     * product.
     */
    private Long discountOverridableProduct() {
        if (discountOverridableProductId == null) {
            discountOverridableProductId = productHelper.createWorkingCapitalLoanProduct(
                    new WorkingCapitalLoanProductTestBuilder().withName("WCL Parity Disc " + Utils.uniqueRandomStringGenerator("", 8))
                            .withShortName(Utils.uniqueRandomStringGenerator("", 4))
                            .withAllowAttributeOverrides(Map.of("discountDefault", Boolean.TRUE)).build())
                    .getResourceId();
        }
        return discountOverridableProductId;
    }

    /**
     * The client-accounts summary carries its timeline as an untyped object in the generated client, so it arrives as a
     * plain map rather than a typed timeline model.
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> timelineOf(final GetClientsWorkingCapitalLoanAccounts account) {
        final Object timeline = account.getTimeline();
        assertNotNull(timeline, "the WC account summary must carry a timeline");
        return assertInstanceOf(Map.class, timeline, "the summary timeline must deserialise as a JSON object");
    }
}
