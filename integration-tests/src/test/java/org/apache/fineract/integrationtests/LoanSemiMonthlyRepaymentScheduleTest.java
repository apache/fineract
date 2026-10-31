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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansRepaymentSchedulePeriods;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.models.PutLoanProductsProductIdRequest;
import org.apache.fineract.client.models.PutLoansLoanIdRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.junit.jupiter.api.Test;

/**
 * A semi-monthly loan is repaid twice every calendar month, on two configured days: the first one between 1 and 27 and
 * the second one between 2 and 31 and after the first. A second day the month lacks falls on its last day, so 31 is the
 * last day of every month.
 */
public class LoanSemiMonthlyRepaymentScheduleTest extends FeignLoanTestBase {

    private static final Integer MONTHLY = 2;
    private static final Integer SEMI_MONTHLY = 6;
    private static final String CLIENT_ACTIVATION_DATE = "01 December 2022";
    private static final String PRINCIPAL = "12000";
    private static final String PRODUCT_FIRST_DAY_OUT_OF_RANGE = "validation.msg.loanproduct.firstRepaymentDayOfMonth.is.not.within.expected.range";
    private static final String PRODUCT_SECOND_DAY_OUT_OF_RANGE = "validation.msg.loanproduct.secondRepaymentDayOfMonth.is.not.within.expected.range";
    private static final String PRODUCT_SECOND_DAY_NOT_AFTER_FIRST = "validation.msg.loanproduct.secondRepaymentDayOfMonth.must.be.greater.than.first.repayment.day.of.month";
    private static final String PRODUCT_FIRST_DAY_MISSING = "validation.msg.loanproduct.firstRepaymentDayOfMonth.cannot.be.blank";
    private static final String PRODUCT_SECOND_DAY_MISSING = "validation.msg.loanproduct.secondRepaymentDayOfMonth.cannot.be.blank";
    private static final String PRODUCT_DAY_NOT_SEMI_MONTHLY = "validation.msg.loanproduct.firstRepaymentDayOfMonth.supported.only.for.semi.monthly.repayment.frequency";
    private static final String PRODUCT_REPAYMENT_EVERY_NOT_ONE = "validation.msg.loanproduct.repaymentEvery.must.be.one.for.semi.monthly.repayment.frequency";
    private static final String LOAN_FIRST_DAY_OUT_OF_RANGE = "validation.msg.loan.firstRepaymentDayOfMonth.is.not.within.expected.range";
    private static final String LOAN_SECOND_DAY_NOT_AFTER_FIRST = "validation.msg.loan.secondRepaymentDayOfMonth.must.be.greater.than.first.repayment.day.of.month";
    private static final String LOAN_DAY_NOT_SEMI_MONTHLY = "validation.msg.loan.firstRepaymentDayOfMonth.supported.only.for.semi.monthly.repayment.frequency";
    private static final String LOAN_FIRST_DAY_MISSING = "validation.msg.loan.firstRepaymentDayOfMonth.cannot.be.blank";
    private static final String LOAN_SECOND_DAY_MISSING = "validation.msg.loan.secondRepaymentDayOfMonth.cannot.be.blank";
    private static final String LOAN_REPAYMENT_EVERY_NOT_ONE = "validation.msg.loan.repaymentEvery.must.be.one.for.semi.monthly.repayment.frequency";
    private static final String FIRST_REPAYMENT_NOT_A_DUE_DAY = "error.msg.loan.application.first.repayment.date.not.a.semi.monthly.due.day";
    private static final String REAGE_FREQUENCY_NOT_SUPPORTED = "validation.msg.loan.reAge.frequencyType.is.not.one.of.expected.enumerations";
    /**
     * The second and the third installment of a re-age on 20 February 2023 into three monthly installments from 15
     * March.
     */
    private static final List<LocalDate> LATER_RE_AGED_DUE_DATES = List.of(LocalDate.of(2023, 4, 15), LocalDate.of(2023, 5, 15));

    // --- Schedule generation -------------------------------------------------------------------------------------

    @Test
    public void theFifteenthAndTheLastDayOfTheMonthAlternate() {
        final Long loanId = applyForSemiMonthlyLoan(15, 31, "05 January 2023", 8);

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
    public void theFirstAndTheFifteenthAlternate() {
        final Long loanId = applyForSemiMonthlyLoan(1, 15, "28 December 2022", 6);

        verifyDueDates(loanId, List.of(//
                LocalDate.of(2023, 1, 1), //
                LocalDate.of(2023, 1, 15), //
                LocalDate.of(2023, 2, 1), //
                LocalDate.of(2023, 2, 15), //
                LocalDate.of(2023, 3, 1), //
                LocalDate.of(2023, 3, 15)));
    }

    @Test
    public void theTenthAndTheTwentyFifthAlternate() {
        final Long loanId = applyForSemiMonthlyLoan(10, 25, "02 January 2023", 6);

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
    public void aSecondDayTheMonthLacksFallsOnItsLastDay() {
        final Long loanId = applyForSemiMonthlyLoan(14, 29, "02 February 2023", 4);

        verifyDueDates(loanId, List.of(//
                LocalDate.of(2023, 2, 14), //
                LocalDate.of(2023, 2, 28), //
                LocalDate.of(2023, 3, 14), //
                LocalDate.of(2023, 3, 29)));
    }

    /** A leap February does have a 29th, so no fallback applies. */
    @Test
    public void theSecondDayUsesTheTwentyNinthOfALeapFebruary() {
        final Long loanId = applyForSemiMonthlyLoan(14, 29, "02 February 2024", 2);

        verifyDueDates(loanId, List.of(//
                LocalDate.of(2024, 2, 14), //
                LocalDate.of(2024, 2, 29)));
    }

    /** The two days do not have to be half a month apart; a payroll on the 1st and the 25th is kept as it is. */
    @Test
    public void unevenlySpreadDaysAreKeptAsConfigured() {
        final Long loanId = applyForSemiMonthlyLoan(1, 25, "05 January 2023", 4);

        verifyDueDates(loanId, List.of(//
                LocalDate.of(2023, 1, 25), //
                LocalDate.of(2023, 2, 1), //
                LocalDate.of(2023, 2, 25), //
                LocalDate.of(2023, 3, 1)));
    }

    @Test
    public void daysTenDaysApartAreKeptAsConfigured() {
        final Long loanId = applyForSemiMonthlyLoan(10, 20, "02 January 2023", 4);

        verifyDueDates(loanId, List.of(//
                LocalDate.of(2023, 1, 10), //
                LocalDate.of(2023, 1, 20), //
                LocalDate.of(2023, 2, 10), //
                LocalDate.of(2023, 2, 20)));
    }

    /** The highest first day with the last day of the month: the two due days stay apart even in February. */
    @Test
    public void theTwentySeventhAndTheLastDayNeverMeet() {
        final Long loanId = applyForSemiMonthlyLoan(27, 31, "02 February 2023", 4);

        verifyDueDates(loanId, List.of(//
                LocalDate.of(2023, 2, 27), //
                LocalDate.of(2023, 2, 28), //
                LocalDate.of(2023, 3, 27), //
                LocalDate.of(2023, 3, 31)));
    }

    @Test
    public void aDisbursementAfterBothDueDaysStartsOnTheFollowingMonth() {
        final Long loanId = applyForSemiMonthlyLoan(1, 15, "20 January 2023", 4);

        verifyDueDates(loanId, List.of(//
                LocalDate.of(2023, 2, 1), //
                LocalDate.of(2023, 2, 15), //
                LocalDate.of(2023, 3, 1), //
                LocalDate.of(2023, 3, 15)));
    }

    @Test
    public void aYearOfRepaymentsHoldsTwentyFourInstallments() {
        final Long loanId = applyForSemiMonthlyLoan(15, 31, "02 January 2023", 24);

        final List<GetLoansLoanIdRepaymentPeriod> periods = repaymentPeriods(loanId);
        assertEquals(LocalDate.of(2023, 1, 15), periods.get(1).getDueDate(), "First due date");
        assertEquals(LocalDate.of(2023, 12, 31), periods.get(24).getDueDate(), "Twenty fourth due date closes the year");
    }

    // --- Configuration -------------------------------------------------------------------------------------------

    @Test
    public void theLoanOverridesTheDaysConfiguredOnTheProduct() {
        final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
        final Long productId = createSemiMonthlyProduct(15, 31);

        final Long loanId = applyForLoan(semiMonthlyApplication(clientId, productId, "02 January 2023", 4).firstRepaymentDayOfMonth(5)
                .secondRepaymentDayOfMonth(20));

        verifyDueDates(loanId, List.of(//
                LocalDate.of(2023, 1, 5), //
                LocalDate.of(2023, 1, 20), //
                LocalDate.of(2023, 2, 5), //
                LocalDate.of(2023, 2, 20)));
    }

    @Test
    public void theProductAndTheLoanBothReturnTheConfiguredDays() {
        final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
        final Long productId = createSemiMonthlyProduct(1, 25);

        final GetLoanProductsProductIdResponse product = retrieveLoanProduct(productId);
        assertEquals(1, product.getFirstRepaymentDayOfMonth(), "The product returns the first day it was created with");
        assertEquals(25, product.getSecondRepaymentDayOfMonth(), "The product returns the second day it was created with");
        assertEquals(SEMI_MONTHLY.longValue(), product.getRepaymentFrequencyType().getId(), "The product is semi-monthly");

        final GetLoanProductsProductIdResponse productWithTemplate = retrieveLoanProductWithTemplate(productId);
        assertEquals(1, productWithTemplate.getFirstRepaymentDayOfMonth(), "The product template keeps the first day");
        assertEquals(25, productWithTemplate.getSecondRepaymentDayOfMonth(), "The product template keeps the second day");

        final Long loanId = applyForLoan(semiMonthlyApplication(clientId, productId, "02 January 2023", 4));
        final GetLoansLoanIdResponse loan = getLoanDetails(loanId);
        assertEquals(1, loan.getFirstRepaymentDayOfMonth(), "The loan inherits the first day from the product");
        assertEquals(25, loan.getSecondRepaymentDayOfMonth(), "The loan inherits the second day from the product");
    }

    /** The due days only mean something to a semi-monthly product, so switching it to monthly drops them. */
    @Test
    public void aProductSwitchedToMonthlyDropsTheDueDays() {
        final Long productId = createSemiMonthlyProduct(10, 25);

        updateLoanProduct(productId, new PutLoanProductsProductIdRequest().repaymentFrequencyType(MONTHLY).locale("en"));

        final GetLoanProductsProductIdResponse product = retrieveLoanProduct(productId);
        assertEquals(MONTHLY.longValue(), product.getRepaymentFrequencyType().getId(), "The product is monthly");
        assertNull(product.getFirstRepaymentDayOfMonth(), "The product no longer has a first day");
        assertNull(product.getSecondRepaymentDayOfMonth(), "The product no longer has a second day");
    }

    /**
     * An edit form resends every field of the product. Resending the frequency a semi-monthly product already has,
     * without the days, keeps the stored days instead of asking for them again.
     */
    @Test
    public void aProductThatStaysSemiMonthlyKeepsItsDueDaysWhenTheyAreNotResent() {
        final Long productId = createSemiMonthlyProduct(10, 25);

        updateLoanProduct(productId, new PutLoanProductsProductIdRequest().repaymentFrequencyType(SEMI_MONTHLY).locale("en"));

        final GetLoanProductsProductIdResponse product = retrieveLoanProduct(productId);
        assertEquals(SEMI_MONTHLY.longValue(), product.getRepaymentFrequencyType().getId(), "The product is still semi-monthly");
        assertEquals(10, product.getFirstRepaymentDayOfMonth(), "The product keeps its first day");
        assertEquals(25, product.getSecondRepaymentDayOfMonth(), "The product keeps its second day");
    }

    /**
     * The schedule preview (command calculateLoanSchedule) is what the web app calls before submitting, and it runs its
     * own parameter whitelist, so the loan-level days have to be accepted there as well.
     */
    @Test
    public void theSchedulePreviewAcceptsTheDueDaysOfTheLoan() {
        final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
        final Long productId = createSemiMonthlyProduct(15, 31);

        final PostLoansResponse preview = calculateLoanSchedule(semiMonthlyApplication(clientId, productId, "02 January 2023", 4)
                .firstRepaymentDayOfMonth(10).secondRepaymentDayOfMonth(25));

        final List<LocalDate> dueDates = preview.getPeriods().stream().filter(period -> period.getPeriod() != null)
                .map(PostLoansRepaymentSchedulePeriods::getDueDate).sorted().toList();
        assertEquals(List.of(LocalDate.of(2023, 1, 10), LocalDate.of(2023, 1, 25), LocalDate.of(2023, 2, 10), LocalDate.of(2023, 2, 25)),
                dueDates);
    }

    /** A first repayment date given by the caller is kept when it is one of the configured due days. */
    @Test
    public void aFirstRepaymentDateOnADueDayIsKept() {
        final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
        final Long productId = createSemiMonthlyProduct(15, 31);
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
                .withNumberOfRepayments("24").withRepaymentTypeAsSemiMonthly(15, 31).withInterestRateFrequencyTypeAsMonths()
                .withMinimumDaysBetweenDisbursalAndFirstRepayment("10").buildRequest(null));
        final Long loanId = applyForLoan(semiMonthlyApplication(clientId, productId, "10 January 2023", 3));

        verifyDueDates(loanId, List.of(//
                LocalDate.of(2023, 1, 31), //
                LocalDate.of(2023, 2, 15), //
                LocalDate.of(2023, 2, 28)));
    }

    /** A modification that does not resend the days keeps the ones the loan has, not the product's. */
    @Test
    public void aModificationWithoutTheDaysKeepsTheLoanOverride() {
        final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
        final Long productId = createSemiMonthlyProduct(15, 31);
        final Long loanId = applyForLoan(semiMonthlyApplication(clientId, productId, "02 January 2023", 4).firstRepaymentDayOfMonth(10)
                .secondRepaymentDayOfMonth(25));

        modifyLoanApplication(loanId, "modify", semiMonthlyModification(clientId, productId).principal(10000L));

        final GetLoansLoanIdResponse loan = getLoanDetails(loanId);
        assertEquals(10, loan.getFirstRepaymentDayOfMonth(), "The loan keeps its own first day");
        assertEquals(25, loan.getSecondRepaymentDayOfMonth(), "The loan keeps its own second day");
        verifyDueDates(loanId, List.of(//
                LocalDate.of(2023, 1, 10), //
                LocalDate.of(2023, 1, 25), //
                LocalDate.of(2023, 2, 10), //
                LocalDate.of(2023, 2, 25)));
    }

    @Test
    public void aModificationWithNewDaysRegeneratesTheSchedule() {
        final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
        final Long productId = createSemiMonthlyProduct(15, 31);
        final Long loanId = applyForLoan(semiMonthlyApplication(clientId, productId, "02 January 2023", 4));

        modifyLoanApplication(loanId, "modify",
                semiMonthlyModification(clientId, productId).firstRepaymentDayOfMonth(1).secondRepaymentDayOfMonth(25));

        final GetLoansLoanIdResponse loan = getLoanDetails(loanId);
        assertEquals(1, loan.getFirstRepaymentDayOfMonth(), "The loan takes the new first day");
        assertEquals(25, loan.getSecondRepaymentDayOfMonth(), "The loan takes the new second day");
        // No first repayment date was pinned, so it is derived again from the new days.
        verifyDueDates(loanId, List.of(//
                LocalDate.of(2023, 1, 25), //
                LocalDate.of(2023, 2, 1), //
                LocalDate.of(2023, 2, 25), //
                LocalDate.of(2023, 3, 1)));
    }

    /** One day alone could break the order of the pair, so a modification has to resend both. */
    @Test
    public void aModificationWithOnlyOneDayIsRejected() {
        final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
        final Long productId = createSemiMonthlyProduct(15, 31);
        final Long loanId = applyForLoan(semiMonthlyApplication(clientId, productId, "02 January 2023", 4));
        final PutLoansLoanIdRequest request = semiMonthlyModification(clientId, productId).secondRepaymentDayOfMonth(20);

        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> modifyLoanApplication(loanId, "modify", request));

        assertEquals(400, exception.getStatus());
        assertErrorGlobalisationCode(exception, LOAN_FIRST_DAY_MISSING);
    }

    /**
     * The semi-monthly rules apply to the frequency the loan ends up with, so a modification that only changes
     * repaymentEvery is rejected even though it does not resend the frequency or the days.
     */
    @Test
    public void aModificationWithRepaymentEveryOtherThanOneIsRejected() {
        final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
        final Long productId = createSemiMonthlyProduct(15, 31);
        final Long loanId = applyForLoan(semiMonthlyApplication(clientId, productId, "02 January 2023", 4));
        final PutLoansLoanIdRequest request = semiMonthlyModification(clientId, productId).repaymentEvery(2).loanTermFrequency(8);

        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> modifyLoanApplication(loanId, "modify", request));

        assertEquals(400, exception.getStatus());
        assertErrorGlobalisationCode(exception, LOAN_REPAYMENT_EVERY_NOT_ONE);
    }

    /** A modification that pins the first repayment date must pin it on one of the loan's due days. */
    @Test
    public void aModificationWithAFirstRepaymentDateOffTheDueDaysIsRejected() {
        final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
        final Long productId = createSemiMonthlyProduct(15, 31);
        final Long loanId = applyForLoan(semiMonthlyApplication(clientId, productId, "02 January 2023", 4));
        final PutLoansLoanIdRequest request = semiMonthlyModification(clientId, productId).repaymentsStartingFromDate("20 January 2023");

        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> modifyLoanApplication(loanId, "modify", request));

        assertEquals(403, exception.getStatus());
        assertErrorGlobalisationCode(exception, FIRST_REPAYMENT_NOT_A_DUE_DAY);
    }

    /** New days that leave the pinned first repayment date off the schedule are rejected too. */
    @Test
    public void aModificationWithDaysThatOrphanTheFirstRepaymentDateIsRejected() {
        final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
        final Long productId = createSemiMonthlyProduct(15, 31);
        final Long loanId = applyForLoan(
                semiMonthlyApplication(clientId, productId, "02 January 2023", 4).repaymentsStartingFromDate("15 January 2023"));
        final PutLoansLoanIdRequest request = semiMonthlyModification(clientId, productId).firstRepaymentDayOfMonth(10)
                .secondRepaymentDayOfMonth(25);

        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> modifyLoanApplication(loanId, "modify", request));

        assertEquals(403, exception.getStatus());
        assertErrorGlobalisationCode(exception, FIRST_REPAYMENT_NOT_A_DUE_DAY);
    }

    /**
     * A progressive loan is semi-monthly too. The product uses 30-day months and a 360-day year, so every period counts
     * as 15 days and 24% a year is 1% per period, whatever the period's actual length.
     */
    @Test
    public void aProgressiveLoanIsRepaidOnTheSemiMonthlyDueDays() {
        final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
        final Long productId = createLoanProduct(
                fourInstallmentsProgressiveWithAdvancedAllocation().repaymentFrequencyType(SEMI_MONTHLY.longValue())
                        .firstRepaymentDayOfMonth(15).secondRepaymentDayOfMonth(31).interestRatePerPeriod(24.0));
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
                .withNumberOfRepayments("24").withRepaymentTypeAsSemiMonthly(15, 31).withinterestRatePerPeriod("2")
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

    /**
     * Without a first repayment date the first due date is the next due day after the disbursement, which can be a day
     * away. With interest "same as repayment period" that one-day period costs a full 1/24 of the annual rate; with
     * partial periods allowed it costs the share of the regular period it covers, one day out of the fifteen between
     * the 1st and the 16th. The interest calculation period is set on the loan as well, since the progressive loan
     * request builder applies for daily interest by default.
     */
    @Test
    public void aShortFirstPeriodIsProratedOnlyWhenPartialPeriodsAreAllowed() {
        final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
        final Long wholePeriodProductId = createLoanProduct(
                fourInstallmentsProgressiveWithAdvancedAllocation().repaymentFrequencyType(SEMI_MONTHLY.longValue())
                        .firstRepaymentDayOfMonth(1).secondRepaymentDayOfMonth(16).interestRatePerPeriod(24.0));
        final Long partialPeriodProductId = createLoanProduct(fourInstallmentsProgressiveWithAdvancedAllocation()
                .repaymentFrequencyType(SEMI_MONTHLY.longValue()).firstRepaymentDayOfMonth(1).secondRepaymentDayOfMonth(16)
                .interestRatePerPeriod(24.0).allowPartialPeriodInterestCalculation(true));
        final List<LocalDate> expectedDueDates = List.of(LocalDate.of(2023, 1, 16), LocalDate.of(2023, 2, 1), LocalDate.of(2023, 2, 16),
                LocalDate.of(2023, 3, 1));

        final Long wholePeriodLoanId = applyForLoan(
                LoanRequestBuilders.applyProgressiveLoan(clientId, wholePeriodProductId, "15 January 2023", 1000.0, 4, 24.0)
                        .loanTermFrequencyType(SEMI_MONTHLY).repaymentFrequencyType(SEMI_MONTHLY)
                        .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD));
        verifyDueDates(wholePeriodLoanId, expectedDueDates);
        assertEquals(0, new BigDecimal("10.00").compareTo(repaymentPeriods(wholePeriodLoanId).get(1).getInterestDue()),
                "Interest of a one-day first period without partial periods");

        final Long partialPeriodLoanId = applyForLoan(
                LoanRequestBuilders.applyProgressiveLoan(clientId, partialPeriodProductId, "15 January 2023", 1000.0, 4, 24.0)
                        .loanTermFrequencyType(SEMI_MONTHLY).repaymentFrequencyType(SEMI_MONTHLY)
                        .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD));
        verifyDueDates(partialPeriodLoanId, expectedDueDates);
        final List<GetLoansLoanIdRepaymentPeriod> periods = repaymentPeriods(partialPeriodLoanId);
        assertEquals(0, new BigDecimal("0.67").compareTo(periods.get(1).getInterestDue()),
                "Interest of a one-day first period with partial periods: 1/15 of 1/24 of 24% on 1000");
        assertEquals(0, new BigDecimal("253.91").compareTo(periods.get(1).getTotalDueForPeriod()),
                "Installment of a loan whose first period is prorated");
        assertEquals(0, BigDecimal.ZERO.compareTo(periods.getLast().getPrincipalLoanBalanceOutstanding()),
                "Outstanding principal after the last installment");
    }

    // --- Negative cases ------------------------------------------------------------------------------------------

    /**
     * A first day of 28 could only pair with the 29th or later, which a non leap February caps to the 28th, so both due
     * days would fall on the same date.
     */
    @Test
    public void aProductCannotBeCreatedWithAFirstDayBeyondTheTwentySeventh() {
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> createLoanProduct(semiMonthlyProductRequest(28, 31)));

        assertEquals(400, exception.getStatus());
        assertErrorGlobalisationCode(exception, PRODUCT_FIRST_DAY_OUT_OF_RANGE);
    }

    @Test
    public void aProductCannotBeCreatedWithAFirstDayBelowTheFirst() {
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> createLoanProduct(semiMonthlyProductRequest(0, 15)));

        assertEquals(400, exception.getStatus());
        assertErrorGlobalisationCode(exception, PRODUCT_FIRST_DAY_OUT_OF_RANGE);
    }

    @Test
    public void aProductCannotBeCreatedWithASecondDayBeyondTheThirtyFirst() {
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> createLoanProduct(semiMonthlyProductRequest(10, 32)));

        assertEquals(400, exception.getStatus());
        assertErrorGlobalisationCode(exception, PRODUCT_SECOND_DAY_OUT_OF_RANGE);
    }

    @Test
    public void aProductCannotBeCreatedWithTheSecondDayBeforeTheFirst() {
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> createLoanProduct(semiMonthlyProductRequest(20, 10)));

        assertEquals(400, exception.getStatus());
        assertErrorGlobalisationCode(exception, PRODUCT_SECOND_DAY_NOT_AFTER_FIRST);
    }

    /** Equal days would be a single repayment a month. */
    @Test
    public void aProductCannotBeCreatedWithTheSameDayTwice() {
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> createLoanProduct(semiMonthlyProductRequest(10, 10)));

        assertEquals(400, exception.getStatus());
        assertErrorGlobalisationCode(exception, PRODUCT_SECOND_DAY_NOT_AFTER_FIRST);
    }

    @Test
    public void aSemiMonthlyProductNeedsTheDueDays() {
        final PostLoanProductsRequest request = semiMonthlyProductRequest(15, 31).firstRepaymentDayOfMonth(null)
                .secondRepaymentDayOfMonth(null);

        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class, () -> createLoanProduct(request));

        assertEquals(400, exception.getStatus());
        assertErrorGlobalisationCode(exception, PRODUCT_FIRST_DAY_MISSING);
    }

    @Test
    public void aSemiMonthlyProductNeedsTheSecondDueDay() {
        final PostLoanProductsRequest request = semiMonthlyProductRequest(15, 31).secondRepaymentDayOfMonth(null);

        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class, () -> createLoanProduct(request));

        assertEquals(400, exception.getStatus());
        assertErrorGlobalisationCode(exception, PRODUCT_SECOND_DAY_MISSING);
    }

    @Test
    public void theDueDaysAreRejectedOnAMonthlyProduct() {
        final PostLoanProductsRequest request = new LoanProductTestBuilder().withPrincipal(PRINCIPAL).withRepaymentAfterEvery("1")
                .withNumberOfRepayments("6").withRepaymentTypeAsMonth().withInterestRateFrequencyTypeAsMonths()
                .withSemiMonthlyDueDays(15, 31).buildRequest(null);

        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class, () -> createLoanProduct(request));

        assertEquals(400, exception.getStatus());
        assertErrorGlobalisationCode(exception, PRODUCT_DAY_NOT_SEMI_MONTHLY);
    }

    /** Two repayments a month is the whole point of the frequency, so it cannot repeat on a multiple of its period. */
    @Test
    public void aSemiMonthlyProductCannotRepeatEveryTwoPeriods() {
        final PostLoanProductsRequest request = semiMonthlyProductRequest(15, 31).repaymentEvery(2);

        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class, () -> createLoanProduct(request));

        assertEquals(400, exception.getStatus());
        assertErrorGlobalisationCode(exception, PRODUCT_REPAYMENT_EVERY_NOT_ONE);
    }

    @Test
    public void aLoanCannotOverrideTheFirstDayBeyondTheTwentySeventh() {
        final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
        final Long productId = createSemiMonthlyProduct(15, 31);
        final PostLoansRequest request = semiMonthlyApplication(clientId, productId, "02 January 2023", 4).firstRepaymentDayOfMonth(28)
                .secondRepaymentDayOfMonth(31);

        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class, () -> applyForLoan(request));

        assertEquals(400, exception.getStatus());
        assertErrorGlobalisationCode(exception, LOAN_FIRST_DAY_OUT_OF_RANGE);
    }

    @Test
    public void aLoanCannotOverrideTheDaysOutOfOrder() {
        final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
        final Long productId = createSemiMonthlyProduct(15, 31);
        final PostLoansRequest request = semiMonthlyApplication(clientId, productId, "02 January 2023", 4).firstRepaymentDayOfMonth(25)
                .secondRepaymentDayOfMonth(10);

        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class, () -> applyForLoan(request));

        assertEquals(400, exception.getStatus());
        assertErrorGlobalisationCode(exception, LOAN_SECOND_DAY_NOT_AFTER_FIRST);
    }

    /**
     * A loan overrides the days as a pair: a first day of 26 next to the product's second day, the 25th, would be out
     * of order.
     */
    @Test
    public void aLoanCannotOverrideOnlyOneDay() {
        final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
        final Long productId = createSemiMonthlyProduct(10, 25);
        final PostLoansRequest request = semiMonthlyApplication(clientId, productId, "02 January 2023", 4).firstRepaymentDayOfMonth(26);

        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class, () -> applyForLoan(request));

        assertEquals(400, exception.getStatus());
        assertErrorGlobalisationCode(exception, LOAN_SECOND_DAY_MISSING);
    }

    @Test
    public void aMonthlyLoanCannotCarryTheDueDays() {
        final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
        final Long productId = createLoanProduct(new LoanProductTestBuilder().withPrincipal(PRINCIPAL).withRepaymentAfterEvery("1")
                .withNumberOfRepayments("6").withRepaymentTypeAsMonth().withInterestRateFrequencyTypeAsMonths().buildRequest(null));
        final PostLoansRequest request = LoanRequestBuilders
                .legacyIndividualApplication(clientId, productId, PRINCIPAL, 6, BigDecimal.valueOf(2), "02 January 2023")
                .firstRepaymentDayOfMonth(15).secondRepaymentDayOfMonth(31);

        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class, () -> applyForLoan(request));

        assertEquals(400, exception.getStatus());
        assertErrorGlobalisationCode(exception, LOAN_DAY_NOT_SEMI_MONTHLY);
    }

    /** The days would otherwise fall back silently to a pair nobody configured. */
    @Test
    public void aLoanOnAMonthlyProductNeedsTheDueDaysToBeSemiMonthly() {
        final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
        final Long productId = createLoanProduct(new LoanProductTestBuilder().withPrincipal(PRINCIPAL).withRepaymentAfterEvery("1")
                .withNumberOfRepayments("6").withRepaymentTypeAsMonth().withInterestRateFrequencyTypeAsMonths().buildRequest(null));
        final PostLoansRequest request = semiMonthlyApplication(clientId, productId, "02 January 2023", 4);

        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class, () -> applyForLoan(request));

        assertEquals(400, exception.getStatus());
        assertErrorGlobalisationCode(exception, LOAN_FIRST_DAY_MISSING);
    }

    @Test
    public void aFirstRepaymentDateOffTheDueDaysIsRejected() {
        final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
        final Long productId = createSemiMonthlyProduct(15, 31);
        final PostLoansRequest request = semiMonthlyApplication(clientId, productId, "02 January 2023", 4)
                .repaymentsStartingFromDate("20 January 2023");

        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class, () -> applyForLoan(request));

        assertEquals(403, exception.getStatus());
        assertErrorGlobalisationCode(exception, FIRST_REPAYMENT_NOT_A_DUE_DAY);
    }

    /**
     * Re-aging derives each due date from a period length, which a semi-monthly frequency does not have, so it is
     * rejected as a bad request before any business rule is checked.
     */
    @Test
    public void aLoanCannotBeReAgedWithASemiMonthlyFrequency() {
        final Long loanId = applyForSemiMonthlyLoan(15, 31, "02 January 2023", 4);

        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> reAgeLoan(loanId, "SEMI_MONTHLY", 1, "15 March 2023", 4, null));

        assertEquals(400, exception.getStatus());
        assertErrorGlobalisationCode(exception, REAGE_FREQUENCY_NOT_SUPPORTED);
    }

    // --- Re-aging ------------------------------------------------------------------------------------------------

    /**
     * With interest "same as repayment period" and partial periods allowed, a re-aged period is charged 1/24 of the
     * annual rate for each semi-monthly period it covers: a monthly re-aged period of a loan due on the 15th and the
     * last day covers two of them, so it is charged 2% at 24% a year, the same as a monthly loan. The loan is re-aged
     * before its maturity date: re-aging after it loses the interest of the re-aged periods on every frequency, which
     * is a separate engine issue.
     */
    @Test
    public void aSemiMonthlyLoanWithPartialPeriodsIsReAgedByTheSemiMonthlyPeriodsEachInstallmentCovers() {
        final Long loanId = disburseProgressiveSemiMonthlyLoan(
                fourInstallmentsProgressiveWithAdvancedAllocation().allowPartialPeriodInterestCalculation(true),
                LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD);

        runAt("20 February 2023", () -> {
            reAgeLoan(loanId, "MONTHS", 1, "15 March 2023", 3, null);

            final List<GetLoansLoanIdRepaymentPeriod> periods = repaymentPeriods(loanId);
            int checkedInstallments = 0;
            for (int index = 1; index < periods.size(); index++) {
                final GetLoansLoanIdRepaymentPeriod period = periods.get(index);
                if (!LATER_RE_AGED_DUE_DATES.contains(period.getDueDate())) {
                    continue;
                }
                final BigDecimal openingBalance = periods.get(index - 1).getPrincipalLoanBalanceOutstanding();
                final BigDecimal expectedInterest = openingBalance.multiply(new BigDecimal("0.02")).setScale(2, RoundingMode.HALF_UP);
                assertTrue(expectedInterest.subtract(period.getInterestDue()).abs().compareTo(new BigDecimal("0.01")) <= 0,
                        "Interest of the re-aged installment due on " + period.getDueDate() + " (two semi-monthly periods on "
                                + openingBalance + "): expected " + expectedInterest + " but was " + period.getInterestDue());
                checkedInstallments++;
            }
            assertEquals(2, checkedInstallments, "Re-aged installments due on 15 April and 15 May 2023");
            assertEquals(0, BigDecimal.ZERO.compareTo(periods.getLast().getPrincipalLoanBalanceOutstanding()),
                    "Outstanding principal after the last re-aged installment");
        });
    }

    /**
     * With daily interest and actual days in month each re-aged period is charged the interest of its own days on the
     * balance it starts with, so a semi-monthly loan can be re-aged into monthly installments: disbursed on 15 January
     * 2023, re-aged on 20 February 2023 into three monthly installments from 15 March 2023, the second and the third
     * re-aged installments carry 31 and 30 days of interest at 24% a year and the loan amortises to zero.
     */
    @Test
    public void aSemiMonthlyLoanWithDailyInterestIsReAgedByTheActualDaysOfEachMonth() {
        final Long loanId = disburseProgressiveSemiMonthlyLoan(fourInstallmentsProgressiveWithAdvancedAllocation()
                .daysInMonthType(LoanTestData.DaysInMonthType.ACTUAL).daysInYearType(LoanTestData.DaysInYearType.ACTUAL),
                LoanTestData.InterestCalculationPeriodType.DAILY);

        runAt("20 February 2023", () -> {
            reAgeLoan(loanId, "MONTHS", 1, "15 March 2023", 3, null);

            final List<GetLoansLoanIdRepaymentPeriod> periods = repaymentPeriods(loanId);
            int checkedInstallments = 0;
            for (int index = 1; index < periods.size(); index++) {
                final GetLoansLoanIdRepaymentPeriod period = periods.get(index);
                if (!LATER_RE_AGED_DUE_DATES.contains(period.getDueDate())) {
                    continue;
                }
                final BigDecimal openingBalance = periods.get(index - 1).getPrincipalLoanBalanceOutstanding();
                final long days = ChronoUnit.DAYS.between(period.getFromDate(), period.getDueDate());
                final BigDecimal expectedInterest = openingBalance.multiply(new BigDecimal("0.24")).multiply(BigDecimal.valueOf(days))
                        .divide(BigDecimal.valueOf(365), 2, RoundingMode.HALF_UP);
                assertTrue(expectedInterest.subtract(period.getInterestDue()).abs().compareTo(new BigDecimal("0.01")) <= 0,
                        "Interest of the re-aged installment due on " + period.getDueDate() + " (" + days + " days on " + openingBalance
                                + "): expected " + expectedInterest + " but was " + period.getInterestDue());
                checkedInstallments++;
            }
            assertEquals(2, checkedInstallments, "Re-aged installments due on 15 April and 15 May 2023");
            assertEquals(0, BigDecimal.ZERO.compareTo(periods.getLast().getPrincipalLoanBalanceOutstanding()),
                    "Outstanding principal after the last re-aged installment");
        });
    }

    // --- Helpers -------------------------------------------------------------------------------------------------
    /**
     * Creates, approves and disburses on 15 January 2023 a 1000 loan at 24% a year with four semi-monthly installments
     * (15th and last day) on a progressive product built from the given template, with interest recalculation enabled
     * since re-aging an interest bearing loan requires it. The interest calculation period is set on the product and on
     * the loan alike.
     */
    private Long disburseProgressiveSemiMonthlyLoan(final PostLoanProductsRequest productTemplate,
            final Integer interestCalculationPeriodType) {
        final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
        final Long productId = createLoanProduct(productTemplate.repaymentFrequencyType(SEMI_MONTHLY.longValue())
                .firstRepaymentDayOfMonth(15).secondRepaymentDayOfMonth(31).interestRatePerPeriod(24.0)
                .interestCalculationPeriodType(interestCalculationPeriodType).isInterestRecalculationEnabled(true)
                .interestRecalculationCompoundingMethod(LoanTestData.InterestRecalculationCompoundingMethod.NONE)
                .recalculationRestFrequencyType(LoanTestData.RecalculationRestFrequencyType.DAILY).recalculationRestFrequencyInterval(1)
                .preClosureInterestCalculationStrategy(1).isArrearsBasedOnOriginalSchedule(false)
                .isCompoundingToBePostedAsTransaction(false).allowCompoundingOnEod(false));
        final AtomicLong loanId = new AtomicLong();
        runAt("15 January 2023", () -> {
            loanId.set(applyForLoan(LoanRequestBuilders.applyProgressiveLoan(clientId, productId, "15 January 2023", 1000.0, 4, 24.0)
                    .loanTermFrequencyType(SEMI_MONTHLY).repaymentFrequencyType(SEMI_MONTHLY)
                    .interestCalculationPeriodType(interestCalculationPeriodType)));
            approveLoan(loanId.get(), approveLoanRequest(1000.0, "15 January 2023"));
            disburseLoan(loanId.get(), BigDecimal.valueOf(1000.0), "15 January 2023");
        });
        return loanId.get();
    }

    private Long applyForSemiMonthlyLoan(final int firstRepaymentDayOfMonth, final int secondRepaymentDayOfMonth,
            final String disbursementDate, final int numberOfRepayments) {
        final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
        final Long productId = createSemiMonthlyProduct(firstRepaymentDayOfMonth, secondRepaymentDayOfMonth);
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

    private Long createSemiMonthlyProduct(final int firstRepaymentDayOfMonth, final int secondRepaymentDayOfMonth) {
        return createLoanProduct(semiMonthlyProductRequest(firstRepaymentDayOfMonth, secondRepaymentDayOfMonth));
    }

    private PostLoanProductsRequest semiMonthlyProductRequest(final int firstRepaymentDayOfMonth, final int secondRepaymentDayOfMonth) {
        return new LoanProductTestBuilder().withPrincipal(PRINCIPAL).withRepaymentAfterEvery("1").withNumberOfRepayments("24")
                .withRepaymentTypeAsSemiMonthly(firstRepaymentDayOfMonth, secondRepaymentDayOfMonth).withInterestRateFrequencyTypeAsMonths()
                .buildRequest(null);
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
