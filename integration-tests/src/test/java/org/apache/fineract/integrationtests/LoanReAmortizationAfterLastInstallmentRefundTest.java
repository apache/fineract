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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.junit.jupiter.api.Test;

/**
 * A refund allocated to the last installment can pay that installment in full. A later disbursement re-amortizes the
 * loan, and the rounding difference of the recalculated installment amounts must not be taken off an installment which
 * has already been paid, otherwise that installment ends up with a negative outstanding amount and the loan can neither
 * be closed nor marked as overpaid.
 */
public class LoanReAmortizationAfterLastInstallmentRefundTest extends FeignLoanTestBase {

    private static final String DISBURSEMENT_DATE = "01 January 2024";
    private static final String REFUND_DATE = "05 January 2024";
    private static final String SECOND_DISBURSEMENT_DATE = "10 January 2024";
    private static final Double LOAN_PRINCIPAL = 70.16;
    private static final Double FIRST_DISBURSEMENT_AMOUNT = 70.15;

    /**
     * 70.15 over 3 installments gives 23.38 / 23.38 / 23.39, the extra cent sitting on the last installment. Disbursing
     * one more cent turns the installment amount into 23.39, so the rounding difference flips sign and has to be taken
     * off an installment - which must not be the last one, since the refund has fully paid it.
     */
    @Test
    public void disbursementAfterLastInstallmentRefundOnNonInterestBearingLoan() {
        runAt(SECOND_DISBURSEMENT_DATE, () -> {
            final Long loanId = createProgressiveLoan(0.0);

            disburseLoan(loanId, BigDecimal.valueOf(FIRST_DISBURSEMENT_AMOUNT), DISBURSEMENT_DATE);
            verifyRepaymentSchedule(loanId, //
                    installment(FIRST_DISBURSEMENT_AMOUNT, null, DISBURSEMENT_DATE), //
                    installment(23.38, 0.0, 23.38, false, "01 February 2024"), //
                    installment(23.38, 0.0, 23.38, false, "01 March 2024"), //
                    installment(23.39, 0.0, 23.39, false, "01 April 2024"));

            makeMerchantIssuedRefund(loanId, 23.39, REFUND_DATE);
            disburseLoan(loanId, BigDecimal.valueOf(0.01), SECOND_DISBURSEMENT_DATE);

            // The cent has to come off an installment which still has room for it, not off the paid one
            verifyRepaymentSchedule(loanId, //
                    installment(FIRST_DISBURSEMENT_AMOUNT, null, DISBURSEMENT_DATE), //
                    installment(0.01, null, SECOND_DISBURSEMENT_DATE), //
                    installment(23.39, 0.0, 23.39, false, "01 February 2024"), //
                    installment(23.38, 0.0, 23.38, false, "01 March 2024"), //
                    installment(23.39, 0.0, 0.0, true, "01 April 2024"));
            verifyNoInstallmentIsOverpaid(loanId);

            addRepaymentForLoan(loanId, 46.77, SECOND_DISBURSEMENT_DATE);
            verifyLoanStatus(loanId, LoanStatus.CLOSED_OBLIGATIONS_MET);
            verifyNoInstallmentIsOverpaid(loanId);
        });
    }

    /**
     * The payments of a charged-off loan are not reported to the interest model either, so a re-amortization leaves the
     * same inconsistency behind. Disbursement is not permitted on a charged-off loan, capitalized income is, and it
     * re-amortizes the loan the same way. The installment amounts depend on the interest of the loan, therefore the
     * refund amount is taken from the schedule and the outcome is asserted on the invariant instead of fixed amounts.
     */
    @Test
    public void capitalizedIncomeAfterLastInstallmentRefundOnChargedOffLoan() {
        runAt(SECOND_DISBURSEMENT_DATE, () -> {
            final Long loanId = createProgressiveLoan(create4IProgressiveWithCapitalizedIncome(), 10.0);

            disburseLoan(loanId, BigDecimal.valueOf(FIRST_DISBURSEMENT_AMOUNT), DISBURSEMENT_DATE);
            chargeOffLoan(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate(REFUND_DATE).locale("en"));

            // Pay the last installment in full, whatever amount it came out as
            makeMerchantIssuedRefund(loanId, lastInstallmentOf(loanId).getTotalDueForPeriod().doubleValue(), REFUND_DATE);
            assertTrue(isFullyPaid(lastInstallmentOf(loanId)),
                    "The refund was expected to pay the last installment in full, otherwise the test does not cover the case");

            addCapitalizedIncome(loanId, SECOND_DISBURSEMENT_DATE, 0.01);

            verifyNoInstallmentIsOverpaid(loanId);
            final GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getSummary().getTotalOutstanding().compareTo(BigDecimal.ZERO) >= 0,
                    "The loan must not end up with a negative outstanding amount, actual: "
                            + loanDetails.getSummary().getTotalOutstanding());
        });
    }

    private Long createProgressiveLoan(final Double interestRate) {
        return createProgressiveLoan(create4IProgressive(), interestRate);
    }

    private Long createProgressiveLoan(final PostLoanProductsRequest product, final Double interestRate) {
        final Long clientId = createClient();
        final Long loanProductId = createLoanProduct(withRefundOnLastInstallment(product));
        final Long loanId = applyForLoan(
                applyLP2ProgressiveLoanRequest(clientId, loanProductId, DISBURSEMENT_DATE, LOAN_PRINCIPAL, interestRate, 3, null));
        assertNotNull(loanId);
        approveLoan(loanId, LoanRequestBuilders.approveLoan(LOAN_PRINCIPAL, DISBURSEMENT_DATE));
        return loanId;
    }

    private PostLoanProductsRequest withRefundOnLastInstallment(final PostLoanProductsRequest product) {
        return product//
                .numberOfRepayments(3)//
                // the rounding difference this test is about only shows up on amounts below the default minimum
                .minPrincipal(1.0)//
                .principal(LOAN_PRINCIPAL)//
                .paymentAllocation(List.of(createDefaultPaymentAllocation("NEXT_INSTALLMENT"),
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "LAST_INSTALLMENT")));
    }

    private void makeMerchantIssuedRefund(final Long loanId, final Double amount, final String date) {
        makeMerchantIssuedRefund(loanId, new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate(date)
                .locale("en").transactionAmount(amount));
    }

    private List<GetLoansLoanIdRepaymentPeriod> installmentsOf(final Long loanId) {
        return getLoanDetails(loanId).getRepaymentSchedule().getPeriods().stream().filter(period -> period.getPeriod() != null).toList();
    }

    private GetLoansLoanIdRepaymentPeriod lastInstallmentOf(final Long loanId) {
        return installmentsOf(loanId).getLast();
    }

    private boolean isFullyPaid(final GetLoansLoanIdRepaymentPeriod installment) {
        return installment.getTotalOutstandingForPeriod() == null
                || installment.getTotalOutstandingForPeriod().compareTo(BigDecimal.ZERO) == 0;
    }

    private void verifyNoInstallmentIsOverpaid(final Long loanId) {
        for (final GetLoansLoanIdRepaymentPeriod installment : installmentsOf(loanId)) {
            final BigDecimal principalDue = zeroIfNull(installment.getPrincipalDue());
            final BigDecimal principalPaid = zeroIfNull(installment.getPrincipalPaid());
            assertTrue(principalDue.compareTo(principalPaid) >= 0,
                    "Installment %s has less principal due (%s) than what has been paid on it (%s)".formatted(installment.getDueDate(),
                            principalDue, principalPaid));
            assertTrue(zeroIfNull(installment.getTotalOutstandingForPeriod()).compareTo(BigDecimal.ZERO) >= 0,
                    "Installment %s has a negative outstanding amount (%s)".formatted(installment.getDueDate(),
                            installment.getTotalOutstandingForPeriod()));
        }
    }

    private BigDecimal zeroIfNull(final BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
