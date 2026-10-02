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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansRepaymentSchedulePeriods;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.models.PutLoansLoanIdRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.junit.jupiter.api.Test;

/**
 * A semi-monthly loan is repaid twice every calendar month. Only the first due day is configured, between 1 and 15; the
 * second one is derived from it, so the two days can never be out of order or duplicated.
 */
public class LoanSemiMonthlyRepaymentScheduleTest extends FeignLoanTestBase {

    private static final Integer SEMI_MONTHLY = 6;
    private static final String CLIENT_ACTIVATION_DATE = "01 December 2022";
    private static final String PRINCIPAL = "12000";
    private static final String PRODUCT_DAY_OUT_OF_RANGE = "validation.msg.loanproduct.firstRepaymentDayOfMonth.is.not.within.expected.range";
    private static final String PRODUCT_DAY_MISSING = "validation.msg.loanproduct.firstRepaymentDayOfMonth.cannot.be.blank";
    private static final String PRODUCT_DAY_NOT_SEMI_MONTHLY = "validation.msg.loanproduct.firstRepaymentDayOfMonth.supported.only.for.semi.monthly.repayment.frequency";
    private static final String PRODUCT_REPAYMENT_EVERY_NOT_ONE = "validation.msg.loanproduct.repaymentEvery.must.be.one.for.semi.monthly.repayment.frequency";
    private static final String LOAN_DAY_OUT_OF_RANGE = "validation.msg.loan.firstRepaymentDayOfMonth.is.not.within.expected.range";
    private static final String LOAN_DAY_NOT_SEMI_MONTHLY = "validation.msg.loan.firstRepaymentDayOfMonth.supported.only.for.semi.monthly.repayment.frequency";
    private static final String LOAN_DAY_MISSING = "validation.msg.loan.firstRepaymentDayOfMonth.cannot.be.blank";
    private static final String FIRST_REPAYMENT_NOT_A_DUE_DAY = "error.msg.loan.application.first.repayment.date.not.a.semi.monthly.due.day";

    // --- Schedule generation -------------------------------------------------------------------------------------

    @Test
    public void theUpperAnchorAlternatesBetweenTheFifteenthAndTheLastDayOfTheMonth() {
        final Long loanId = applyForSemiMonthlyLoan(15, "05 January 2023", 8);

        verifyDueDates(loanId, List.of(//
                LocalDate.of(2023, 1, 15), //
                LocalDate.of(2023, 1, 31), //
                LocalDate.of(2023, 2, 15), //
                LocalDate.of(2023, 2, 28), //
                LocalDate.of(2023, 3, 15), //
                LocalDate.of(2023, 3, 31), //
                LocalDate.of(2023, 4, 15), //
                LocalDate.of(2023, 4, 30)));
    }

    @Test
    public void theLowerAnchorPairsTheFirstWithTheFifteenth() {
        final Long loanId = applyForSemiMonthlyLoan(1, "28 December 2022", 6);

        verifyDueDates(loanId, List.of(//
                LocalDate.of(2023, 1, 1), //
                LocalDate.of(2023, 1, 15), //
                LocalDate.of(2023, 2, 1), //
                LocalDate.of(2023, 2, 15), //
                LocalDate.of(2023, 3, 1), //
                LocalDate.of(2023, 3, 15)));
    }

    @Test
    public void aDayInBetweenPairsWithItselfPlusFifteenDays() {
        final Long loanId = applyForSemiMonthlyLoan(10, "02 January 2023", 6);

        verifyDueDates(loanId, List.of(//
                LocalDate.of(2023, 1, 10), //
                LocalDate.of(2023, 1, 25), //
                LocalDate.of(2023, 2, 10), //
                LocalDate.of(2023, 2, 25), //
                LocalDate.of(2023, 3, 10), //
                LocalDate.of(2023, 3, 25)));
    }

    /**
     * The 29th does not exist in a non leap February, so the second due day falls back to the last day of the month.
     */
    @Test
    public void theDerivedDayNeverExceedsTheLengthOfTheMonth() {
        final Long loanId = applyForSemiMonthlyLoan(14, "02 February 2023", 4);

        verifyDueDates(loanId, List.of(//
                LocalDate.of(2023, 2, 14), //
                LocalDate.of(2023, 2, 28), //
                LocalDate.of(2023, 3, 14), //
                LocalDate.of(2023, 3, 29)));
    }

    /** A leap February does have a 29th, so no fallback applies. */
    @Test
    public void theDerivedDayUsesTheTwentyNinthOfALeapFebruary() {
        final Long loanId = applyForSemiMonthlyLoan(14, "02 February 2024", 2);

        verifyDueDates(loanId, List.of(//
                LocalDate.of(2024, 2, 14), //
                LocalDate.of(2024, 2, 29)));
    }

    @Test
    public void aDisbursementAfterBothDueDaysStartsOnTheFollowingMonth() {
        final Long loanId = applyForSemiMonthlyLoan(1, "20 January 2023", 4);

        verifyDueDates(loanId, List.of(//
                LocalDate.of(2023, 2, 1), //
                LocalDate.of(2023, 2, 15), //
                LocalDate.of(2023, 3, 1), //
                LocalDate.of(2023, 3, 15)));
    }

    @Test
    public void aYearOfRepaymentsHoldsTwentyFourInstallments() {
        final Long loanId = applyForSemiMonthlyLoan(15, "02 January 2023", 24);

        final List<GetLoansLoanIdRepaymentPeriod> periods = repaymentPeriods(loanId);
        assertEquals(LocalDate.of(2023, 1, 15), periods.get(1).getDueDate(), "First due date");
        assertEquals(LocalDate.of(2023, 12, 31), periods.get(24).getDueDate(), "Twenty fourth due date closes the year");
    }

    // --- Configuration -------------------------------------------------------------------------------------------

    @Test
    public void theLoanOverridesTheDayConfiguredOnTheProduct() {
        final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
        final Long productId = createSemiMonthlyProduct(15);

        final Long loanId = applyForLoan(semiMonthlyApplication(clientId, productId, "02 January 2023", 4).firstRepaymentDayOfMonth(10));

        verifyDueDates(loanId, List.of(//
                LocalDate.of(2023, 1, 10), //
                LocalDate.of(2023, 1, 25), //
                LocalDate.of(2023, 2, 10), //
                LocalDate.of(2023, 2, 25)));
    }

    @Test
    public void theProductAndTheLoanBothReturnTheConfiguredDay() {
        final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
        final Long productId = createSemiMonthlyProduct(15);

        final GetLoanProductsProductIdResponse product = retrieveLoanProduct(productId);
        assertEquals(15, product.getFirstRepaymentDayOfMonth(), "The product returns the day it was created with");
        assertEquals(SEMI_MONTHLY.longValue(), product.getRepaymentFrequencyType().getId(), "The product is semi-monthly");

        final Long loanId = applyForLoan(semiMonthlyApplication(clientId, productId, "02 January 2023", 4));
        assertEquals(15, getLoanDetails(loanId).getFirstRepaymentDayOfMonth(), "The loan inherits the day from the product");
    }

    /**
     * The schedule preview (command calculateLoanSchedule) is what the web app calls before submitting, and it runs its
     * own parameter whitelist, so the loan-level day has to be accepted there as well.
     */
    @Test
    public void theSchedulePreviewAcceptsTheFirstDueDayOfTheLoan() {
        final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
        final Long productId = createSemiMonthlyProduct(15);

        final PostLoansResponse preview = calculateLoanSchedule(
                semiMonthlyApplication(clientId, productId, "02 January 2023", 4).firstRepaymentDayOfMonth(10));

        final List<LocalDate> dueDates = preview.getPeriods().stream().filter(period -> period.getPeriod() != null)
                .map(PostLoansRepaymentSchedulePeriods::getDueDate).sorted().toList();
        assertEquals(List.of(LocalDate.of(2023, 1, 10), LocalDate.of(2023, 1, 25), LocalDate.of(2023, 2, 10), LocalDate.of(2023, 2, 25)),
                dueDates);
    }

    /** A first repayment date given by the caller is kept when it is one of the configured due days. */
    @Test
    public void aFirstRepaymentDateOnADueDayIsKept() {
        final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
        final Long productId = createSemiMonthlyProduct(15);
        final Long loanId = applyForLoan(
                semiMonthlyApplication(clientId, productId, "02 January 2023", 4).repaymentsStartingFromDate("31 January 2023"));

        verifyDueDates(loanId, List.of(//
                LocalDate.of(2023, 1, 31), //
                LocalDate.of(2023, 2, 15), //
                LocalDate.of(2023, 2, 28), //
                LocalDate.of(2023, 3, 15)));
    }

    /**
     * The minimum gap pushes the first repayment past the 15th (10 January + 10 days = 20 January), so it lands on the
     * next due day, the 31st, rather than on the 20th.
     */
    @Test
    public void theMinimumGapMovesTheFirstRepaymentToTheNextDueDay() {
        final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
        final Long productId = createLoanProduct(new LoanProductTestBuilder().withPrincipal(PRINCIPAL).withRepaymentAfterEvery("1")
                .withNumberOfRepayments("24").withRepaymentTypeAsSemiMonthly(15).withInterestRateFrequencyTypeAsMonths()
                .withMinimumDaysBetweenDisbursalAndFirstRepayment("10").buildRequest(null));
        final Long loanId = applyForLoan(semiMonthlyApplication(clientId, productId, "10 January 2023", 3));

        verifyDueDates(loanId, List.of(//
                LocalDate.of(2023, 1, 31), //
                LocalDate.of(2023, 2, 15), //
                LocalDate.of(2023, 2, 28)));
    }

    /** A modification that does not resend the day keeps the one the loan has, not the product's. */
    @Test
    public void aModificationWithoutTheDayKeepsTheLoanOverride() {
        final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
        final Long productId = createSemiMonthlyProduct(15);
        final Long loanId = applyForLoan(semiMonthlyApplication(clientId, productId, "02 January 2023", 4).firstRepaymentDayOfMonth(10));

        modifyLoanApplication(loanId, "modify", semiMonthlyModification(clientId, productId).principal(10000L));

        assertEquals(10, getLoanDetails(loanId).getFirstRepaymentDayOfMonth(), "The loan keeps its own day");
        verifyDueDates(loanId, List.of(//
                LocalDate.of(2023, 1, 10), //
                LocalDate.of(2023, 1, 25), //
                LocalDate.of(2023, 2, 10), //
                LocalDate.of(2023, 2, 25)));
    }

    @Test
    public void aModificationWithANewDayRegeneratesTheSchedule() {
        final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
        final Long productId = createSemiMonthlyProduct(15);
        final Long loanId = applyForLoan(semiMonthlyApplication(clientId, productId, "02 January 2023", 4));

        modifyLoanApplication(loanId, "modify", semiMonthlyModification(clientId, productId).firstRepaymentDayOfMonth(10));

        assertEquals(10, getLoanDetails(loanId).getFirstRepaymentDayOfMonth(), "The loan takes the new day");
        verifyDueDates(loanId, List.of(//
                LocalDate.of(2023, 1, 10), // no first repayment date was pinned, so it is derived again from the new
                                           // day
                LocalDate.of(2023, 1, 25), //
                LocalDate.of(2023, 2, 10), //
                LocalDate.of(2023, 2, 25)));
    }

    /**
     * A progressive loan is semi-monthly too. The product uses 30-day months and a 360-day year, so every period counts
     * as 15 days and 24% a year is 1% per period, whatever the period's actual length.
     */
    @Test
    public void aProgressiveLoanIsRepaidOnTheSemiMonthlyDueDays() {
        final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
        final Long productId = createLoanProduct(fourInstallmentsProgressiveWithAdvancedAllocation()
                .repaymentFrequencyType(SEMI_MONTHLY.longValue()).firstRepaymentDayOfMonth(15).interestRatePerPeriod(24.0));
        final Long loanId = applyForLoan(LoanRequestBuilders.applyProgressiveLoan(clientId, productId, "15 January 2023", 1000.0, 4, 24.0)
                .loanTermFrequencyType(SEMI_MONTHLY).repaymentFrequencyType(SEMI_MONTHLY));

        verifyDueDates(loanId, List.of(//
                LocalDate.of(2023, 1, 31), //
                LocalDate.of(2023, 2, 15), //
                LocalDate.of(2023, 2, 28), //
                LocalDate.of(2023, 3, 15)));
        final List<GetLoansLoanIdRepaymentPeriod> periods = repaymentPeriods(loanId);
        assertEquals(0, new BigDecimal("10.00").compareTo(periods.get(1).getInterestDue()), "Interest of the first period");
        assertEquals(0, new BigDecimal("256.28").compareTo(periods.get(1).getTotalDueForPeriod()), "Installment of the first period");
        assertEquals(0, new BigDecimal("2.54").compareTo(periods.get(4).getInterestDue()), "Interest of the last period");
    }

    /**
     * With interest recalculation on and rest dates "same as repayment", an on-time repayment leaves the semi-monthly
     * schedule where it was: same due dates and the same interest on the next installment. Interest recalculation needs
     * daily interest calculation (or partial periods), on the product and on the loan alike.
     */
    @Test
    public void anOnTimeRepaymentKeepsTheScheduleOfALoanWithInterestRecalculation() {
        final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
        final Long productId = createLoanProduct(new LoanProductTestBuilder().withPrincipal(PRINCIPAL).withRepaymentAfterEvery("1")
                .withNumberOfRepayments("24").withRepaymentTypeAsSemiMonthly(15).withinterestRatePerPeriod("2")
                .withInterestRateFrequencyTypeAsMonths().withInterestTypeAsDecliningBalance().withInterestCalculationPeriodTypeAsDays()
                .withInterestRecalculationDetails(LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_NONE,
                        LoanProductTestBuilder.RECALCULATION_STRATEGY_REDUCE_EMI_AMOUN,
                        LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE)
                .withInterestRecalculationRestFrequencyDetails(LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_SAME_AS_REPAYMENT_PERIOD,
                        "1", null, null)
                .withInterestRecalculationCompoundingFrequencyDetails(null, null, null, null).buildRequest(null));

        runAt("31 January 2023", () -> {
            final Long loanId = applyForLoan(semiMonthlyApplication(clientId, productId, "15 January 2023", 4)
                    .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.DAILY));
            approveLoan(loanId, approveLoanRequest(12000.0, "15 January 2023"));
            disburseLoan(loanId, new BigDecimal(PRINCIPAL), "15 January 2023");
            final List<GetLoansLoanIdRepaymentPeriod> before = repaymentPeriods(loanId);

            addRepaymentForLoan(loanId, before.get(1).getTotalDueForPeriod().doubleValue(), "31 January 2023");

            verifyDueDates(loanId, List.of(//
                    LocalDate.of(2023, 1, 31), //
                    LocalDate.of(2023, 2, 15), //
                    LocalDate.of(2023, 2, 28), //
                    LocalDate.of(2023, 3, 15)));
            final List<GetLoansLoanIdRepaymentPeriod> after = repaymentPeriods(loanId);
            assertEquals(0, before.get(2).getInterestDue().compareTo(after.get(2).getInterestDue()),
                    "Interest of the second installment after an on-time repayment");
        });
    }

    // --- Negative cases ------------------------------------------------------------------------------------------

    @Test
    public void aProductCannotBeCreatedWithADayBeyondTheFifteenth() {
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> createLoanProduct(semiMonthlyProductRequest(16)));

        assertEquals(400, exception.getStatus());
        assertErrorGlobalisationCode(exception, PRODUCT_DAY_OUT_OF_RANGE);
    }

    @Test
    public void aProductCannotBeCreatedWithADayBelowTheFirst() {
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> createLoanProduct(semiMonthlyProductRequest(0)));

        assertEquals(400, exception.getStatus());
        assertErrorGlobalisationCode(exception, PRODUCT_DAY_OUT_OF_RANGE);
    }

    @Test
    public void aSemiMonthlyProductNeedsTheFirstDueDay() {
        final PostLoanProductsRequest request = semiMonthlyProductRequest(15).firstRepaymentDayOfMonth(null);

        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class, () -> createLoanProduct(request));

        assertEquals(400, exception.getStatus());
        assertErrorGlobalisationCode(exception, PRODUCT_DAY_MISSING);
    }

    @Test
    public void theFirstDueDayIsRejectedOnAMonthlyProduct() {
        final PostLoanProductsRequest request = new LoanProductTestBuilder().withPrincipal(PRINCIPAL).withRepaymentAfterEvery("1")
                .withNumberOfRepayments("6").withRepaymentTypeAsMonth().withInterestRateFrequencyTypeAsMonths()
                .withFirstRepaymentDayOfMonth(15).buildRequest(null);

        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class, () -> createLoanProduct(request));

        assertEquals(400, exception.getStatus());
        assertErrorGlobalisationCode(exception, PRODUCT_DAY_NOT_SEMI_MONTHLY);
    }

    /** Two repayments a month is the whole point of the frequency, so it cannot repeat on a multiple of its period. */
    @Test
    public void aSemiMonthlyProductCannotRepeatEveryTwoPeriods() {
        final PostLoanProductsRequest request = semiMonthlyProductRequest(15).repaymentEvery(2);

        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class, () -> createLoanProduct(request));

        assertEquals(400, exception.getStatus());
        assertErrorGlobalisationCode(exception, PRODUCT_REPAYMENT_EVERY_NOT_ONE);
    }

    @Test
    public void aLoanCannotOverrideTheDayBeyondTheFifteenth() {
        final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
        final Long productId = createSemiMonthlyProduct(15);
        final PostLoansRequest request = semiMonthlyApplication(clientId, productId, "02 January 2023", 4).firstRepaymentDayOfMonth(20);

        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class, () -> applyForLoan(request));

        assertEquals(400, exception.getStatus());
        assertErrorGlobalisationCode(exception, LOAN_DAY_OUT_OF_RANGE);
    }

    @Test
    public void aMonthlyLoanCannotCarryTheFirstDueDay() {
        final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
        final Long productId = createLoanProduct(new LoanProductTestBuilder().withPrincipal(PRINCIPAL).withRepaymentAfterEvery("1")
                .withNumberOfRepayments("6").withRepaymentTypeAsMonth().withInterestRateFrequencyTypeAsMonths().buildRequest(null));
        final PostLoansRequest request = LoanRequestBuilders
                .legacyIndividualApplication(clientId, productId, PRINCIPAL, 6, BigDecimal.valueOf(2), "02 January 2023")
                .firstRepaymentDayOfMonth(15);

        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class, () -> applyForLoan(request));

        assertEquals(400, exception.getStatus());
        assertErrorGlobalisationCode(exception, LOAN_DAY_NOT_SEMI_MONTHLY);
    }

    /** The day would otherwise fall back silently to the 15th, a pair nobody configured. */
    @Test
    public void aLoanOnAMonthlyProductNeedsTheFirstDueDayToBeSemiMonthly() {
        final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
        final Long productId = createLoanProduct(new LoanProductTestBuilder().withPrincipal(PRINCIPAL).withRepaymentAfterEvery("1")
                .withNumberOfRepayments("6").withRepaymentTypeAsMonth().withInterestRateFrequencyTypeAsMonths().buildRequest(null));
        final PostLoansRequest request = semiMonthlyApplication(clientId, productId, "02 January 2023", 4);

        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class, () -> applyForLoan(request));

        assertEquals(400, exception.getStatus());
        assertErrorGlobalisationCode(exception, LOAN_DAY_MISSING);
    }

    @Test
    public void aFirstRepaymentDateOffTheDueDaysIsRejected() {
        final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
        final Long productId = createSemiMonthlyProduct(15);
        final PostLoansRequest request = semiMonthlyApplication(clientId, productId, "02 January 2023", 4)
                .repaymentsStartingFromDate("20 January 2023");

        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class, () -> applyForLoan(request));

        assertEquals(403, exception.getStatus());
        assertErrorGlobalisationCode(exception, FIRST_REPAYMENT_NOT_A_DUE_DAY);
    }

    // --- Helpers -------------------------------------------------------------------------------------------------

    private Long applyForSemiMonthlyLoan(final int firstRepaymentDayOfMonth, final String disbursementDate, final int numberOfRepayments) {
        final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
        final Long productId = createSemiMonthlyProduct(firstRepaymentDayOfMonth);
        return applyForLoan(semiMonthlyApplication(clientId, productId, disbursementDate, numberOfRepayments));
    }

    private PostLoansRequest semiMonthlyApplication(final Long clientId, final Long productId, final String disbursementDate,
            final int numberOfRepayments) {
        return LoanRequestBuilders
                .legacyIndividualApplication(clientId, productId, PRINCIPAL, numberOfRepayments, BigDecimal.valueOf(2), disbursementDate)
                .loanTermFrequencyType(SEMI_MONTHLY)//
                .repaymentFrequencyType(SEMI_MONTHLY);
    }

    private PutLoansLoanIdRequest semiMonthlyModification(final Long clientId, final Long productId) {
        return new PutLoansLoanIdRequest().clientId(clientId).productId(productId).loanType("individual").locale("en")
                .dateFormat("dd MMMM yyyy");
    }

    private Long createSemiMonthlyProduct(final int firstRepaymentDayOfMonth) {
        return createLoanProduct(semiMonthlyProductRequest(firstRepaymentDayOfMonth));
    }

    private PostLoanProductsRequest semiMonthlyProductRequest(final int firstRepaymentDayOfMonth) {
        return new LoanProductTestBuilder().withPrincipal(PRINCIPAL).withRepaymentAfterEvery("1").withNumberOfRepayments("24")
                .withRepaymentTypeAsSemiMonthly(firstRepaymentDayOfMonth).withInterestRateFrequencyTypeAsMonths().buildRequest(null);
    }

    private void verifyDueDates(final Long loanId, final List<LocalDate> expectedDueDates) {
        final List<GetLoansLoanIdRepaymentPeriod> periods = repaymentPeriods(loanId);
        assertEquals(expectedDueDates.size() + 1, periods.size(), "Checking the number of repayment periods");
        for (int installment = 1; installment <= expectedDueDates.size(); installment++) {
            assertEquals(expectedDueDates.get(installment - 1), periods.get(installment).getDueDate(),
                    "Checking the due date of installment " + installment);
        }
    }

    private List<GetLoansLoanIdRepaymentPeriod> repaymentPeriods(final Long loanId) {
        final GetLoansLoanIdResponse loan = getLoanDetails(loanId);
        return loan.getRepaymentSchedule().getPeriods();
    }
}
