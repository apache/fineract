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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdStatus;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTemplateResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansRequestChargeData;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.ChargeRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.junit.jupiter.api.Test;

/**
 * Foreclosure of a progressive loan without interest recalculation (FINERACT-2971): the foreclosure accelerates the
 * maturity to the foreclosure date and pays off what is due on that date.
 */
public class FeignLoanForeclosureProgressiveTest extends FeignLoanTestBase {

    private static final String DISBURSEMENT_DATE = "01 January 2026";
    private static final String SECOND_DUE_DATE = "01 March 2026";
    private static final String MID_PERIOD_DATE = "15 March 2026";
    private static final String FEE_DUE_DATE = "15 April 2026";
    private static final String GOODWILL_CREDIT_DATE = "10 March 2026";
    private static final double PRINCIPAL = 1000.0;
    private static final double YEARLY_INTEREST_RATE = 24.0;
    private static final int ACCOUNTING_NONE = 1;

    @Test
    void testForeclosureOnDueDateClosesTheLoanWithTheInterestUpToThatDate() {
        final Long loanId = createDisbursedLoan();

        runAt(SECOND_DUE_DATE, () -> {
            // Without earlier repayments, the foreclosure collects the interest of the first two installments
            final BigDecimal interestOfFirstTwoInstallments = interestDueOfPeriods(getLoanDetails(loanId), 2);
            final GetLoansLoanIdTransactionsTemplateResponse template = foreclosureTemplate(loanId, SECOND_DUE_DATE);
            assertAmount(interestOfFirstTwoInstallments, amount(template.getInterestPortion()));

            forecloseLoan(loanId, foreclosure(SECOND_DUE_DATE));

            final GetLoansLoanIdResponse loan = getLoanDetails(loanId);
            verifyClosedWithoutOverpayment(loan);
            final GetLoansLoanIdTransactions foreclosure = foreclosureRepayment(loan, LocalDate.of(2026, 3, 1));
            assertAmount(amount(template.getAmount()), foreclosure.getAmount());
            assertAmount(BigDecimal.valueOf(PRINCIPAL), foreclosure.getPrincipalPortion());
            assertAmount(interestOfFirstTwoInstallments, foreclosure.getInterestPortion());
            assertEquals(2, repaymentPeriods(loan).size(), "the schedule ends on the foreclosure date");
            verifyInterestBookedMatchesInterestCharged(loanId, loan, LocalDate.of(2026, 3, 1));
        });
    }

    @Test
    void testForeclosureInTheMiddleOfAPeriodCollectsWhatTheTemplateShows() {
        final Long loanId = createDisbursedLoan();

        runAt(MID_PERIOD_DATE, () -> {
            // The interest of the first two installments, and the third one's prorated by day: 14 of its 31 days
            final GetLoansLoanIdResponse loanBefore = getLoanDetails(loanId);
            final BigDecimal expectedInterest = interestDueOfPeriods(loanBefore, 2).add(interestDueOfPeriod(loanBefore, 3)
                    .multiply(BigDecimal.valueOf(14)).divide(BigDecimal.valueOf(31), 2, RoundingMode.HALF_EVEN));
            final GetLoansLoanIdTransactionsTemplateResponse template = foreclosureTemplate(loanId, MID_PERIOD_DATE);
            assertAmount(expectedInterest, amount(template.getInterestPortion()));

            forecloseLoan(loanId, foreclosure(MID_PERIOD_DATE));

            final GetLoansLoanIdResponse loan = getLoanDetails(loanId);
            verifyClosedWithoutOverpayment(loan);
            final GetLoansLoanIdTransactions foreclosure = foreclosureRepayment(loan, LocalDate.of(2026, 3, 15));
            assertAmount(amount(template.getAmount()), foreclosure.getAmount());
            assertAmount(expectedInterest, foreclosure.getInterestPortion());
            assertEquals(3, repaymentPeriods(loan).size(), "the schedule ends on the foreclosure date");
            verifyInterestBookedMatchesInterestCharged(loanId, loan, LocalDate.of(2026, 3, 15));
        });
    }

    @Test
    void testForeclosureInTheMiddleOfAPeriodWithAPercentageInstallmentFeeCollectsWhatTheTemplateShows() {
        final Long loanId = createDisbursedLoanWithAPercentageInstallmentFee();

        runAt(MID_PERIOD_DATE, () -> {
            final BigDecimal expectedFee = expectedFeeAfterAMidPeriodTermination(getLoanDetails(loanId));
            final GetLoansLoanIdTransactionsTemplateResponse template = foreclosureTemplate(loanId, MID_PERIOD_DATE);
            assertAmount(expectedFee, amount(template.getFeeChargesPortion()));

            forecloseLoan(loanId, foreclosure(MID_PERIOD_DATE));

            final GetLoansLoanIdResponse loan = getLoanDetails(loanId);
            verifyClosedWithoutOverpayment(loan);
            final GetLoansLoanIdTransactions foreclosure = foreclosureRepayment(loan, LocalDate.of(2026, 3, 15));
            assertAmount(amount(template.getAmount()), foreclosure.getAmount());
            assertAmount(amount(template.getPrincipalPortion()), foreclosure.getPrincipalPortion());
            assertAmount(amount(template.getInterestPortion()), foreclosure.getInterestPortion());
            assertAmount(expectedFee, foreclosure.getFeeChargesPortion());
            assertEquals(3, repaymentPeriods(loan).size(), "the schedule ends on the foreclosure date");
        });
    }

    @Test
    void testContractTerminationWithAPercentageInstallmentFeeLeavesWhatTheTemplateShows() {
        final Long loanId = createDisbursedLoanWithAPercentageInstallmentFee();

        runAt(MID_PERIOD_DATE, () -> {
            final BigDecimal expectedFee = expectedFeeAfterAMidPeriodTermination(getLoanDetails(loanId));
            final GetLoansLoanIdTransactionsTemplateResponse template = transactionHelper.retrieveTransactionTemplate(loanId,
                    "contractTermination", LoanTestData.DATETIME_PATTERN, MID_PERIOD_DATE, LoanTestData.LOCALE);
            assertAmount(expectedFee, amount(template.getFeeChargesPortion()));

            applyContractTermination(loanId, MID_PERIOD_DATE);

            // The termination doesn't pay anything: what it leaves outstanding is what the template showed
            final GetLoansLoanIdResponse loan = getLoanDetails(loanId);
            assertAmount(amount(template.getAmount()), loan.getSummary().getTotalOutstanding());
            assertAmount(amount(template.getInterestPortion()), loan.getSummary().getInterestOutstanding());
            assertAmount(expectedFee, loan.getSummary().getFeeChargesOutstanding());
        });
    }

    @Test
    void testForeclosureLeavesOutAFeeDueAfterTheForeclosureDate() {
        final Long loanId = createDisbursedLoan();
        runAt(DISBURSEMENT_DATE, () -> addLoanCharge(loanId, chargesHelper.createLoanSpecifiedDueDateCharge(10.0, "EUR").getResourceId(),
                FEE_DUE_DATE, 10.0));

        runAt(SECOND_DUE_DATE, () -> {
            final GetLoansLoanIdTransactionsTemplateResponse template = foreclosureTemplate(loanId, SECOND_DUE_DATE);
            assertAmount(BigDecimal.ZERO, amount(template.getFeeChargesPortion()));

            forecloseLoan(loanId, foreclosure(SECOND_DUE_DATE));

            final GetLoansLoanIdResponse loan = getLoanDetails(loanId);
            verifyClosedWithoutOverpayment(loan);
            assertAmount(amount(template.getAmount()), foreclosureRepayment(loan, LocalDate.of(2026, 3, 1)).getAmount());
            assertEquals(2, repaymentPeriods(loan).size(), "the fee due after the foreclosure date adds no installment");
        });
    }

    @Test
    void testGoodwillCreditAfterForeclosureKeepsTheForeclosure() {
        final Long loanId = createDisbursedLoan();
        final GetLoansLoanIdTransactions[] foreclosureHolder = new GetLoansLoanIdTransactions[1];

        runAt(SECOND_DUE_DATE, () -> {
            forecloseLoan(loanId, foreclosure(SECOND_DUE_DATE));
            foreclosureHolder[0] = foreclosureRepayment(getLoanDetails(loanId), LocalDate.of(2026, 3, 1));
        });

        runAt(GOODWILL_CREDIT_DATE, () -> {
            // A goodwill credit on the closed loan makes the processor replay the loan's transactions
            makeGoodwillCredit(loanId, repayment(50.0, GOODWILL_CREDIT_DATE));

            final GetLoansLoanIdResponse loan = getLoanDetails(loanId);
            verifyLoanStatus(loan, GetLoansLoanIdStatus::getOverpaid);
            assertAmount(BigDecimal.valueOf(50.0), loan.getTotalOverpaid());
            final GetLoansLoanIdTransactions foreclosure = foreclosureRepayment(loan, LocalDate.of(2026, 3, 1));
            assertAmount(foreclosureHolder[0].getPrincipalPortion(), foreclosure.getPrincipalPortion());
            assertAmount(foreclosureHolder[0].getInterestPortion(), foreclosure.getInterestPortion());
            assertEquals(2, repaymentPeriods(loan).size(), "the replay keeps the schedule ending on the foreclosure date");
        });
    }

    @Test
    void testAdjustingTheForeclosureKeepsTheForeclosure() {
        final Long loanId = createDisbursedLoan();
        final GetLoansLoanIdTransactions[] foreclosureHolder = new GetLoansLoanIdTransactions[1];

        runAt(SECOND_DUE_DATE, () -> {
            forecloseLoan(loanId, foreclosure(SECOND_DUE_DATE));
            foreclosureHolder[0] = foreclosureRepayment(getLoanDetails(loanId), LocalDate.of(2026, 3, 1));
        });

        runAt(GOODWILL_CREDIT_DATE, () -> {
            // A closed foreclosed loan can't be adjusted, but an overpaid one can
            makeGoodwillCredit(loanId, repayment(50.0, GOODWILL_CREDIT_DATE));
            transactionHelper.adjustLoanTransaction(loanId, foreclosureHolder[0].getId(), SECOND_DUE_DATE,
                    foreclosureHolder[0].getAmount().doubleValue());

            // The replacement still ends the loan on the foreclosure date
            final GetLoansLoanIdResponse loan = getLoanDetails(loanId);
            verifyLoanStatus(loan, GetLoansLoanIdStatus::getOverpaid);
            assertAmount(BigDecimal.valueOf(50.0), loan.getTotalOverpaid());
            final GetLoansLoanIdTransactions replacement = foreclosureRepayment(loan, LocalDate.of(2026, 3, 1));
            assertNotEquals(foreclosureHolder[0].getId(), replacement.getId(), "the foreclosure is replaced");
            assertAmount(foreclosureHolder[0].getPrincipalPortion(), replacement.getPrincipalPortion());
            assertAmount(foreclosureHolder[0].getInterestPortion(), replacement.getInterestPortion());
            assertEquals(2, repaymentPeriods(loan).size(), "the replacement keeps the schedule ending on the foreclosure date");
        });
    }

    private Long createDisbursedLoan() {
        return createDisbursedLoan(fourInstallmentsProgressiveWithAdvancedAllocation(), List.of());
    }

    private Long createDisbursedLoan(final PostLoanProductsRequest product, final List<PostLoansRequestChargeData> charges) {
        final Long[] loanIdHolder = new Long[1];
        runAt(DISBURSEMENT_DATE, () -> {
            final Long clientId = createClient(DISBURSEMENT_DATE);
            final Long productId = createLoanProduct(product);
            final Long loanId = applyForLoan(LoanRequestBuilders
                    .applyProgressiveLoan(clientId, productId, DISBURSEMENT_DATE, PRINCIPAL, 4, YEARLY_INTEREST_RATE).charges(charges));
            approveLoan(loanId, LoanRequestBuilders.approveLoan(PRINCIPAL, DISBURSEMENT_DATE));
            disburseLoan(loanId, LoanRequestBuilders.disburseLoan(PRINCIPAL, DISBURSEMENT_DATE));
            loanIdHolder[0] = loanId;
        });
        return loanIdHolder[0];
    }

    private Long createDisbursedLoanWithAPercentageInstallmentFee() {
        final Long[] chargeIdHolder = new Long[1];
        runAt(DISBURSEMENT_DATE,
                () -> chargeIdHolder[0] = chargesHelper.createCharge(ChargeRequestBuilders
                        .loanInstallmentCharge(ChargeCalculationType.PERCENT_OF_AMOUNT_AND_INTEREST, 1.0, false).currencyCode("EUR"))
                        .getResourceId());
        // No accounting until FINERACT-3314 is fixed: with periodic accrual, foreclosing a loan with an installment fee
        // fails before the foreclosure is processed
        return createDisbursedLoan(
                fourInstallmentsProgressiveWithAdvancedAllocation().accountingRule(ACCOUNTING_NONE).enableAccrualActivityPosting(null),
                List.of(new PostLoansRequestChargeData().chargeId(chargeIdHolder[0]).amount(BigDecimal.valueOf(1.0))));
    }

    /**
     * The fees of the first two installments, and 1% of the third one once it holds the remaining principal and its
     * interest is prorated to the termination date (14 of its 31 days).
     */
    private static BigDecimal expectedFeeAfterAMidPeriodTermination(final GetLoansLoanIdResponse loan) {
        final BigDecimal proratedInterest = interestDueOfPeriod(loan, 3).multiply(BigDecimal.valueOf(14)).divide(BigDecimal.valueOf(31), 2,
                RoundingMode.HALF_EVEN);
        final BigDecimal remainingPrincipal = repaymentPeriods(loan).stream().filter(period -> period.getPeriod() >= 3)
                .map(GetLoansLoanIdRepaymentPeriod::getPrincipalDue).reduce(BigDecimal.ZERO, BigDecimal::add);
        return repaymentPeriods(loan).stream().filter(period -> period.getPeriod() <= 2)
                .map(GetLoansLoanIdRepaymentPeriod::getFeeChargesDue).reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(remainingPrincipal.add(proratedInterest).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN));
    }

    private static PostLoansLoanIdTransactionsRequest foreclosure(final String date) {
        return new PostLoansLoanIdTransactionsRequest().transactionDate(date).dateFormat(LoanTestData.DATETIME_PATTERN)
                .locale(LoanTestData.LOCALE);
    }

    private GetLoansLoanIdTransactionsTemplateResponse foreclosureTemplate(final Long loanId, final String date) {
        return transactionHelper.retrieveTransactionTemplate(loanId, "foreclosure", LoanTestData.DATETIME_PATTERN, date,
                LoanTestData.LOCALE);
    }

    private void verifyClosedWithoutOverpayment(final GetLoansLoanIdResponse loan) {
        verifyLoanStatus(loan, GetLoansLoanIdStatus::getClosedObligationsMet);
        assertAmount(BigDecimal.ZERO, loan.getSummary().getTotalOutstanding());
        assertAmount(BigDecimal.ZERO, Objects.requireNonNullElse(loan.getTotalOverpaid(), BigDecimal.ZERO));
    }

    private static GetLoansLoanIdTransactions foreclosureRepayment(final GetLoansLoanIdResponse loan, final LocalDate date) {
        final List<GetLoansLoanIdTransactions> repayments = loan.getTransactions().stream()
                .filter(tx -> Boolean.TRUE.equals(tx.getType().getRepayment()) && date.equals(tx.getDate())
                        && !Boolean.TRUE.equals(tx.getManuallyReversed()))
                .toList();
        assertEquals(1, repayments.size(), "one foreclosure repayment on " + date);
        return repayments.getFirst();
    }

    private static List<GetLoansLoanIdRepaymentPeriod> repaymentPeriods(final GetLoansLoanIdResponse loan) {
        return loan.getRepaymentSchedule().getPeriods().stream().filter(period -> period.getPeriod() != null).toList();
    }

    private static BigDecimal interestDueOfPeriod(final GetLoansLoanIdResponse loan, final int periodNumber) {
        return repaymentPeriods(loan).stream().filter(period -> period.getPeriod() == periodNumber)
                .map(period -> Objects.requireNonNullElse(period.getInterestDue(), BigDecimal.ZERO)).findFirst().orElseThrow();
    }

    private static BigDecimal interestDueOfPeriods(final GetLoansLoanIdResponse loan, final int numberOfPeriods) {
        return repaymentPeriods(loan).stream().filter(period -> period.getPeriod() <= numberOfPeriods)
                .map(period -> Objects.requireNonNullElse(period.getInterestDue(), BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void verifyInterestBookedMatchesInterestCharged(final Long loanId, final GetLoansLoanIdResponse loan,
            final LocalDate foreclosureDate) {
        final BigDecimal interestCharged = loan.getSummary().getInterestCharged();
        final BigDecimal accrued = sumInterestPortion(loan, "loanTransactionType.accrual")
                .subtract(sumInterestPortion(loan, "loanTransactionType.accrualAdjustment"));
        assertTrue(accrued.signum() > 0, "the foreclosure books the accrued interest");
        assertAmount(interestCharged, accrued);
        // Periodic accrual accounting: the accruals credit interest income, and the repayment clears the receivable
        assertEquals(interestCharged.doubleValue(),
                interimJournalBalance(loanId, getIncomeAccountId("interestIncome"), foreclosureDate, false), 0.001, "interest income");
        assertEquals(0.0, interimJournalBalance(loanId, getAssetAccountId("interestReceivable"), foreclosureDate, true), 0.001,
                "interest receivable");
    }

    private static BigDecimal sumInterestPortion(final GetLoansLoanIdResponse loan, final String transactionTypeCode) {
        return loan.getTransactions().stream()
                .filter(tx -> transactionTypeCode.equals(tx.getType().getCode()) && !Boolean.TRUE.equals(tx.getManuallyReversed()))
                .map(tx -> Objects.requireNonNullElse(tx.getInterestPortion(), BigDecimal.ZERO)).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal amount(final Double templateAmount) {
        return BigDecimal.valueOf(Objects.requireNonNull(templateAmount));
    }

    private static void assertAmount(final BigDecimal expected, final BigDecimal actual) {
        assertNotNull(actual);
        assertEquals(0, expected.compareTo(actual), () -> "expected " + expected + " but was " + actual);
    }
}
