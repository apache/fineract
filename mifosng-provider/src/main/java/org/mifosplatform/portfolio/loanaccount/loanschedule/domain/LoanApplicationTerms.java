/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.loanschedule.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrency;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.calendar.domain.CalendarInstance;
import org.mifosplatform.portfolio.common.domain.DayOfWeekType;
import org.mifosplatform.portfolio.common.domain.DaysInMonthType;
import org.mifosplatform.portfolio.common.domain.DaysInYearType;
import org.mifosplatform.portfolio.common.domain.NthDayType;
import org.mifosplatform.portfolio.common.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.loanaccount.data.DisbursementData;
import org.mifosplatform.portfolio.loanaccount.data.LoanTermVariationsData;
import org.mifosplatform.portfolio.loanaccount.domain.LoanInterestRecalculationDetails;
import org.mifosplatform.portfolio.loanproduct.domain.AmortizationMethod;
import org.mifosplatform.portfolio.loanproduct.domain.InterestCalculationPeriodMethod;
import org.mifosplatform.portfolio.loanproduct.domain.InterestMethod;
import org.mifosplatform.portfolio.loanproduct.domain.InterestRecalculationCompoundingMethod;
import org.mifosplatform.portfolio.loanproduct.domain.LoanPreClosureInterestCalculationStrategy;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.mifosplatform.portfolio.loanproduct.domain.LoanRescheduleStrategyMethod;
import org.mifosplatform.portfolio.loanproduct.domain.RecalculationFrequencyType;

public final class LoanApplicationTerms {

    private final ApplicationCurrency currency;
    private Integer loanTermFrequency;
    private final PeriodFrequencyType loanTermPeriodFrequencyType;
    private Integer numberOfRepayments;
    private final Integer repaymentEvery;
    private final PeriodFrequencyType repaymentPeriodFrequencyType;
    private final Integer nthDay;

    private final DayOfWeekType weekDayType;
    private final AmortizationMethod amortizationMethod;

    private final InterestMethod interestMethod;
    private BigDecimal interestRatePerPeriod;
    private final PeriodFrequencyType interestRatePeriodFrequencyType;
    private BigDecimal annualNominalInterestRate;
    private final InterestCalculationPeriodMethod interestCalculationPeriodMethod;

    private Money principal;
    private final LocalDate expectedDisbursementDate;
    private final LocalDate repaymentsStartingFromDate;
    private final LocalDate calculatedRepaymentsStartingFromDate;
    /**
     * Integer representing the number of 'repayment frequencies' or
     * installments where 'grace' should apply to the principal component of a
     * loans repayment period (installment).
     */
    private Integer principalGrace;

    /**
     * Integer representing the number of 'repayment frequencies' or
     * installments where 'grace' should apply to the payment of interest in a
     * loans repayment period (installment).
     * 
     * <b>Note:</b> Interest is still calculated taking into account the full
     * loan term, the interest is simply offset to a later period.
     */
    private Integer interestPaymentGrace;

    /**
     * Integer representing the number of 'repayment frequencies' or
     * installments where 'grace' should apply to the charging of interest in a
     * loans repayment period (installment).
     * 
     * <b>Note:</b> The loan is <i>interest-free</i> for the period of time
     * indicated.
     */
    private final Integer interestChargingGrace;

    /**
     * Legacy method of support 'grace' on the charging of interest on a loan.
     * 
     * <p>
     * For the typical structured loan, its reasonable to use an integer to
     * indicate the number of 'repayment frequency' periods the 'grace' should
     * apply to but for slightly <b>irregular</b> loans where the period between
     * disbursement and the date of the 'first repayment period' isnt doest
     * match the 'repayment frequency' but can be less (15days instead of 1
     * month) or more (6 weeks instead of 1 month) - The idea was to use a date
     * to indicate from whence interest should be charged.
     * </p>
     */
    private LocalDate interestChargedFromDate;
    private final Money inArrearsTolerance;

    private final Integer graceOnArrearsAgeing;

    // added
    private LocalDate loanEndDate;

    private final List<DisbursementData> disbursementDatas;

    private final boolean multiDisburseLoan;

    private BigDecimal fixedEmiAmount;

    private final BigDecimal actualFixedEmiAmount;

    private final BigDecimal maxOutstandingBalance;

    private final List<LoanTermVariationsData> emiAmountVariations;

    private Money totalInterestDue;

    private final DaysInMonthType daysInMonthType;

    private final DaysInYearType daysInYearType;

    private final boolean interestRecalculationEnabled;

    private final LoanRescheduleStrategyMethod rescheduleStrategyMethod;

    private final InterestRecalculationCompoundingMethod interestRecalculationCompoundingMethod;

    private final CalendarInstance restCalendarInstance;

    private final RecalculationFrequencyType recalculationFrequencyType;

    private final BigDecimal principalThresholdForLastInstalment;
    private final Integer installmentAmountInMultiplesOf;

    private final LoanPreClosureInterestCalculationStrategy preClosureInterestCalculationStrategy;

    public static LoanApplicationTerms assembleFrom(final ApplicationCurrency currency, final Integer loanTermFrequency,
            final PeriodFrequencyType loanTermPeriodFrequencyType, final Integer numberOfRepayments, final Integer repaymentEvery,
            final PeriodFrequencyType repaymentPeriodFrequencyType, Integer nthDay, DayOfWeekType weekDayType,
            final AmortizationMethod amortizationMethod, final InterestMethod interestMethod, final BigDecimal interestRatePerPeriod,
            final PeriodFrequencyType interestRatePeriodFrequencyType, final BigDecimal annualNominalInterestRate,
            final InterestCalculationPeriodMethod interestCalculationPeriodMethod, final Money principalMoney,
            final LocalDate expectedDisbursementDate, final LocalDate repaymentsStartingFromDate,
            final LocalDate calculatedRepaymentsStartingFromDate, final Integer graceOnPrincipalPayment,
            final Integer graceOnInterestPayment, final Integer graceOnInterestCharged, final LocalDate interestChargedFromDate,
            final Money inArrearsTolerance, final boolean multiDisburseLoan, final BigDecimal emiAmount,
            final List<DisbursementData> disbursementDatas, final BigDecimal maxOutstandingBalance,
            final List<LoanTermVariationsData> emiAmountVariations, final Integer graceOnArrearsAgeing,
            final DaysInMonthType daysInMonthType, final DaysInYearType daysInYearType, final boolean isInterestRecalculationEnabled,
            final RecalculationFrequencyType recalculationFrequencyType, final CalendarInstance restCalendarInstance,
            final BigDecimal principalThresholdForLastInstalment, final Integer installmentAmountInMultiplesOf,
            final LoanPreClosureInterestCalculationStrategy preClosureInterestCalculationStrategy) {

        final LoanRescheduleStrategyMethod rescheduleStrategyMethod = null;
        final InterestRecalculationCompoundingMethod interestRecalculationCompoundingMethod = null;
        return new LoanApplicationTerms(currency, loanTermFrequency, loanTermPeriodFrequencyType, numberOfRepayments, repaymentEvery,
                repaymentPeriodFrequencyType, nthDay, weekDayType, amortizationMethod, interestMethod, interestRatePerPeriod,
                interestRatePeriodFrequencyType, annualNominalInterestRate, interestCalculationPeriodMethod, principalMoney,
                expectedDisbursementDate, repaymentsStartingFromDate, calculatedRepaymentsStartingFromDate, graceOnPrincipalPayment,
                graceOnInterestPayment, graceOnInterestCharged, interestChargedFromDate, inArrearsTolerance, multiDisburseLoan, emiAmount,
                disbursementDatas, maxOutstandingBalance, emiAmountVariations, graceOnArrearsAgeing, daysInMonthType, daysInYearType,
                isInterestRecalculationEnabled, rescheduleStrategyMethod, interestRecalculationCompoundingMethod, restCalendarInstance,
                recalculationFrequencyType, principalThresholdForLastInstalment, installmentAmountInMultiplesOf,
                preClosureInterestCalculationStrategy);
    }

    public static LoanApplicationTerms assembleFrom(final ApplicationCurrency applicationCurrency, final Integer loanTermFrequency,
            final PeriodFrequencyType loanTermPeriodFrequencyType, NthDayType nthDay, DayOfWeekType dayOfWeek,
            final LocalDate expectedDisbursementDate, final LocalDate repaymentsStartingFromDate,
            final LocalDate calculatedRepaymentsStartingFromDate, final Money inArrearsTolerance,
            final LoanProductRelatedDetail loanProductRelatedDetail, final boolean multiDisburseLoan, final BigDecimal emiAmount,
            final List<DisbursementData> disbursementDatas, final BigDecimal maxOutstandingBalance,
            final List<LoanTermVariationsData> emiAmountVariations, final LocalDate interestChargedFromDate,
            final BigDecimal principalThresholdForLastInstalment, final Integer installmentAmountInMultiplesOf,
            final RecalculationFrequencyType recalculationFrequencyType, final CalendarInstance restCalendarInstance,
            final InterestRecalculationCompoundingMethod compoundingMethod,
            final LoanPreClosureInterestCalculationStrategy loanPreClosureInterestCalculationStrategy) {

        final Integer numberOfRepayments = loanProductRelatedDetail.getNumberOfRepayments();
        final Integer repaymentEvery = loanProductRelatedDetail.getRepayEvery();
        final PeriodFrequencyType repaymentPeriodFrequencyType = loanProductRelatedDetail.getRepaymentPeriodFrequencyType();
        final AmortizationMethod amortizationMethod = loanProductRelatedDetail.getAmortizationMethod();
        final InterestMethod interestMethod = loanProductRelatedDetail.getInterestMethod();
        final BigDecimal interestRatePerPeriod = loanProductRelatedDetail.getNominalInterestRatePerPeriod();
        final PeriodFrequencyType interestRatePeriodFrequencyType = loanProductRelatedDetail.getInterestPeriodFrequencyType();
        final BigDecimal annualNominalInterestRate = loanProductRelatedDetail.getAnnualNominalInterestRate();
        final InterestCalculationPeriodMethod interestCalculationPeriodMethod = loanProductRelatedDetail
                .getInterestCalculationPeriodMethod();
        final Money principalMoney = loanProductRelatedDetail.getPrincipal();

        //
        final Integer graceOnPrincipalPayment = loanProductRelatedDetail.graceOnPrincipalPayment();
        final Integer graceOnInterestPayment = loanProductRelatedDetail.graceOnInterestPayment();
        final Integer graceOnInterestCharged = loanProductRelatedDetail.graceOnInterestCharged();

        // Interest recalculation settings
        final DaysInMonthType daysInMonthType = loanProductRelatedDetail.fetchDaysInMonthType();
        final DaysInYearType daysInYearType = loanProductRelatedDetail.fetchDaysInYearType();
        final boolean isInterestRecalculationEnabled = loanProductRelatedDetail.isInterestRecalculationEnabled();
        final LoanRescheduleStrategyMethod rescheduleStrategyMethod = null;

        return new LoanApplicationTerms(applicationCurrency, loanTermFrequency, loanTermPeriodFrequencyType, numberOfRepayments,
                repaymentEvery, repaymentPeriodFrequencyType, nthDay.getValue(), dayOfWeek, amortizationMethod, interestMethod,
                interestRatePerPeriod, interestRatePeriodFrequencyType, annualNominalInterestRate, interestCalculationPeriodMethod,
                principalMoney, expectedDisbursementDate, repaymentsStartingFromDate, calculatedRepaymentsStartingFromDate,
                graceOnPrincipalPayment, graceOnInterestPayment, graceOnInterestCharged, interestChargedFromDate, inArrearsTolerance,
                multiDisburseLoan, emiAmount, disbursementDatas, maxOutstandingBalance, emiAmountVariations,
                loanProductRelatedDetail.getGraceOnDueDate(), daysInMonthType, daysInYearType, isInterestRecalculationEnabled,
                rescheduleStrategyMethod, compoundingMethod, restCalendarInstance, recalculationFrequencyType,
                principalThresholdForLastInstalment, installmentAmountInMultiplesOf, loanPreClosureInterestCalculationStrategy);
    }

    public static LoanApplicationTerms assembleFrom(final ApplicationCurrency applicationCurrency, final Integer loanTermFrequency,
            final PeriodFrequencyType loanTermPeriodFrequencyType, final LocalDate expectedDisbursementDate,
            final LocalDate repaymentsStartingFromDate, final LocalDate calculatedRepaymentsStartingFromDate,
            final Money inArrearsTolerance, final LoanProductRelatedDetail loanProductRelatedDetail, final boolean multiDisburseLoan,
            final BigDecimal emiAmount, final List<DisbursementData> disbursementDatas, final BigDecimal maxOutstandingBalance,
            final List<LoanTermVariationsData> emiAmountVariations, final LocalDate interestChargedFromDate,
            final LoanInterestRecalculationDetails interestRecalculationDetails, final CalendarInstance restCalendarInstance,
            final RecalculationFrequencyType recalculationFrequencyType, final BigDecimal principalThresholdForLastInstalment,
            Integer installmentAmountInMultiplesOf,
            final LoanPreClosureInterestCalculationStrategy loanPreClosureInterestCalculationStrategy) {

        final Integer numberOfRepayments = loanProductRelatedDetail.getNumberOfRepayments();
        final Integer repaymentEvery = loanProductRelatedDetail.getRepayEvery();
        final PeriodFrequencyType repaymentPeriodFrequencyType = loanProductRelatedDetail.getRepaymentPeriodFrequencyType();
        final AmortizationMethod amortizationMethod = loanProductRelatedDetail.getAmortizationMethod();
        final InterestMethod interestMethod = loanProductRelatedDetail.getInterestMethod();
        final BigDecimal interestRatePerPeriod = loanProductRelatedDetail.getNominalInterestRatePerPeriod();
        final PeriodFrequencyType interestRatePeriodFrequencyType = loanProductRelatedDetail.getInterestPeriodFrequencyType();
        final BigDecimal annualNominalInterestRate = loanProductRelatedDetail.getAnnualNominalInterestRate();
        final InterestCalculationPeriodMethod interestCalculationPeriodMethod = loanProductRelatedDetail
                .getInterestCalculationPeriodMethod();
        final Money principalMoney = loanProductRelatedDetail.getPrincipal();

        //
        final Integer graceOnPrincipalPayment = loanProductRelatedDetail.graceOnPrincipalPayment();
        final Integer graceOnInterestPayment = loanProductRelatedDetail.graceOnInterestPayment();
        final Integer graceOnInterestCharged = loanProductRelatedDetail.graceOnInterestCharged();

        // Interest recalculation settings
        final DaysInMonthType daysInMonthType = loanProductRelatedDetail.fetchDaysInMonthType();
        final DaysInYearType daysInYearType = loanProductRelatedDetail.fetchDaysInYearType();
        final boolean isInterestRecalculationEnabled = loanProductRelatedDetail.isInterestRecalculationEnabled();
        LoanRescheduleStrategyMethod rescheduleStrategyMethod = null;
        InterestRecalculationCompoundingMethod interestRecalculationCompoundingMethod = null;
        if (isInterestRecalculationEnabled) {
            rescheduleStrategyMethod = interestRecalculationDetails.getRescheduleStrategyMethod();
            interestRecalculationCompoundingMethod = interestRecalculationDetails.getInterestRecalculationCompoundingMethod();
        }

        return new LoanApplicationTerms(applicationCurrency, loanTermFrequency, loanTermPeriodFrequencyType, numberOfRepayments,
                repaymentEvery, repaymentPeriodFrequencyType, null, null, amortizationMethod, interestMethod, interestRatePerPeriod,
                interestRatePeriodFrequencyType, annualNominalInterestRate, interestCalculationPeriodMethod, principalMoney,
                expectedDisbursementDate, repaymentsStartingFromDate, calculatedRepaymentsStartingFromDate, graceOnPrincipalPayment,
                graceOnInterestPayment, graceOnInterestCharged, interestChargedFromDate, inArrearsTolerance, multiDisburseLoan, emiAmount,
                disbursementDatas, maxOutstandingBalance, emiAmountVariations, loanProductRelatedDetail.getGraceOnDueDate(),
                daysInMonthType, daysInYearType, isInterestRecalculationEnabled, rescheduleStrategyMethod,
                interestRecalculationCompoundingMethod, restCalendarInstance, recalculationFrequencyType,
                principalThresholdForLastInstalment, installmentAmountInMultiplesOf, loanPreClosureInterestCalculationStrategy);
    }

    public static LoanApplicationTerms assembleFrom(final Integer repaymentEvery, final PeriodFrequencyType repaymentPeriodFrequencyType,
            final LoanApplicationTerms applicationTerms) {
        return new LoanApplicationTerms(applicationTerms.currency, applicationTerms.loanTermFrequency,
                applicationTerms.loanTermPeriodFrequencyType, applicationTerms.numberOfRepayments, repaymentEvery,
                repaymentPeriodFrequencyType, null, null, applicationTerms.amortizationMethod, applicationTerms.interestMethod,
                applicationTerms.interestRatePerPeriod, applicationTerms.interestRatePeriodFrequencyType,
                applicationTerms.annualNominalInterestRate, applicationTerms.interestCalculationPeriodMethod, applicationTerms.principal,
                applicationTerms.expectedDisbursementDate, applicationTerms.repaymentsStartingFromDate,
                applicationTerms.calculatedRepaymentsStartingFromDate, applicationTerms.principalGrace,
                applicationTerms.interestPaymentGrace, applicationTerms.interestChargingGrace, applicationTerms.interestChargedFromDate,
                applicationTerms.inArrearsTolerance, applicationTerms.multiDisburseLoan, applicationTerms.fixedEmiAmount,
                applicationTerms.disbursementDatas, applicationTerms.maxOutstandingBalance, applicationTerms.emiAmountVariations,
                applicationTerms.graceOnArrearsAgeing, applicationTerms.daysInMonthType, applicationTerms.daysInYearType,
                applicationTerms.interestRecalculationEnabled, applicationTerms.rescheduleStrategyMethod,
                applicationTerms.interestRecalculationCompoundingMethod, applicationTerms.restCalendarInstance,
                applicationTerms.recalculationFrequencyType, applicationTerms.principalThresholdForLastInstalment,
                applicationTerms.installmentAmountInMultiplesOf, applicationTerms.preClosureInterestCalculationStrategy);
    }

    private LoanApplicationTerms(final ApplicationCurrency currency, final Integer loanTermFrequency,
            final PeriodFrequencyType loanTermPeriodFrequencyType, final Integer numberOfRepayments, final Integer repaymentEvery,
            final PeriodFrequencyType repaymentPeriodFrequencyType, final Integer nthDay, final DayOfWeekType weekDayType,
            final AmortizationMethod amortizationMethod, final InterestMethod interestMethod, final BigDecimal interestRatePerPeriod,
            final PeriodFrequencyType interestRatePeriodFrequencyType, final BigDecimal annualNominalInterestRate,
            final InterestCalculationPeriodMethod interestCalculationPeriodMethod, final Money principal,
            final LocalDate expectedDisbursementDate, final LocalDate repaymentsStartingFromDate,
            final LocalDate calculatedRepaymentsStartingFromDate, final Integer principalGrace, final Integer interestPaymentGrace,
            final Integer interestChargingGrace, final LocalDate interestChargedFromDate, final Money inArrearsTolerance,
            final boolean multiDisburseLoan, final BigDecimal emiAmount, final List<DisbursementData> disbursementDatas,
            final BigDecimal maxOutstandingBalance, final List<LoanTermVariationsData> emiAmountVariations,
            final Integer graceOnArrearsAgeing, final DaysInMonthType daysInMonthType, final DaysInYearType daysInYearType,
            final boolean isInterestRecalculationEnabled, final LoanRescheduleStrategyMethod rescheduleStrategyMethod,
            final InterestRecalculationCompoundingMethod interestRecalculationCompoundingMethod,
            final CalendarInstance restCalendarInstance, final RecalculationFrequencyType recalculationFrequencyType,
            final BigDecimal principalThresholdForLastInstalment, final Integer installmentAmountInMultiplesOf,
            final LoanPreClosureInterestCalculationStrategy preClosureInterestCalculationStrategy) {
        this.currency = currency;
        this.loanTermFrequency = loanTermFrequency;
        this.loanTermPeriodFrequencyType = loanTermPeriodFrequencyType;
        this.numberOfRepayments = numberOfRepayments;
        this.repaymentEvery = repaymentEvery;
        this.repaymentPeriodFrequencyType = repaymentPeriodFrequencyType;
        this.nthDay = nthDay;
        this.weekDayType = weekDayType;
        this.amortizationMethod = amortizationMethod;

        this.interestMethod = interestMethod;
        this.interestRatePerPeriod = interestRatePerPeriod;
        this.interestRatePeriodFrequencyType = interestRatePeriodFrequencyType;
        this.annualNominalInterestRate = annualNominalInterestRate;
        this.interestCalculationPeriodMethod = interestCalculationPeriodMethod;

        this.principal = principal;
        this.expectedDisbursementDate = expectedDisbursementDate;
        this.repaymentsStartingFromDate = repaymentsStartingFromDate;
        this.calculatedRepaymentsStartingFromDate = calculatedRepaymentsStartingFromDate;

        this.principalGrace = principalGrace;
        this.interestPaymentGrace = interestPaymentGrace;
        this.interestChargingGrace = interestChargingGrace;
        this.interestChargedFromDate = interestChargedFromDate;

        this.inArrearsTolerance = inArrearsTolerance;
        this.multiDisburseLoan = multiDisburseLoan;
        this.fixedEmiAmount = emiAmount;
        this.actualFixedEmiAmount = emiAmount;
        this.disbursementDatas = disbursementDatas;
        this.maxOutstandingBalance = maxOutstandingBalance;
        this.emiAmountVariations = emiAmountVariations;
        this.graceOnArrearsAgeing = graceOnArrearsAgeing;
        this.daysInMonthType = daysInMonthType;
        this.daysInYearType = daysInYearType;
        this.interestRecalculationEnabled = isInterestRecalculationEnabled;
        this.rescheduleStrategyMethod = rescheduleStrategyMethod;
        this.interestRecalculationCompoundingMethod = interestRecalculationCompoundingMethod;
        this.restCalendarInstance = restCalendarInstance;
        this.recalculationFrequencyType = recalculationFrequencyType;
        this.principalThresholdForLastInstalment = principalThresholdForLastInstalment;
        this.installmentAmountInMultiplesOf = installmentAmountInMultiplesOf;
        this.preClosureInterestCalculationStrategy = preClosureInterestCalculationStrategy;
    }

    public Money adjustPrincipalIfLastRepaymentPeriod(final Money principalForPeriod, final Money totalCumulativePrincipalToDate,
            final int periodNumber) {

        Money adjusted = principalForPeriod;

        final Money totalPrincipalRemaining = this.principal.minus(totalCumulativePrincipalToDate);
        if (totalPrincipalRemaining.isLessThanZero()) {
            // paid too much principal, subtract amount that overpays from
            // principal paid for period.
            adjusted = principalForPeriod.minus(totalPrincipalRemaining.abs());
        } else if (this.actualFixedEmiAmount != null) {
            final Money difference = this.principal.minus(totalCumulativePrincipalToDate);
            final Money principalThreshold = principalForPeriod.multipliedBy(this.principalThresholdForLastInstalment).dividedBy(100,
                    RoundingMode.HALF_EVEN);
            if (difference.isLessThan(principalThreshold)) {
                adjusted = principalForPeriod.plus(difference.abs());
            }
        } else if (isLastRepaymentPeriod(this.numberOfRepayments, periodNumber)) {

            final Money difference = totalCumulativePrincipalToDate.minus(this.principal);
            if (difference.isLessThanZero()) {
                adjusted = principalForPeriod.plus(difference.abs());
            } else if (difference.isGreaterThanZero()) {
                adjusted = principalForPeriod.minus(difference.abs());
            }
        }

        return adjusted;
    }

    public Money adjustInterestIfLastRepaymentPeriod(final Money interestForThisPeriod, final Money totalCumulativeInterestToDate,
            final Money totalInterestDueForLoan, final int periodNumber) {

        Money adjusted = interestForThisPeriod;

        final Money totalInterestRemaining = totalInterestDueForLoan.minus(totalCumulativeInterestToDate);
        if (totalInterestRemaining.isLessThanZero()) {
            // paid too much interest, subtract amount that overpays from
            // interest paid for period.
            adjusted = interestForThisPeriod.minus(totalInterestRemaining.abs());
        } else if (isLastRepaymentPeriod(this.numberOfRepayments, periodNumber)) {
            final Money interestDifference = totalCumulativeInterestToDate.minus(totalInterestDueForLoan);
            if (interestDifference.isLessThanZero()) {
                adjusted = interestForThisPeriod.plus(interestDifference.abs());
            } else if (interestDifference.isGreaterThanZero()) {
                adjusted = interestForThisPeriod.minus(interestDifference.abs());
            }
        }
        if (adjusted.isLessThanZero()) {
            adjusted = adjusted.plus(adjusted);
        }
        return adjusted;
    }

    /**
     * Calculates the total interest to be charged on loan taking into account
     * grace settings.
     * 
     */
    public Money calculateTotalInterestCharged(final PaymentPeriodsInOneYearCalculator calculator, final MathContext mc) {

        Money totalInterestCharged = this.principal.zero();

        switch (this.interestMethod) {
            case FLAT:
                final Money totalInterestChargedForLoanTerm = calculateTotalFlatInterestDueWithoutGrace(calculator, mc);

                final Money totalInterestPerInstallment = calculateTotalInterestPerInstallmentWithoutGrace(calculator, mc);

                final Money totalGraceOnInterestCharged = totalInterestPerInstallment.multiplyRetainScale(getInterestChargingGrace(),
                        mc.getRoundingMode());

                totalInterestCharged = totalInterestChargedForLoanTerm.minus(totalGraceOnInterestCharged);
            break;
            case DECLINING_BALANCE:
            case INVALID:
            break;
        }

        return totalInterestCharged;
    }

    public Money calculateTotalPrincipalForPeriod(final PaymentPeriodsInOneYearCalculator calculator, final int daysInPeriod,
            final Money outstandingBalance, final int periodNumber, final MathContext mc, Money interestForThisInstallment) {

        Money principalForInstallment = this.principal.zero();

        switch (this.interestMethod) {
            case FLAT:
                principalForInstallment = calculateTotalPrincipalPerPeriodWithoutGrace(mc);
                if (isPrincipalGraceApplicableForThisPeriod(periodNumber)) {
                    principalForInstallment = Money.zero(principalForInstallment.getCurrency());
                }
            break;
            case DECLINING_BALANCE:
                switch (this.amortizationMethod) {
                    case EQUAL_INSTALLMENTS:
                        // equal installments
                        final int periodsElapsed = periodNumber - 1;
                        // with periodic interest for default month and year for
                        // equal installment
                        final BigDecimal periodicInterestRateForRepaymentPeriod = periodicInterestRate(calculator, mc, daysInPeriod,
                                DaysInMonthType.DAYS_30, DaysInYearType.DAYS_365);
                        Money totalPmtForThisInstallment = calculateTotalDueForEqualInstallmentRepaymentPeriod(
                                periodicInterestRateForRepaymentPeriod, outstandingBalance, periodsElapsed);
                        principalForInstallment = calculatePrincipalDueForInstallment(periodNumber, totalPmtForThisInstallment,
                                interestForThisInstallment);
                    break;
                    case EQUAL_PRINCIPAL:
                        principalForInstallment = calculateEqualPrincipalDueForInstallment(mc, periodNumber);
                    break;
                    case INVALID:
                    break;
                }
            break;
            case INVALID:
            break;
        }

        return principalForInstallment;
    }

    public PrincipalInterest calculateTotalInterestForPeriod(final PaymentPeriodsInOneYearCalculator calculator,
            final double interestCalculationGraceOnRepaymentPeriodFraction, final int periodNumber, final MathContext mc,
            final Money cumulatingInterestPaymentDueToGrace, final int daysInPeriodApplicableForInterest, final Money outstandingBalance) {

        Money interestForInstallment = this.principal.zero();
        Money interestBroughtForwardDueToGrace = cumulatingInterestPaymentDueToGrace.copy();

        switch (this.interestMethod) {
            case FLAT:

                switch (this.amortizationMethod) {
                    case EQUAL_INSTALLMENTS:
                        // average out outstanding interest over remaining
                        // instalments where interest is applicable
                        interestForInstallment = calculateTotalFlatInterestForInstallmentAveragingOutGracePeriods(calculator, periodNumber,
                                mc);
                    break;
                    case EQUAL_PRINCIPAL:
                        // interest follows time-value of money and is brought
                        // forward to next applicable interest payment period
                        final PrincipalInterest result = calculateTotalFlatInterestForPeriod(calculator, periodNumber, mc,
                                interestBroughtForwardDueToGrace);
                        interestForInstallment = result.interest();
                        interestBroughtForwardDueToGrace = result.interestPaymentDueToGrace();
                    break;
                    case INVALID:
                    break;
                }
            break;
            case DECLINING_BALANCE:

                final Money interestForThisInstallmentBeforeGrace = calculateDecliningInterestDueForInstallmentBeforeApplyingGrace(
                        calculator, mc, daysInPeriodApplicableForInterest, outstandingBalance);

                final Money interestForThisInstallmentAfterGrace = calculateDecliningInterestDueForInstallmentAfterApplyingGrace(
                        calculator, interestCalculationGraceOnRepaymentPeriodFraction, mc, daysInPeriodApplicableForInterest,
                        outstandingBalance, periodNumber);

                interestForInstallment = interestForThisInstallmentAfterGrace;
                if (interestForThisInstallmentAfterGrace.isGreaterThanZero()) {
                    interestForInstallment = interestBroughtForwardDueToGrace.plus(interestForThisInstallmentAfterGrace);
                    interestBroughtForwardDueToGrace = interestBroughtForwardDueToGrace.zero();
                } else if (isInterestFreeGracePeriod(periodNumber)) {
                    interestForInstallment = interestForInstallment.zero();
                } else if (isInterestFreeGracePeriodFromDate(interestCalculationGraceOnRepaymentPeriodFraction)) {
                    interestForInstallment = interestForThisInstallmentAfterGrace;
                } else {
                    interestBroughtForwardDueToGrace = interestBroughtForwardDueToGrace.plus(interestForThisInstallmentBeforeGrace);
                }
            break;
            case INVALID:
            break;
        }

        return new PrincipalInterest(null, interestForInstallment, interestBroughtForwardDueToGrace);
    }

    private final boolean isLastRepaymentPeriod(final int numberOfRepayments, final int periodNumber) {
        return periodNumber == numberOfRepayments;
    }

    /**
     * general method to calculate totalInterestDue discounting any grace
     * settings
     */
    private Money calculateTotalFlatInterestDueWithoutGrace(final PaymentPeriodsInOneYearCalculator calculator, final MathContext mc) {

        Money totalInterestDue = this.principal.zero();

        switch (this.interestMethod) {
            case FLAT:
                final BigDecimal interestRateForLoanTerm = calculateFlatInterestRateForLoanTerm(calculator, mc);
                totalInterestDue = this.principal.multiplyRetainScale(interestRateForLoanTerm, mc.getRoundingMode());

            break;
            case DECLINING_BALANCE:
            break;
            case INVALID:
            break;
        }

        if (this.totalInterestDue != null) {
            totalInterestDue = this.totalInterestDue;
        }

        return totalInterestDue;
    }

    private BigDecimal calculateFlatInterestRateForLoanTerm(final PaymentPeriodsInOneYearCalculator calculator, final MathContext mc) {

        final BigDecimal divisor = BigDecimal.valueOf(Double.valueOf("100.0"));

        final long loanTermPeriodsInOneYear = calculatePeriodsInOneYear(calculator);
        final BigDecimal loanTermPeriodsInYearBigDecimal = BigDecimal.valueOf(loanTermPeriodsInOneYear);

        final BigDecimal loanTermFrequencyBigDecimal = calculatePeriodsInLoanTerm();

        return this.annualNominalInterestRate.divide(loanTermPeriodsInYearBigDecimal, mc).divide(divisor, mc)
                .multiply(loanTermFrequencyBigDecimal);
    }

    private BigDecimal calculatePeriodsInLoanTerm() {

        BigDecimal periodsInLoanTerm = BigDecimal.valueOf(this.loanTermFrequency);
        switch (this.interestCalculationPeriodMethod) {
            case DAILY:
                // number of days from 'ideal disbursement' to final date

                LocalDate loanStartDate = getExpectedDisbursementDate();
                if (getInterestChargedFromDate() != null && loanStartDate.isBefore(getInterestChargedFromLocalDate())) {
                    loanStartDate = getInterestChargedFromLocalDate();
                }

                final int periodsInLoanTermInteger = Days.daysBetween(loanStartDate, this.loanEndDate).getDays();
                periodsInLoanTerm = BigDecimal.valueOf(periodsInLoanTermInteger);
            break;
            case INVALID:
            break;
            case SAME_AS_REPAYMENT_PERIOD:
            break;
        }

        return periodsInLoanTerm;
    }

    public void updateLoanEndDate(final LocalDate loanEndDate) {
        this.loanEndDate = loanEndDate;
    }

    private Money calculateTotalInterestPerInstallmentWithoutGrace(final PaymentPeriodsInOneYearCalculator calculator, final MathContext mc) {

        final Money totalInterestForLoanTerm = calculateTotalFlatInterestDueWithoutGrace(calculator, mc);

        return totalInterestForLoanTerm.dividedBy(Long.valueOf(this.numberOfRepayments), mc.getRoundingMode());
    }

    private Money calculateTotalPrincipalPerPeriodWithoutGrace(final MathContext mc) {
        final int totalRepaymentsWithCapitalPayment = calculateNumberOfRepaymentsWithPrincipalPayment();
        return this.principal.dividedBy(totalRepaymentsWithCapitalPayment, mc.getRoundingMode());
    }

    private PrincipalInterest calculateTotalFlatInterestForPeriod(final PaymentPeriodsInOneYearCalculator calculator,
            final int periodNumber, final MathContext mc, final Money cumulatingInterestPaymentDueToGrace) {

        Money interestBroughtForwardDueToGrace = cumulatingInterestPaymentDueToGrace.copy();

        Money interestForInstallment = calculateTotalInterestPerInstallmentWithoutGrace(calculator, mc);
        if (isInterestPaymentGraceApplicableForThisPeriod(periodNumber)) {
            interestBroughtForwardDueToGrace = interestBroughtForwardDueToGrace.plus(interestForInstallment);
            interestForInstallment = interestForInstallment.zero();
        } else if (isInterestFreeGracePeriod(periodNumber)) {
            interestForInstallment = interestForInstallment.zero();
        } else if (isFirstPeriodAfterInterestPaymentGracePeriod(periodNumber)) {
            interestForInstallment = cumulatingInterestPaymentDueToGrace.plus(interestForInstallment);
            interestBroughtForwardDueToGrace = interestBroughtForwardDueToGrace.zero();
        }

        return new PrincipalInterest(null, interestForInstallment, interestBroughtForwardDueToGrace);
    }

    /*
     * calculates the interest that should be due for a given scheduled loan
     * repayment period. It takes into account GRACE periods and calculates how
     * much interest is due per period by averaging the number of periods where
     * interest is due and should be paid against the total known interest that
     * is due without grace.
     */
    private Money calculateTotalFlatInterestForInstallmentAveragingOutGracePeriods(final PaymentPeriodsInOneYearCalculator calculator,
            final int periodNumber, final MathContext mc) {

        Money interestForInstallment = calculateTotalInterestPerInstallmentWithoutGrace(calculator, mc);
        if (isInterestPaymentGraceApplicableForThisPeriod(periodNumber)) {
            interestForInstallment = interestForInstallment.zero();
        } else if (isInterestFreeGracePeriod(periodNumber)) {
            interestForInstallment = interestForInstallment.zero();
        } else {

            final Money totalInterestForLoanTerm = calculateTotalFlatInterestDueWithoutGrace(calculator, mc);

            final Money interestPerGracePeriod = calculateTotalInterestPerInstallmentWithoutGrace(calculator, mc);

            final Money totalInterestFree = interestPerGracePeriod.multipliedBy(getInterestChargingGrace());
            final Money realTotalInterestForLoan = totalInterestForLoanTerm.minus(totalInterestFree);

            final Integer interestPaymentDuePeriods = calculateNumberOfRepaymentPeriodsWhereInterestPaymentIsDue(this.numberOfRepayments);

            interestForInstallment = realTotalInterestForLoan
                    .dividedBy(BigDecimal.valueOf(interestPaymentDuePeriods), mc.getRoundingMode());
        }

        return interestForInstallment;
    }

    private BigDecimal periodicInterestRate(final PaymentPeriodsInOneYearCalculator calculator, final MathContext mc, final int actualDays,
            final DaysInMonthType daysInMonthType, final DaysInYearType daysInYearType) {

        final long loanTermPeriodsInOneYear = calculatePeriodsInOneYear(calculator);

        final BigDecimal divisor = BigDecimal.valueOf(Double.valueOf("100.0"));
        final BigDecimal loanTermPeriodsInYearBigDecimal = BigDecimal.valueOf(loanTermPeriodsInOneYear);

        BigDecimal periodicInterestRate = BigDecimal.ZERO;

        switch (this.interestCalculationPeriodMethod) {
            case INVALID:
            break;
            case DAILY:
                // For daily work out number of days in the period
                int numberOfDaysInPeriod = actualDays;

                final BigDecimal oneDayOfYearInterestRate = this.annualNominalInterestRate.divide(loanTermPeriodsInYearBigDecimal, mc)
                        .divide(divisor, mc);

                switch (this.repaymentPeriodFrequencyType) {
                    case INVALID:
                    break;
                    case DAYS:
                        final BigDecimal loanTermFrequencyBigDecimal = BigDecimal.valueOf(numberOfDaysInPeriod);
                        periodicInterestRate = oneDayOfYearInterestRate.multiply(loanTermFrequencyBigDecimal, mc);
                    break;
                    case WEEKS:
                        periodicInterestRate = oneDayOfYearInterestRate.multiply(BigDecimal.valueOf(numberOfDaysInPeriod), mc);
                    break;
                    case MONTHS:
                        if (daysInMonthType.isDaysInMonth_30()) {
                            numberOfDaysInPeriod = this.repaymentEvery * 30;
                        }
                        periodicInterestRate = oneDayOfYearInterestRate.multiply(BigDecimal.valueOf(numberOfDaysInPeriod), mc);
                    break;
                    case YEARS:
                        switch (daysInYearType) {
                            case DAYS_360:
                                numberOfDaysInPeriod = this.repaymentEvery * 360;
                            break;
                            case DAYS_364:
                                numberOfDaysInPeriod = this.repaymentEvery * 364;
                            break;
                            case DAYS_365:
                                numberOfDaysInPeriod = this.repaymentEvery * 365;
                            break;
                            default:
                            break;
                        }
                        periodicInterestRate = oneDayOfYearInterestRate.multiply(BigDecimal.valueOf(numberOfDaysInPeriod), mc);
                    break;
                }
            break;
            case SAME_AS_REPAYMENT_PERIOD:
                final BigDecimal loanTermFrequencyBigDecimal = BigDecimal.valueOf(this.repaymentEvery);
                periodicInterestRate = this.annualNominalInterestRate.divide(loanTermPeriodsInYearBigDecimal, mc).divide(divisor, mc)
                        .multiply(loanTermFrequencyBigDecimal);
            break;
        }

        return periodicInterestRate;
    }

    public BigDecimal interestRateFor(final PaymentPeriodsInOneYearCalculator calculator, final MathContext mc,
            final Money outstandingBalance, final LocalDate fromDate, final LocalDate toDate) {

        long loanTermPeriodsInOneYear = calculator.calculate(PeriodFrequencyType.DAYS).longValue();
        int repaymentEvery = Days.daysBetween(fromDate, toDate).getDays();
        if (isFallingInRepaymentPeriod(fromDate, toDate)) {
            loanTermPeriodsInOneYear = calculatePeriodsInOneYear(calculator);
            repaymentEvery = getPeriodsBetween(fromDate, toDate);
        }

        final BigDecimal divisor = BigDecimal.valueOf(Double.valueOf("100.0"));
        final BigDecimal loanTermPeriodsInYearBigDecimal = BigDecimal.valueOf(loanTermPeriodsInOneYear);
        final BigDecimal oneDayOfYearInterestRate = this.annualNominalInterestRate.divide(loanTermPeriodsInYearBigDecimal, mc).divide(
                divisor, mc);
        BigDecimal interestRate = oneDayOfYearInterestRate.multiply(BigDecimal.valueOf(repaymentEvery), mc);
        return outstandingBalance.getAmount().multiply(interestRate, mc);
    }

    private long calculatePeriodsInOneYear(final PaymentPeriodsInOneYearCalculator calculator) {

        long periodsInOneYear = calculator.calculate(this.repaymentPeriodFrequencyType).longValue();
        switch (this.interestCalculationPeriodMethod) {
            case DAILY:
                periodsInOneYear = calculator.calculate(PeriodFrequencyType.DAYS).longValue();
            break;
            case INVALID:
            break;
            case SAME_AS_REPAYMENT_PERIOD:
            break;
        }

        return periodsInOneYear;
    }

    private int calculateNumberOfRepaymentsWithPrincipalPayment() {
        return this.numberOfRepayments - getPrincipalGrace();
    }

    private Integer calculateNumberOfRepaymentPeriodsWhereInterestPaymentIsDue(final Integer totalNumberOfRepaymentPeriods) {
        return totalNumberOfRepaymentPeriods - Math.max(getInterestChargingGrace(), getInterestPaymentGrace());
    }

    private Integer calculateNumberOfPrincipalPaymentPeriods(final Integer totalNumberOfRepaymentPeriods) {
        return totalNumberOfRepaymentPeriods - getPrincipalGrace();
    }

    public boolean isPrincipalGraceApplicableForThisPeriod(final int periodNumber) {
        return periodNumber > 0 && periodNumber <= getPrincipalGrace();
    }

    private boolean isInterestPaymentGraceApplicableForThisPeriod(final int periodNumber) {
        return periodNumber > 0 && periodNumber <= getInterestPaymentGrace();
    }

    private boolean isFirstPeriodAfterInterestPaymentGracePeriod(final int periodNumber) {
        return periodNumber > 0 && periodNumber == getInterestPaymentGrace() + 1;
    }

    private boolean isInterestFreeGracePeriod(final int periodNumber) {
        return periodNumber > 0 && periodNumber <= getInterestChargingGrace();
    }

    public Integer getPrincipalGrace() {
        Integer graceOnPrincipalPayments = Integer.valueOf(0);
        if (this.principalGrace != null) {
            graceOnPrincipalPayments = this.principalGrace;
        }
        return graceOnPrincipalPayments;
    }

    public Integer getInterestPaymentGrace() {
        Integer graceOnInterestPayments = Integer.valueOf(0);
        if (this.interestPaymentGrace != null) {
            graceOnInterestPayments = this.interestPaymentGrace;
        }
        return graceOnInterestPayments;
    }

    public Integer getInterestChargingGrace() {
        Integer graceOnInterestCharged = Integer.valueOf(0);
        if (this.interestChargingGrace != null) {
            graceOnInterestCharged = this.interestChargingGrace;
        }
        return graceOnInterestCharged;
    }

    private double paymentPerPeriod(final BigDecimal periodicInterestRate, final Money balance, final int periodsElapsed) {

        if (this.fixedEmiAmount == null) {
            final double futureValue = 0;
            final double principalDouble = balance.getAmount().multiply(BigDecimal.valueOf(-1)).doubleValue();

            final Integer periodsRemaining = this.numberOfRepayments - periodsElapsed;

            double installmentAmount = FinanicalFunctions.pmt(periodicInterestRate.doubleValue(), periodsRemaining.doubleValue(),
                    principalDouble, futureValue, false);

            if (this.installmentAmountInMultiplesOf != null) {
                installmentAmount = Money.roundToMultiplesOf(installmentAmount, this.installmentAmountInMultiplesOf);
            }
            setFixedEmiAmount(BigDecimal.valueOf(installmentAmount));
        }
        return this.fixedEmiAmount.doubleValue();
    }

    private Money calculateDecliningInterestDueForInstallmentBeforeApplyingGrace(final PaymentPeriodsInOneYearCalculator calculator,
            final MathContext mc, final int daysInPeriod, final Money outstandingBalance) {

        Money interestDue = Money.zero(outstandingBalance.getCurrency());

        final BigDecimal periodicInterestRate = periodicInterestRate(calculator, mc, daysInPeriod, this.daysInMonthType,
                this.daysInYearType);
        interestDue = outstandingBalance.multiplyRetainScale(periodicInterestRate, mc.getRoundingMode());

        return interestDue;
    }

    private Money calculateDecliningInterestDueForInstallmentAfterApplyingGrace(final PaymentPeriodsInOneYearCalculator calculator,
            final double interestCalculationGraceOnRepaymentPeriodFraction, final MathContext mc, final int daysInPeriod,
            final Money outstandingBalance, final int periodNumber) {

        Money interest = calculateDecliningInterestDueForInstallmentBeforeApplyingGrace(calculator, mc, daysInPeriod, outstandingBalance);

        if (isInterestPaymentGraceApplicableForThisPeriod(periodNumber)) {
            interest = interest.zero();
        }

        Double fraction = interestCalculationGraceOnRepaymentPeriodFraction;

        if (isInterestFreeGracePeriod(periodNumber)) {
            interest = interest.zero();
        } else if (isInterestFreeGracePeriodFromDate(interestCalculationGraceOnRepaymentPeriodFraction)) {

            if (interestCalculationGraceOnRepaymentPeriodFraction >= Integer.valueOf(1).doubleValue()) {
                interest = interest.zero();
                fraction = fraction - Integer.valueOf(1).doubleValue();

            } else if (interestCalculationGraceOnRepaymentPeriodFraction > Double.valueOf("0.25")
                    && interestCalculationGraceOnRepaymentPeriodFraction < Integer.valueOf(1).doubleValue()) {

                final Money graceOnInterestForRepaymentPeriod = interest.multipliedBy(interestCalculationGraceOnRepaymentPeriodFraction);
                interest = interest.minus(graceOnInterestForRepaymentPeriod);
                fraction = Double.valueOf("0");
            }
        }

        return interest;
    }

    private boolean isInterestFreeGracePeriodFromDate(final double interestCalculationGraceOnRepaymentPeriodFraction) {
        return this.interestChargedFromDate != null && interestCalculationGraceOnRepaymentPeriodFraction > Double.valueOf("0.0");
    }

    private Money calculateEqualPrincipalDueForInstallment(final MathContext mc, final int periodNumber) {

        final Integer numberOfPrincipalPaymentPeriods = calculateNumberOfPrincipalPaymentPeriods(this.numberOfRepayments);

        Money principal = this.principal.dividedBy(numberOfPrincipalPaymentPeriods, mc.getRoundingMode());
        if (isPrincipalGraceApplicableForThisPeriod(periodNumber)) {
            principal = principal.zero();
        }
        return principal;
    }

    private Money calculatePrincipalDueForInstallment(final int periodNumber, final Money totalDuePerInstallment, final Money periodInterest) {

        Money principal = totalDuePerInstallment.minus(periodInterest);
        if (isPrincipalGraceApplicableForThisPeriod(periodNumber)) {
            principal = principal.zero();
        }
        return principal;
    }

    private Money calculateTotalDueForEqualInstallmentRepaymentPeriod(final BigDecimal periodicInterestRate, final Money balance,
            final int periodsElapsed) {

        final double paymentPerRepaymentPeriod = paymentPerPeriod(periodicInterestRate, balance, periodsElapsed);

        return Money.of(balance.getCurrency(), BigDecimal.valueOf(paymentPerRepaymentPeriod));
    }

    public LoanProductRelatedDetail toLoanProductRelatedDetail() {
        final MonetaryCurrency currency = new MonetaryCurrency(this.currency.getCode(), this.currency.getDecimalPlaces(),
                this.currency.getCurrencyInMultiplesOf());

        return LoanProductRelatedDetail.createFrom(currency, this.principal.getAmount(), this.interestRatePerPeriod,
                this.interestRatePeriodFrequencyType, this.annualNominalInterestRate, this.interestMethod,
                this.interestCalculationPeriodMethod, this.repaymentEvery, this.repaymentPeriodFrequencyType, this.numberOfRepayments,
                this.principalGrace, this.interestPaymentGrace, this.interestChargingGrace, this.amortizationMethod,
                this.inArrearsTolerance.getAmount(), this.graceOnArrearsAgeing, this.daysInMonthType.getValue(),
                this.daysInYearType.getValue(), this.interestRecalculationEnabled);
    }

    public Integer getLoanTermFrequency() {
        return this.loanTermFrequency;
    }

    public PeriodFrequencyType getLoanTermPeriodFrequencyType() {
        return this.loanTermPeriodFrequencyType;
    }

    public Integer getRepaymentEvery() {
        return this.repaymentEvery;
    }

    public PeriodFrequencyType getRepaymentPeriodFrequencyType() {
        return this.repaymentPeriodFrequencyType;
    }

    public Date getRepaymentStartFromDate() {
        Date dateValue = null;
        if (this.repaymentsStartingFromDate != null) {
            dateValue = this.repaymentsStartingFromDate.toDate();
        }
        return dateValue;
    }

    public Date getInterestChargedFromDate() {
        Date dateValue = null;
        if (this.interestChargedFromDate != null) {
            dateValue = this.interestChargedFromDate.toDate();
        }
        return dateValue;
    }

    public void setPrincipal(Money principal) {
        this.principal = principal;
    }

    public LocalDate getInterestChargedFromLocalDate() {
        return this.interestChargedFromDate;
    }

    public InterestMethod getInterestMethod() {
        return this.interestMethod;
    }

    public AmortizationMethod getAmortizationMethod() {
        return this.amortizationMethod;
    }

    public MonetaryCurrency getCurrency() {
        return this.principal.getCurrency();
    }

    public Integer getNumberOfRepayments() {
        return this.numberOfRepayments;
    }

    public LocalDate getExpectedDisbursementDate() {
        return this.expectedDisbursementDate;
    }

    public LocalDate getRepaymentsStartingFromLocalDate() {
        return this.repaymentsStartingFromDate;
    }

    public LocalDate getCalculatedRepaymentsStartingFromLocalDate() {
        return this.calculatedRepaymentsStartingFromDate;
    }

    public Money getPrincipal() {
        return this.principal;
    }

    public List<DisbursementData> getDisbursementDatas() {
        return this.disbursementDatas;
    }

    public boolean isMultiDisburseLoan() {
        return this.multiDisburseLoan;
    }

    public Money getMaxOutstandingBalance() {
        return Money.of(getCurrency(), this.maxOutstandingBalance);
    }

    public BigDecimal getFixedEmiAmount() {
        return this.fixedEmiAmount;
    }

    public Integer getNthDay() {
        return this.nthDay;
    }

    public DayOfWeekType getWeekDayType() {
        return this.weekDayType;
    }

    public void setFixedEmiAmountForPeriod(LocalDate periodDate) {
        LocalDate startDate = expectedDisbursementDate;
        for (LoanTermVariationsData loanVariationTermsData : this.emiAmountVariations) {
            if (!periodDate.isBefore(loanVariationTermsData.getTermApplicableFrom())
                    && !startDate.isAfter(loanVariationTermsData.getTermApplicableFrom())) {
                if (loanVariationTermsData.getTermValue() != null) {
                    this.fixedEmiAmount = loanVariationTermsData.getTermValue();
                    startDate = loanVariationTermsData.getTermApplicableFrom();
                }
            }
        }
    }

    public void setFixedEmiAmount(BigDecimal fixedEmiAmount) {
        this.fixedEmiAmount = fixedEmiAmount;
    }

    public void resetFixedEmiAmount() {
        this.fixedEmiAmount = this.actualFixedEmiAmount;
    }

    public LoanRescheduleStrategyMethod getLoanRescheduleStrategyMethod() {
        return LoanRescheduleStrategyMethod.REDUCE_EMI_AMOUNT;
    }

    public boolean isInterestRecalculationEnabled() {
        return this.interestRecalculationEnabled;
    }

    public LoanRescheduleStrategyMethod getRescheduleStrategyMethod() {
        return this.rescheduleStrategyMethod;
    }

    public InterestRecalculationCompoundingMethod getInterestRecalculationCompoundingMethod() {
        return this.interestRecalculationCompoundingMethod;
    }

    public CalendarInstance getRestCalendarInstance() {
        return this.restCalendarInstance;
    }

    private boolean isFallingInRepaymentPeriod(LocalDate fromDate, LocalDate toDate) {
        boolean isSameAsRepaymentPeriod = false;
        if (this.interestCalculationPeriodMethod.getValue().equals(InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD.getValue())) {
            switch (this.repaymentPeriodFrequencyType) {
                case WEEKS:
                    int days = Days.daysBetween(fromDate, toDate).getDays();
                    isSameAsRepaymentPeriod = (days % 7) == 0;
                break;
                case MONTHS:
                    boolean isFromDateOnEndDate = false;
                    if (fromDate.getDayOfMonth() > fromDate.plusDays(1).getDayOfMonth()) {
                        isFromDateOnEndDate = true;
                    }
                    boolean isToDateOnEndDate = false;
                    if (toDate.getDayOfMonth() > toDate.plusDays(1).getDayOfMonth()) {
                        isToDateOnEndDate = true;
                    }

                    if (isFromDateOnEndDate && isToDateOnEndDate) {
                        isSameAsRepaymentPeriod = true;
                    } else {

                        int months = getPeriodsBetween(fromDate, toDate);
                        fromDate = fromDate.plusMonths(months);
                        isSameAsRepaymentPeriod = fromDate.isEqual(toDate);
                    }

                break;
                default:
                break;
            }
        }
        return isSameAsRepaymentPeriod;
    }

    private Integer getPeriodsBetween(LocalDate fromDate, LocalDate toDate) {
        Integer numberOfPeriods = 0;
        PeriodType periodType = PeriodType.yearMonthDay();
        Period difference = new Period(fromDate, toDate, periodType);
        switch (this.repaymentPeriodFrequencyType) {
            case DAYS:
                numberOfPeriods = difference.getDays();
            break;
            case WEEKS:
                periodType = PeriodType.weeks();
                difference = new Period(fromDate, toDate, periodType);
                numberOfPeriods = difference.getWeeks();
            break;
            case MONTHS:
                numberOfPeriods = difference.getMonths();
            break;
            case YEARS:
                numberOfPeriods = difference.getYears();
            break;
            default:
            break;
        }
        return numberOfPeriods;
    }

    public RecalculationFrequencyType getRecalculationFrequencyType() {
        return this.recalculationFrequencyType;
    }

    public void updateNumberOfRepayments(final Integer numberOfRepayments) {
        this.numberOfRepayments = numberOfRepayments;
    }

    public void updatePrincipalGrace(final Integer principalGrace) {
        this.principalGrace = principalGrace;
    }

    public void updateInterestPaymentGrace(final Integer interestPaymentGrace) {
        this.interestPaymentGrace = interestPaymentGrace;
    }

    public void updateInterestRatePerPeriod(BigDecimal interestRatePerPeriod) {
        if (interestRatePerPeriod != null) {
            this.interestRatePerPeriod = interestRatePerPeriod;
        }
    }

    public void updateAnnualNominalInterestRate(BigDecimal annualNominalInterestRate) {
        if (annualNominalInterestRate != null) {
            this.annualNominalInterestRate = annualNominalInterestRate;
        }
    }

    public BigDecimal getInterestRatePerPeriod() {
        return this.interestRatePerPeriod;
    }

    public BigDecimal getAnnualNominalInterestRate() {
        return this.annualNominalInterestRate;
    }

    public void updateInterestChargedFromDate(LocalDate interestChargedFromDate) {
        if (interestChargedFromDate != null) {
            this.interestChargedFromDate = interestChargedFromDate;
        }
    }

    public void updateLoanTermFrequency(Integer loanTermFrequency) {
        if (loanTermFrequency != null) {
            this.loanTermFrequency = loanTermFrequency;
        }
    }

    public void updateTotalInterestDue(Money totalInterestDue) {

        if (totalInterestDue != null) {
            this.totalInterestDue = totalInterestDue;
        }
    }

    public ApplicationCurrency getApplicationCurrency() {
        return this.currency;
    }

    public InterestCalculationPeriodMethod getInterestCalculationPeriodMethod() {
        return this.interestCalculationPeriodMethod;
    }

    public LoanPreClosureInterestCalculationStrategy getPreClosureInterestCalculationStrategy() {
        return this.preClosureInterestCalculationStrategy;
    }
}