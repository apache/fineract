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
import static org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper.findCharge;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.DelinquencyRangeRequest;
import org.apache.fineract.client.models.GetBalance;
import org.apache.fineract.client.models.GetWorkingCapitalLoanTransactionIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.PostDelinquencyBucketResponse;
import org.apache.fineract.client.models.PostDelinquencyRangeResponse;
import org.apache.fineract.client.models.PostWorkingCapitalLoanProductsRequest.AccountingRuleEnum;
import org.apache.fineract.client.models.WorkingCapitalLoanChargeData;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignAccountHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessDateHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalLoanRequestBuilders;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
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
 * Adding charges after the maturity date on closed, overpaid, or past-maturity Working-Capital loans.
 *
 * <p>
 * Adding a charge to such a loan must be allowed and, depending on how the charge compares to any existing overpayment,
 * must reopen the loan to ACTIVE, close it (obligations met), or leave it OVERPAID, updating the maturity date and
 * generating delinquency/breach periods when a balance becomes outstanding.
 *
 * <p>
 * The charge-add is wrapped so a server rejection surfaces as an {@link AssertionError} ("adding a charge must be
 * allowed on a closed/overpaid/past-maturity loan"), never as a raw transport exception.
 */
public class FeignWorkingCapitalLoanChargeAfterMaturityTest extends FeignIntegrationTest {

    private static final String STATUS_ACTIVE = "loanStatusType.active";
    private static final String STATUS_CLOSED_OBLIGATIONS_MET = "loanStatusType.closed.obligations.met";
    private static final String STATUS_OVERPAID = "loanStatusType.overpaid";

    private static final BigDecimal PRINCIPAL = BigDecimal.valueOf(1000);
    private static final double CHARGE_AMOUNT = 100;

    // Delinquency configuration so a period can be generated once a charge creates an outstanding balance.
    private static final int DELINQUENCY_FREQUENCY_DAYS = 20;
    private static final int DELINQUENCY_GRACE_DAYS = 3;
    private static final BigDecimal DELINQUENCY_MIN_PAYMENT_PERCENT = new BigDecimal("3");

    // Breach configuration so a breach period can be generated once a charge creates an outstanding balance.
    private static final int BREACH_FREQUENCY = 1;
    private static final String BREACH_FREQUENCY_TYPE = "MONTHS";
    private static final String BREACH_AMOUNT_CALCULATION_TYPE = "FLAT";
    private static final BigDecimal BREACH_MIN_PAYMENT_AMOUNT = new BigDecimal("500");

    private FeignWorkingCapitalLoanHelper wcLoanHelper;
    private FeignClientHelper clientHelper;
    private FeignBusinessDateHelper businessDateHelper;
    private WorkingCapitalLoanProductHelper productHelper;
    private WorkingCapitalBreachHelper breachHelper;

    // GL accounts (only needed for the accrual-with-deferred-revenue product used by the accounting test).
    private Account fundSourceAccount;
    private Account loanPortfolioAccount;
    private Account transfersSuspenseAccount;
    private Account incomeFromDiscountFeeAccount;
    private Account feesReceivableAccount;
    private Account penaltiesReceivableAccount;
    private Account incomeFromFeeAccount;
    private Account incomeFromPenaltyAccount;
    private Account incomeFromRecoveryAccount;
    private Account writeOffAccount;
    private Account overpaymentAccount;
    private Account deferredIncomeAccount;

    private final List<Long> createdLoanIds = new ArrayList<>();
    private final List<Long> createdProductIds = new ArrayList<>();

    @BeforeAll
    void setupHelpers() {
        final var feignClient = fineractClient();
        wcLoanHelper = new FeignWorkingCapitalLoanHelper(feignClient);
        clientHelper = new FeignClientHelper(feignClient);
        businessDateHelper = new FeignBusinessDateHelper(feignClient);
        productHelper = new WorkingCapitalLoanProductHelper();
        breachHelper = new WorkingCapitalBreachHelper();

        final FeignAccountHelper accountHelper = new FeignAccountHelper(feignClient);
        fundSourceAccount = accountHelper.createLiabilityAccount("wcChgMatFundSrc");
        loanPortfolioAccount = accountHelper.createAssetAccount("wcChgMatLoanPort");
        transfersSuspenseAccount = accountHelper.createAssetAccount("wcChgMatXferSusp");
        incomeFromDiscountFeeAccount = accountHelper.createIncomeAccount("wcChgMatIncDisc");
        feesReceivableAccount = accountHelper.createAssetAccount("wcChgMatFeesRcv");
        penaltiesReceivableAccount = accountHelper.createAssetAccount("wcChgMatPenRcv");
        incomeFromFeeAccount = accountHelper.createIncomeAccount("wcChgMatIncFee");
        incomeFromPenaltyAccount = accountHelper.createIncomeAccount("wcChgMatIncPen");
        incomeFromRecoveryAccount = accountHelper.createIncomeAccount("wcChgMatIncRec");
        writeOffAccount = accountHelper.createExpenseAccount("wcChgMatWrtOff");
        overpaymentAccount = accountHelper.createLiabilityAccount("wcChgMatOverpay");
        deferredIncomeAccount = accountHelper.createLiabilityAccount("wcChgMatDefInc");
    }

    @AfterAll
    void cleanupEntities() {
        createdLoanIds.forEach(wcLoanHelper::cleanupLoan);
        createdLoanIds.clear();
        createdProductIds.clear();
    }

    @Test
    @DisplayName("charge on CLOSED loan reopens to ACTIVE with fee outstanding, new maturity, 1 delinquency + 1 breach period")
    void chargeOnClosedLoan_reopensToActive() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final Long clientId = clientHelper.createClient("01 January 2026");
            final Long loanId = createDisburseAndCloseLoan(clientId, "01 January 2026");

            assertStatus(loanId, STATUS_CLOSED_OBLIGATIONS_MET, "loan should be closed before adding the charge");
            final int delinquencyBefore = wcLoanHelper.getDelinquencyRangeSchedule(loanId).size();
            final int breachBefore = wcLoanHelper.getBreachSchedule(loanId).size();

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-06-01");
            addChargeExpectingAllowed(loanId, false, CHARGE_AMOUNT, "01 June 2026", "closed loan");

            final GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
            assertEquals(STATUS_ACTIVE, loan.getStatus().getCode(), "closed loan must reopen to ACTIVE once a charge creates outstanding");
            assertEquals(LocalDate.of(2026, 6, 1), loan.getTimeline().getActualMaturityDate(),
                    "new maturity date must equal the charge due date");

            final GetBalance balance = balanceOf(loan);
            assertEqualBigDecimal(BigDecimal.valueOf(CHARGE_AMOUNT), balance.getFeeOutstanding(), "fee outstanding must equal the charge");
            assertEqualBigDecimal(BigDecimal.valueOf(CHARGE_AMOUNT), balance.getTotalOutstanding(),
                    "total outstanding must equal the charge amount");

            assertEquals(delinquencyBefore + 1, wcLoanHelper.getDelinquencyRangeSchedule(loanId).size(),
                    "exactly one new delinquency period must be created from the charge due date");
            assertEquals(breachBefore + 1, wcLoanHelper.getBreachSchedule(loanId).size(),
                    "exactly one new breach period must be created from the charge due date");
        });
    }

    @Test
    @DisplayName("charge on OVERPAID loan (overpaid 150 > charge 100) stays OVERPAID; overpayment 50, no fee outstanding")
    void chargeOnOverpaidLoan_overpaidGreaterThanCharge_staysOverpaid() {
        businessDateHelper.runAt("2026-02-01", () -> {
            final Long clientId = clientHelper.createClient("01 February 2026");
            final Long loanId = createDisburseAndOverpayLoan(clientId, "01 February 2026", BigDecimal.valueOf(1150));

            assertStatus(loanId, STATUS_OVERPAID, "loan should be overpaid before adding the charge");
            final int delinquencyBefore = wcLoanHelper.getDelinquencyRangeSchedule(loanId).size();
            final int breachBefore = wcLoanHelper.getBreachSchedule(loanId).size();

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-07-01");
            addChargeExpectingAllowed(loanId, false, CHARGE_AMOUNT, "01 July 2026", "overpaid > charge");

            final GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
            assertEquals(STATUS_OVERPAID, loan.getStatus().getCode(), "loan must stay OVERPAID when overpayment exceeds the charge");

            final GetBalance balance = balanceOf(loan);
            assertEqualBigDecimal(BigDecimal.valueOf(50), balance.getOverpaymentAmount(),
                    "overpayment must be reduced by the charge (150 - 100)");
            assertEqualBigDecimal(BigDecimal.ZERO, balance.getFeeOutstanding(),
                    "the charge is fully covered by overpayment: no fee outstanding");

            assertEquals(delinquencyBefore, wcLoanHelper.getDelinquencyRangeSchedule(loanId).size(),
                    "no new delinquency period when nothing becomes outstanding");
            assertEquals(breachBefore, wcLoanHelper.getBreachSchedule(loanId).size(),
                    "no new breach period when nothing becomes outstanding");
        });
    }

    @Test
    @DisplayName("charge on OVERPAID loan (overpaid 100 == charge 100) closes it; overpayment 0, totalOutstanding 0, maturity set")
    void chargeOnOverpaidLoan_overpaidEqualsCharge_closes() {
        businessDateHelper.runAt("2026-03-01", () -> {
            final Long clientId = clientHelper.createClient("01 March 2026");
            final Long loanId = createDisburseAndOverpayLoan(clientId, "01 March 2026", BigDecimal.valueOf(1100));

            assertStatus(loanId, STATUS_OVERPAID, "loan should be overpaid before adding the charge");
            final int delinquencyBefore = wcLoanHelper.getDelinquencyRangeSchedule(loanId).size();
            final int breachBefore = wcLoanHelper.getBreachSchedule(loanId).size();

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-08-01");
            addChargeExpectingAllowed(loanId, false, CHARGE_AMOUNT, "01 August 2026", "overpaid == charge");

            final GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
            assertEquals(STATUS_CLOSED_OBLIGATIONS_MET, loan.getStatus().getCode(),
                    "loan must close (obligations met) when overpayment exactly matches the charge");
            assertEquals(LocalDate.of(2026, 8, 1), loan.getTimeline().getActualMaturityDate(),
                    "maturity date must be set to the charge due date on close");

            final GetBalance balance = balanceOf(loan);
            assertEqualBigDecimal(BigDecimal.ZERO, balance.getOverpaymentAmount(), "overpayment must be fully consumed (100 - 100)");
            assertEqualBigDecimal(BigDecimal.ZERO, balance.getTotalOutstanding(), "nothing outstanding once the charge is covered exactly");

            assertEquals(delinquencyBefore, wcLoanHelper.getDelinquencyRangeSchedule(loanId).size(),
                    "no new delinquency period when the loan closes with zero outstanding");
            assertEquals(breachBefore, wcLoanHelper.getBreachSchedule(loanId).size(),
                    "no new breach period when the loan closes with zero outstanding");
        });
    }

    @Test
    @DisplayName("charge on OVERPAID loan (overpaid 40 < charge 100) reopens to ACTIVE; overpayment 0, fee 60, maturity set")
    void chargeOnOverpaidLoan_overpaidLessThanCharge_reopensToActive() {
        businessDateHelper.runAt("2026-04-01", () -> {
            final Long clientId = clientHelper.createClient("01 April 2026");
            final Long loanId = createDisburseAndOverpayLoan(clientId, "01 April 2026", BigDecimal.valueOf(1040));

            assertStatus(loanId, STATUS_OVERPAID, "loan should be overpaid before adding the charge");
            final int delinquencyBefore = wcLoanHelper.getDelinquencyRangeSchedule(loanId).size();
            final int breachBefore = wcLoanHelper.getBreachSchedule(loanId).size();

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-09-01");
            addChargeExpectingAllowed(loanId, false, CHARGE_AMOUNT, "01 September 2026", "overpaid < charge");

            final GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
            assertEquals(STATUS_ACTIVE, loan.getStatus().getCode(), "loan must reopen to ACTIVE when the charge exceeds the overpayment");
            assertEquals(LocalDate.of(2026, 9, 1), loan.getTimeline().getActualMaturityDate(),
                    "new maturity date must equal the charge due date");

            final GetBalance balance = balanceOf(loan);
            assertEqualBigDecimal(BigDecimal.ZERO, balance.getOverpaymentAmount(), "overpayment must be fully consumed by the charge");
            assertEqualBigDecimal(BigDecimal.valueOf(60), balance.getFeeOutstanding(),
                    "residual fee outstanding must be charge minus overpayment (100 - 40)");

            assertEquals(delinquencyBefore + 1, wcLoanHelper.getDelinquencyRangeSchedule(loanId).size(),
                    "exactly one new delinquency period must be created from the charge due date");
            assertEquals(breachBefore + 1, wcLoanHelper.getBreachSchedule(loanId).size(),
                    "exactly one new breach period must be created from the charge due date");
        });
    }

    @Test
    @DisplayName("charge after maturity on still-ACTIVE loan updates maturity to charge due date and creates delinquency/breach")
    void chargeAfterMaturity_onActiveLoan_updatesMaturityAndSchedules() {
        businessDateHelper.runAt("2026-05-01", () -> {
            final Long clientId = clientHelper.createClient("01 May 2026");
            final Long productId = createDefaultProduct();
            final Long loanId = submitApproveDisburse(clientId, productId, "01 May 2026");

            assertStatus(loanId, STATUS_ACTIVE, "loan should be active (no repayment) before adding the charge");
            final int delinquencyBefore = wcLoanHelper.getDelinquencyRangeSchedule(loanId).size();
            final int breachBefore = wcLoanHelper.getBreachSchedule(loanId).size();

            // Advance well past the original maturity and add a charge dated at the (past-maturity) business date.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-10-01");
            addChargeExpectingAllowed(loanId, false, CHARGE_AMOUNT, "01 October 2026", "active past maturity");

            final GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
            assertEquals(STATUS_ACTIVE, loan.getStatus().getCode(), "past-maturity active loan must stay ACTIVE after the charge");
            assertEquals(LocalDate.of(2026, 10, 1), loan.getTimeline().getActualMaturityDate(),
                    "maturity date must be updated to the charge due date (N+1 term)");

            assertEquals(delinquencyBefore + 1, wcLoanHelper.getDelinquencyRangeSchedule(loanId).size(),
                    "a delinquency period must be created covering the charge due date");
            assertEquals(breachBefore + 1, wcLoanHelper.getBreachSchedule(loanId).size(),
                    "a breach period must be created covering the charge due date");
        });
    }

    @Test
    @DisplayName("charge-add on accrual product reopens the loan and posts no spurious transaction/journal entry at add-time")
    void chargeAdd_onAccrualProduct_postsNoSpuriousJournalEntryAtAddTime() {
        businessDateHelper.runAt("2026-06-01", () -> {
            final Long clientId = clientHelper.createClient("01 June 2026");
            final Long productId = createAccrualWithDeferredRevenueProduct();
            final Long loanId = submitApproveDisburse(clientId, productId, "01 June 2026");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(PRINCIPAL, "01 June 2026"));

            assertStatus(loanId, STATUS_CLOSED_OBLIGATIONS_MET, "accrual-product loan should be closed before adding the charge");
            final int transactionsBefore = wcLoanHelper.getTransactions(loanId).size();

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-11-01");
            addChargeExpectingAllowed(loanId, false, CHARGE_AMOUNT, "01 November 2026", "accounting on charge add");

            final GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
            assertEquals(STATUS_ACTIVE, loan.getStatus().getCode(), "the accrual-product loan must reopen to ACTIVE on charge add");

            final List<GetWorkingCapitalLoanTransactionIdResponse> transactionsAfter = wcLoanHelper.getTransactions(loanId);
            assertEquals(transactionsBefore, transactionsAfter.size(),
                    "adding a charge must not post any spurious transaction/journal entry at add-time (accrual is deferred to COB)");
        });
    }

    @Test
    @DisplayName("partial fee repayment on a charge-reopened loan stays ACTIVE with fee still outstanding (not re-closed)")
    void chargeReopenedLoan_thenPartialFeeRepayment_staysActive() {
        businessDateHelper.runAt("2026-07-01", () -> {
            final Long clientId = clientHelper.createClient("01 July 2026");
            final Long loanId = createDisburseAndCloseLoan(clientId, "01 July 2026");

            assertStatus(loanId, STATUS_CLOSED_OBLIGATIONS_MET, "loan should be closed before the charge reopens it");

            // The charge reopens the CLOSED loan to ACTIVE with a 100 fee outstanding and 0 principal outstanding.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-12-01");
            addChargeExpectingAllowed(loanId, false, CHARGE_AMOUNT, "01 December 2026", "reopen closed loan");

            final GetWorkingCapitalLoansLoanIdResponse reopened = wcLoanHelper.getLoanDetails(loanId);
            assertEquals(STATUS_ACTIVE, reopened.getStatus().getCode(), "the charge must reopen the closed loan to ACTIVE");
            final GetBalance reopenedBalance = balanceOf(reopened);
            assertEqualBigDecimal(BigDecimal.valueOf(CHARGE_AMOUNT), reopenedBalance.getFeeOutstanding(),
                    "fee outstanding must equal the charge (100) right after the reopen");
            assertEqualBigDecimal(BigDecimal.ZERO, reopenedBalance.getPrincipalOutstanding(),
                    "principal outstanding must be zero after the reopen (only the fee is due)");

            // A partial repayment of 40 against the fee leaves 60 of the fee outstanding; with principal already 0 the
            // loan must stay ACTIVE rather than re-close.
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(40), "01 December 2026"));

            final GetWorkingCapitalLoansLoanIdResponse afterPartial = wcLoanHelper.getLoanDetails(loanId);
            assertEquals(STATUS_ACTIVE, afterPartial.getStatus().getCode(),
                    "loan must stay ACTIVE while 60 of the fee remains outstanding after a partial fee repayment (not re-close)");
            final GetBalance afterBalance = balanceOf(afterPartial);
            assertEqualBigDecimal(BigDecimal.valueOf(60), afterBalance.getFeeOutstanding(),
                    "fee outstanding must be 60 (100 - 40) after the partial fee repayment");
            assertEqualBigDecimal(BigDecimal.valueOf(60), afterBalance.getTotalOutstanding(),
                    "total outstanding must be 60 after the partial fee repayment");
        });
    }

    @Test
    @DisplayName("an overpayment consumed by a charge survives reprocessing triggered by a backdated repayment")
    void chargeConsumedOverpayment_thenReprocessingTriggered_consumptionSurvives() {
        businessDateHelper.runAt("2026-08-01", () -> {
            final Long clientId = clientHelper.createClient("01 August 2026");
            final Long productId = createDefaultProduct();
            final Long loanId = submitApproveDisburse(clientId, productId, "01 August 2026");

            // Overpay the loan by 150 via a repayment on day 10 (later than disbursement so it can be backdated
            // before).
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-08-10");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(1150), "10 August 2026"));
            assertStatus(loanId, STATUS_OVERPAID, "loan should be overpaid by 150 before adding the charge");

            // The charge of 100 consumes 100 of the 150 overpayment: loan stays OVERPAID with 50 left, fee settled.
            // The charge is due on 20 August (after both the repayment date and the reprocessing business date) so that
            // a
            // later replay of the persisted repayments cannot itself settle this not-yet-due charge: only the
            // overpayment
            // consumption ever paid it, which is the state at risk when reprocessing runs.
            final Long feeLoanChargeId = addChargeReturningId(loanId, CHARGE_AMOUNT, "20 August 2026", "charge consumes overpayment");

            final GetWorkingCapitalLoansLoanIdResponse afterCharge = wcLoanHelper.getLoanDetails(loanId);
            assertEquals(STATUS_OVERPAID, afterCharge.getStatus().getCode(),
                    "loan must stay OVERPAID after the charge consumes part of the overpayment (150 > 100)");
            assertEqualBigDecimal(BigDecimal.valueOf(50), balanceOf(afterCharge).getOverpaymentAmount(),
                    "overpayment must drop to 50 (150 - 100) once the charge consumes 100");
            assertEqualBigDecimal(BigDecimal.ZERO, balanceOf(afterCharge).getFeeOutstanding(),
                    "the charge must be fully settled by the overpayment right after it is added (fee outstanding 0)");
            assertTrue(findCharge(wcLoanHelper.getCharges(loanId), feeLoanChargeId).getPaid(),
                    "the charge must be flagged paid after being consumed by the overpayment");

            // A backdated repayment (dated day 5, before the day-10 repayment) triggers reprocessTransactions.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-08-15");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(50), "05 August 2026"));

            // The charge consumption must be durable across the reprocessing event: the charge stays settled.
            final WorkingCapitalLoanChargeData chargeAfter = findCharge(wcLoanHelper.getCharges(loanId), feeLoanChargeId);
            assertEqualBigDecimal(BigDecimal.ZERO, chargeAfter.getAmountOutstanding(),
                    "the charge must remain fully paid (0 outstanding) after reprocessing, not revert to unpaid");
            assertTrue(chargeAfter.getPaid(), "the charge must remain flagged paid after reprocessing, not revert to unpaid");

            final GetBalance afterReprocess = balanceOf(wcLoanHelper.getLoanDetails(loanId));
            assertEqualBigDecimal(BigDecimal.ZERO, afterReprocess.getFeeOutstanding(),
                    "fee outstanding must stay 0 after reprocessing (the consumed charge must not be restored as due)");
            // Total repaid 1200 against 1000 principal + 100 fee => overpayment 100 once the consumption survives.
            assertEqualBigDecimal(BigDecimal.valueOf(100), afterReprocess.getOverpaymentAmount(),
                    "overpayment must be 100 (1200 repaid - 1000 principal - 100 consumed fee), not restored to 200");
        });
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private void addChargeExpectingAllowed(Long loanId, boolean penalty, double amount, String dueDate, String scenarioContext) {
        final Long chargeDefId = wcLoanHelper.createGlobalCharge(WorkingCapitalLoanRequestBuilders.specifiedDueDateCharge(penalty, amount));
        try {
            wcLoanHelper.addCharge(loanId, WorkingCapitalLoanRequestBuilders.addCharge(chargeDefId, amount, dueDate));
        } catch (final CallFailedRuntimeException e) {
            throw new AssertionError(scenarioContext + " — adding a charge on a closed/overpaid/past-maturity WC loan must be allowed, "
                    + "but the server rejected it: " + e.getMessage(), e);
        }
    }

    private Long addChargeReturningId(Long loanId, double amount, String dueDate, String scenarioContext) {
        final Long chargeDefId = wcLoanHelper.createGlobalCharge(WorkingCapitalLoanRequestBuilders.specifiedDueDateCharge(false, amount));
        try {
            return wcLoanHelper.addCharge(loanId, WorkingCapitalLoanRequestBuilders.addCharge(chargeDefId, amount, dueDate));
        } catch (final CallFailedRuntimeException e) {
            throw new AssertionError(scenarioContext + " — adding a charge on a closed/overpaid/past-maturity WC loan must be allowed, "
                    + "but the server rejected it: " + e.getMessage(), e);
        }
    }

    private Long createDisburseAndCloseLoan(Long clientId, String date) {
        final Long productId = createDefaultProduct();
        final Long loanId = submitApproveDisburse(clientId, productId, date);
        wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(PRINCIPAL, date));
        return loanId;
    }

    private Long createDisburseAndOverpayLoan(Long clientId, String date, BigDecimal repaymentAmount) {
        final Long productId = createDefaultProduct();
        final Long loanId = submitApproveDisburse(clientId, productId, date);
        wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(repaymentAmount, date));
        return loanId;
    }

    private Long submitApproveDisburse(Long clientId, Long productId, String date) {
        final Long loanId = wcLoanHelper.submitApplication(
                WorkingCapitalLoanRequestBuilders.submitApplication(clientId, productId, PRINCIPAL, BigDecimal.valueOf(18), date, date));
        createdLoanIds.add(loanId);
        wcLoanHelper.approve(loanId, WorkingCapitalLoanRequestBuilders.approve(date, PRINCIPAL, date));
        wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburse(date, PRINCIPAL));
        return loanId;
    }

    private Long createDefaultProduct() {
        final String uniqueName = "WCL ChgAfterMat " + Utils.uniqueRandomStringGenerator("", 8);
        final String uniqueShortName = Utils.uniqueRandomStringGenerator("", 4);
        // A delinquency bucket and a breach are configured so that, once the charge creates an outstanding balance, the
        // schedule services can generate the delinquency + breach periods the tests assert.
        final PostDelinquencyRangeResponse range = DelinquencyRangesHelper.createRange(new DelinquencyRangeRequest()
                .classification(Utils.randomStringGenerator("DLQ_R_", 10)).minimumAgeDays(1).maximumAgeDays(60).locale("en"));
        final PostDelinquencyBucketResponse bucket = WorkingCapitalLoanDelinquencyRangeScheduleHelper
                .createWorkingCapitalLoanDelinquencyBucket(List.of(range.getResourceId()), DELINQUENCY_FREQUENCY_DAYS, 0,
                        DELINQUENCY_MIN_PAYMENT_PERCENT, 1);
        final Long breachId = breachHelper.create(breachHelper.createBreachRequest(Utils.randomStringGenerator("WC_BREACH_", 8),
                BREACH_FREQUENCY, BREACH_FREQUENCY_TYPE, BREACH_AMOUNT_CALCULATION_TYPE, BREACH_MIN_PAYMENT_AMOUNT));
        final Long productId = productHelper.createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder().withName(uniqueName)
                .withShortName(uniqueShortName).withDelinquencyBucketId(bucket.getResourceId())
                .withDelinquencyGraceDays(DELINQUENCY_GRACE_DAYS).withBreachId(breachId).build()).getResourceId();
        createdProductIds.add(productId);
        return productId;
    }

    private Long createAccrualWithDeferredRevenueProduct() {
        final String uniqueName = "WCL ChgAfterMatAcc " + Utils.uniqueRandomStringGenerator("", 8);
        final String uniqueShortName = Utils.uniqueRandomStringGenerator("", 4);
        final Long productId = productHelper.createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder().withName(uniqueName)
                .withShortName(uniqueShortName).withAllowAttributeOverrides(Map.of("discountDefault", Boolean.TRUE))
                .withAccountingRule(AccountingRuleEnum.ACC_DEF_REV_AM).withFundSourceAccountId(fundSourceAccount.getAccountID().longValue())
                .withLoanPortfolioAccountId(loanPortfolioAccount.getAccountID().longValue())
                .withTransfersInSuspenseAccountId(transfersSuspenseAccount.getAccountID().longValue())
                .withIncomeFromDiscountFeeAccountId(incomeFromDiscountFeeAccount.getAccountID().longValue())
                .withReceivableFeeAccountId(feesReceivableAccount.getAccountID().longValue())
                .withReceivablePenaltyAccountId(penaltiesReceivableAccount.getAccountID().longValue())
                .withIncomeFromFeeAccountId(incomeFromFeeAccount.getAccountID().longValue())
                .withIncomeFromPenaltyAccountId(incomeFromPenaltyAccount.getAccountID().longValue())
                .withIncomeFromRecoveryAccountId(incomeFromRecoveryAccount.getAccountID().longValue())
                .withWriteOffAccountId(writeOffAccount.getAccountID().longValue())
                .withOverpaymentLiabilityAccountId(overpaymentAccount.getAccountID().longValue())
                .withDeferredIncomeLiabilityAccountId(deferredIncomeAccount.getAccountID().longValue()).build()).getResourceId();
        createdProductIds.add(productId);
        return productId;
    }

    private void assertStatus(Long loanId, String expectedCode, String message) {
        assertEquals(expectedCode, wcLoanHelper.getLoanDetails(loanId).getStatus().getCode(), message);
    }

    private GetBalance balanceOf(GetWorkingCapitalLoansLoanIdResponse loan) {
        assertNotNull(loan.getBalance(), "balance should exist");
        return loan.getBalance();
    }
}
