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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.client.models.GetBalance;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.WorkingCapitalLoanTransactionTemplateResponse;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessDateHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalLoanRequestBuilders;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Verifies that the prepayment quote is the loan's balance as of the requested date rather than the current snapshot.
 *
 * <p>
 * Only what the loan owes is scoped to the date. A charge counts from the earlier of the day it was added and the day
 * it falls due, so a not-yet-due charge is part of the payoff but one added after the quoted date is not. Payments are
 * deliberately not scoped, which is what keeps a backdated payoff from closing the loan and then overpaying it.
 *
 * <p>
 * The last test is the requirement the rest of this exists to serve: backdating must reach the same end state as living
 * through the dates.
 */
public class FeignWorkingCapitalLoanPrepaymentAsOfDateTest extends FeignIntegrationTest {

    private static final String PREPAY = "prepayLoan";
    private static final BigDecimal PRINCIPAL = BigDecimal.valueOf(9000);
    private static final double FEE = 40.0;

    private FeignWorkingCapitalLoanHelper wcLoanHelper;
    private FeignClientHelper clientHelper;
    private FeignBusinessDateHelper businessDateHelper;
    private WorkingCapitalLoanProductHelper productHelper;

    private final List<Long> createdLoanIds = new ArrayList<>();
    private final List<Long> createdProductIds = new ArrayList<>();

    @BeforeAll
    void setupHelpers() {
        wcLoanHelper = new FeignWorkingCapitalLoanHelper(fineractClient());
        clientHelper = new FeignClientHelper(fineractClient());
        businessDateHelper = new FeignBusinessDateHelper(fineractClient());
        productHelper = new WorkingCapitalLoanProductHelper();
    }

    @AfterAll
    void cleanupEntities() {
        createdLoanIds.forEach(wcLoanHelper::cleanupLoan);
        createdLoanIds.clear();
        createdProductIds.clear();
    }

    @Test
    @DisplayName("A payoff quoted before the disbursement is zero, and the full principal from the disbursement date on")
    void payoffQuotedBeforeTheDisbursement_isZero() {
        businessDateHelper.runAt("2026-01-10", () -> {
            final Long client = clientHelper.createClient("10 January 2026");
            final Long loanId = createAndDisburseLoanOnDate(client, PRINCIPAL, "10 January 2026");

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-15");

            // Five days before the money was paid out there was nothing to pay off. Quoting the current balance here
            // would hand the caller an amount for a day on which the loan did not yet owe it.
            final WorkingCapitalLoanTransactionTemplateResponse before = wcLoanHelper.getTransactionTemplate(loanId, PREPAY,
                    "05 January 2026");
            assertEqualBigDecimal(BigDecimal.ZERO, before.getExpectedAmount(), "Payoff before the disbursement");
            assertEqualBigDecimal(BigDecimal.ZERO, before.getPrincipalPortion(), "Principal portion before the disbursement");

            final WorkingCapitalLoanTransactionTemplateResponse onDisbursement = wcLoanHelper.getTransactionTemplate(loanId, PREPAY,
                    "10 January 2026");
            assertEqualBigDecimal(PRINCIPAL, onDisbursement.getExpectedAmount(), "Payoff on the disbursement date");
            assertEqualBigDecimal(PRINCIPAL, onDisbursement.getPrincipalPortion(), "Principal portion on the disbursement date");
        });
    }

    @Test
    @DisplayName("A charge counts from the day it is added, so a not-yet-due charge is still part of the payoff")
    void chargeNotYetDue_isStillPartOfThePayoff() {
        businessDateHelper.runAt("2026-02-01", () -> {
            final Long client = clientHelper.createClient("01 February 2026");
            final Long loanId = createAndDisburseLoanOnDate(client, PRINCIPAL, "01 February 2026");

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-02-10");
            addCharge(loanId, FEE, "20 February 2026");

            // Added on the 10th, due on the 20th. On the 10th it is already a known obligation, so it can be settled -
            // excluding it would leave the quote unable to close a loan that carries any future-dated charge, and
            // would disagree with the outstanding balance the loan itself reports.
            final WorkingCapitalLoanTransactionTemplateResponse template = wcLoanHelper.getTransactionTemplate(loanId, PREPAY,
                    "10 February 2026");
            assertEqualBigDecimal(BigDecimal.valueOf(9040), template.getExpectedAmount(), "Payoff includes the not-yet-due fee");
            assertEqualBigDecimal(BigDecimal.valueOf(FEE), template.getFeeChargesPortion(), "Fee portion of the payoff");

            wcLoanHelper.makeRepayment(loanId,
                    WorkingCapitalLoanRequestBuilders.repayment(template.getExpectedAmount(), "10 February 2026"));

            assertClosed(loanId);
            assertEqualBigDecimal(BigDecimal.ZERO, balanceOf(loanId).getTotalOutstanding(), "Nothing is left outstanding");
        });
    }

    @Test
    @DisplayName("A charge added after the quoted date is left out, so the payoff clears the principal and leaves the loan open")
    void chargeAddedAfterTheQuotedDate_isLeftOutOfThePayoff() {
        businessDateHelper.runAt("2026-03-01", () -> {
            final Long client = clientHelper.createClient("01 March 2026");
            final Long loanId = createAndDisburseLoanOnDate(client, PRINCIPAL, "01 March 2026");

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-03-10");
            addCharge(loanId, FEE, "20 March 2026");

            // On 05 March the fee did not exist yet, so it is not part of that day's payoff.
            final WorkingCapitalLoanTransactionTemplateResponse template = wcLoanHelper.getTransactionTemplate(loanId, PREPAY,
                    "05 March 2026");
            assertEqualBigDecimal(PRINCIPAL, template.getExpectedAmount(), "Payoff excludes the later fee");
            assertEqualBigDecimal(BigDecimal.ZERO, template.getFeeChargesPortion(), "Fee portion of the payoff");

            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(template.getExpectedAmount(), "05 March 2026"));

            // Posting it settles the principal but not the fee, so the loan stays open rather than absorbing an
            // obligation that did not exist on the date it was quoted for.
            assertActive(loanId);
            final GetBalance balance = balanceOf(loanId);
            assertEqualBigDecimal(BigDecimal.ZERO, balance.getPrincipalOutstanding(), "Principal is cleared");
            assertEqualBigDecimal(BigDecimal.valueOf(FEE), balance.getTotalOutstanding(), "The later fee is still outstanding");
            assertEqualBigDecimal(BigDecimal.ZERO, balance.getOverpaymentAmount(), "Nothing spills into overpayment");
        });
    }

    @Test
    @DisplayName("Backdating the payoff reaches the same end state as living through the dates")
    void backdatedAndNonBackdatedPaths_reachTheSameEndState() {
        businessDateHelper.runAt("2026-04-01", () -> {
            final Long client = clientHelper.createClient("01 April 2026");
            final Long livedThrough = createAndDisburseLoanOnDate(client, PRINCIPAL, "01 April 2026");
            final Long backdated = createAndDisburseLoanOnDate(client, PRINCIPAL, "01 April 2026");

            // Path A: close the loan on 05 April, as it happens, with nothing backdated.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-04-05");
            final WorkingCapitalLoanTransactionTemplateResponse quoteOnTheDay = wcLoanHelper.getTransactionTemplate(livedThrough, PREPAY,
                    "05 April 2026");
            wcLoanHelper.makeRepayment(livedThrough,
                    WorkingCapitalLoanRequestBuilders.repayment(quoteOnTheDay.getExpectedAmount(), "05 April 2026"));
            assertClosed(livedThrough);

            // A fee then arrives on the 10th and reopens it.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-04-10");
            addCharge(livedThrough, FEE, "20 April 2026");

            // Path B: the same timeline, but the fee lands first and the payoff is backdated to 05 April afterwards.
            addCharge(backdated, FEE, "20 April 2026");
            final WorkingCapitalLoanTransactionTemplateResponse backdatedQuote = wcLoanHelper.getTransactionTemplate(backdated, PREPAY,
                    "05 April 2026");
            wcLoanHelper.makeRepayment(backdated,
                    WorkingCapitalLoanRequestBuilders.repayment(backdatedQuote.getExpectedAmount(), "05 April 2026"));

            // If these ever diverge, the as-of quote has started inventing history.
            final GetBalance livedThroughBalance = balanceOf(livedThrough);
            final GetBalance backdatedBalance = balanceOf(backdated);
            assertEqualBigDecimal(livedThroughBalance.getTotalOutstanding(), backdatedBalance.getTotalOutstanding(),
                    "Total outstanding matches whether the payoff was backdated or not");
            assertEqualBigDecimal(livedThroughBalance.getPrincipalOutstanding(), backdatedBalance.getPrincipalOutstanding(),
                    "Principal outstanding matches whether the payoff was backdated or not");
            assertEqualBigDecimal(livedThroughBalance.getOverpaymentAmount(), backdatedBalance.getOverpaymentAmount(),
                    "Overpayment matches whether the payoff was backdated or not");

            // And the shared end state is the one the timeline implies: principal settled, the later fee still due.
            assertEqualBigDecimal(BigDecimal.valueOf(FEE), backdatedBalance.getTotalOutstanding(), "The later fee is still outstanding");
            assertEqualBigDecimal(BigDecimal.ZERO, backdatedBalance.getPrincipalOutstanding(), "Principal is cleared");
            assertActive(livedThrough);
            assertActive(backdated);
        });
    }

    private void assertActive(final Long loanId) {
        final GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
        assertNotNull(loan.getStatus(), "Status should exist");
        assertTrue(Boolean.TRUE.equals(loan.getStatus().getActive()),
                "Loan " + loanId + " should be active but was " + loan.getStatus().getValue());
    }

    private void assertClosed(final Long loanId) {
        final GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
        assertNotNull(loan.getStatus(), "Status should exist");
        assertTrue(Boolean.TRUE.equals(loan.getStatus().getClosedObligationsMet()),
                "Loan " + loanId + " should be closed with obligations met but was " + loan.getStatus().getValue());
    }

    private Long addCharge(final Long loanId, final double amount, final String dueDate) {
        final Long chargeId = wcLoanHelper.createGlobalCharge(WorkingCapitalLoanRequestBuilders.specifiedDueDateCharge(false, amount));
        return wcLoanHelper.addCharge(loanId, WorkingCapitalLoanRequestBuilders.addCharge(chargeId, amount, dueDate));
    }

    private GetBalance balanceOf(final Long loanId) {
        final GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
        assertNotNull(loan.getBalance(), "Balance should exist");
        return loan.getBalance();
    }

    private Long createAndDisburseLoanOnDate(final Long clientId, final BigDecimal principal, final String date) {
        final Long productId = createProduct();
        final Long loanId = wcLoanHelper.submitApplication(
                WorkingCapitalLoanRequestBuilders.submitApplication(clientId, productId, principal, BigDecimal.valueOf(18), date, date));
        createdLoanIds.add(loanId);
        wcLoanHelper.approve(loanId, WorkingCapitalLoanRequestBuilders.approve(date, principal, date));
        wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburse(date, principal));
        return loanId;
    }

    private Long createProduct() {
        final String uniqueName = "WCL PrepayAsOf " + Utils.uniqueRandomStringGenerator("", 8);
        final String uniqueShortName = Utils.uniqueRandomStringGenerator("", 4);
        final Long productId = productHelper
                .createWorkingCapitalLoanProduct(
                        new WorkingCapitalLoanProductTestBuilder().withName(uniqueName).withShortName(uniqueShortName).build())
                .getResourceId();
        createdProductIds.add(productId);
        return productId;
    }
}
