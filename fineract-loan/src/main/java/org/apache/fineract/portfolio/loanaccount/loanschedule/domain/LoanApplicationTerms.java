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
package org.apache.fineract.portfolio.loanaccount.loanschedule.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrency;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.calendar.data.CalendarHistoryDataWrapper;
import org.apache.fineract.portfolio.calendar.domain.Calendar;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstance;
import org.apache.fineract.portfolio.calendar.service.CalendarUtils;
import org.apache.fineract.portfolio.common.domain.DayOfWeekType;
import org.apache.fineract.portfolio.common.domain.DaysInMonthType;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.common.domain.NthDayType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanaccount.data.DisbursementData;
import org.apache.fineract.portfolio.loanaccount.data.HolidayDetailDTO;
import org.apache.fineract.portfolio.loanaccount.data.LoanTermVariationsData;
import org.apache.fineract.portfolio.loanaccount.data.LoanTermVariationsDataWrapper;
import org.apache.fineract.portfolio.loanproduct.domain.AmortizationMethod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestCalculationPeriodMethod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestMethod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestRecalculationCompoundingMethod;
import org.apache.fineract.portfolio.loanproduct.domain.LoanPreClosureInterestCalculationStrategy;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.apache.fineract.portfolio.loanproduct.domain.LoanRescheduleStrategyMethod;
import org.apache.fineract.portfolio.loanproduct.domain.RecalculationFrequencyType;

@Slf4j
public final class LoanApplicationTerms {

    private final ApplicationCurrency currency;

    private final Calendar loanCalendar;
    private Integer loanTermFrequency;
    private final PeriodFrequencyType loanTermPeriodFrequencyType;
    private Integer numberOfRepayments;
    private Integer actualNumberOfRepayments;
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
    private final boolean allowPartialPeriodInterestCalcualtion;

    private Money principal;
    private final LocalDate expectedDisbursementDate;
    private final LocalDate repaymentsStartingFromDate;
    private final LocalDate calculatedRepaymentsStartingFromDate;
    /**
     * Integer representing the number of 'repayment frequencies' or installments where 'grace' should apply to the
     * principal component of a loans repayment period (installment).
     */
    private Integer principalGrace;
    private Integer recurringMoratoriumOnPrincipalPeriods;

    /**
     * Integer representing the number of 'repayment frequencies' or installments where 'grace' should apply to the
     * payment of interest in a loans repayment period (installment).
     *
     * <b>Note:</b> Interest is still calculated taking into account the full loan term, the interest is simply offset
     * to a later period.
     */
    private Integer interestPaymentGrace;

    /**
     * Integer representing the number of 'repayment frequencies' or installments where 'grace' should apply to the
     * charging of interest in a loans repayment period (installment).
     *
     * <b>Note:</b> The loan is <i>interest-free</i> for the period of time indicated.
     */
    private final Integer interestChargingGrace;

    /**
     * Legacy method of support 'grace' on the charging of interest on a loan.
     *
     * <p>
     * For the typical structured loan, its reasonable to use an integer to indicate the number of 'repayment frequency'
     * periods the 'grace' should apply to but for slightly <b>irregular</b> loans where the period between disbursement
     * and the date of the 'first repayment period' isnt doest match the 'repayment frequency' but can be less (15days
     * instead of 1 month) or more (6 weeks instead of 1 month) - The idea was to use a date to indicate from whence
     * interest should be charged.
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

    private BigDecimal fixedPrincipalAmount;

    private BigDecimal currentPeriodFixedEmiAmount;

    private BigDecimal currentPeriodFixedPrincipalAmount;

    private final BigDecimal actualFixedEmiAmount;

    private final BigDecimal maxOutstandingBalance;

    private Money totalInterestDue;

    private final DaysInMonthType daysInMonthType;

    private final DaysInYearType daysInYearType;

    private final boolean interestRecalculationEnabled;

    private final LoanRescheduleStrategyMethod rescheduleStrategyMethod;

    private final InterestRecalculationCompoundingMethod interestRecalculationCompoundingMethod;

    private final CalendarInstance restCalendarInstance;

    private final RecalculationFrequencyType recalculationFrequencyType;

    private final CalendarInstance compoundingCalendarInstance;

    private final RecalculationFrequencyType compoundingFrequencyType;
    private final boolean allowCompoundingOnEod;

    private final BigDecimal principalThresholdForLastInstalment;
    private final Integer installmentAmountInMultiplesOf;

    private final LoanPreClosureInterestCalculationStrategy preClosureInterestCalculationStrategy;

    private Money approvedPrincipal = null;

    private final LoanTermVariationsDataWrapper variationsDataWrapper;

    private Money adjustPrincipalForFlatLoans;

    private LocalDate seedDate;

    private final CalendarHistoryDataWrapper calendarHistoryDataWrapper;

    private final Boolean isInterestChargedFromDateSameAsDisbursalDateEnabled;

    private final Integer numberOfDays;

    private final boolean isSkipRepaymentOnFirstDayOfMonth;

    private final boolean isFirstRepaymentDateAllowedOnHoliday;

    private final boolean isInterestToBeRecoveredFirstWhenGreaterThanEMI;

    private boolean isPrincipalCompoundingDisabledForOverdueLoans;

    private final HolidayDetailDTO holidayDetailDTO;

    private final Set<Integer> periodNumbersApplicableForPrincipalGrace = new HashSet<>();

    private final Set<Integer> periodNumbersApplicableForInterestGrace = new HashSet<>();

    // used for FLAT loans when interest rate changed
    private Integer excludePeriodsForCalculation = 0;
    private Money totalPrincipalAccountedForInterestCalcualtion;

    // used for FLAT loans generation on modifying terms
    private Money totalPrincipalAccounted;
    private Money totalInterestAccounted;
    private int periodsCompleted = 0;
    private int extraPeriods = 0;
    private boolean isEqualAmortization;
    private Money interestTobeApproppriated;
    private final BigDecimal fixedPrincipalPercentagePerInstallment;

    private LocalDate newScheduledDueDateStart;
    private boolean isDownPaymentEnabled;
    private BigDecimal disbursedAmountPercentageForDownPayment;
    private boolean isAutoRepaymentForDownPaymentEnabled;

    public static LoanApplicationTerms assembleFrom(final ApplicationCurrency currency, final Integer loanTermFrequency,
            final PeriodFrequencyType loanTermPeriodFrequencyType, final Integer numberOfRepayments, final Integer repaymentEvery,
            final PeriodFrequencyType repaymentPeriodFrequencyType, Integer nthDay, DayOfWeekType weekDayType,
            final AmortizationMethod amortizationMethod, final InterestMethod interestMethod, final BigDecimal interestRatePerPeriod,
            final PeriodFrequencyType interestRatePeriodFrequencyType, final BigDecimal annualNominalInterestRate,
            final InterestCalculationPeriodMethod interestCalculationPeriodMethod, final boolean allowPartialPeriodInterestCalcualtion,
            final Money principalMoney, final LocalDate expectedDisbursementDate, final LocalDate repaymentsStartingFromDate,
            final LocalDate calculatedRepaymentsStartingFromDate, final Integer graceOnPrincipalPayment,
            final Integer recurringMoratoriumOnPrincipalPeriods, final Integer graceOnInterestPayment, final Integer graceOnInterestCharged,
            final LocalDate interestChargedFromDate, final Money inArrearsTolerance, final boolean multiDisburseLoan,
            final BigDecimal emiAmount, final List<DisbursementData> disbursementDatas, final BigDecimal maxOutstandingBalance,
            final Integer graceOnArrearsAgeing, final DaysInMonthType daysInMonthType, final DaysInYearType daysInYearType,
            final boolean isInterestRecalculationEnabled, final RecalculationFrequencyType recalculationFrequencyType,
            final CalendarInstance restCalendarInstance,
            final InterestRecalculationCompoundingMethod interestRecalculationCompoundingMethod,
            final CalendarInstance compoundingCalendarInstance, final RecalculationFrequencyType compoundingFrequencyType,
            final BigDecimal principalThresholdForLastInstalment, final Integer installmentAmountInMultiplesOf,
            final LoanPreClosureInterestCalculationStrategy preClosureInterestCalculationStrategy, final Calendar loanCalendar,
            BigDecimal approvedAmount, List<LoanTermVariationsData> loanTermVariations,
            Boolean isInterestChargedFromDateSameAsDisbursalDateEnabled, final Integer numberOfDays,
            boolean isSkipRepaymentOnFirstDayOfMonth, final HolidayDetailDTO holidayDetailDTO, final boolean allowCompoundingOnEod,
            final boolean isEqualAmortization, final boolean isInterestToBeRecoveredFirstWhenGreaterThanEMI,
            final BigDecimal fixedPrincipalPercentagePerInstallment, final boolean isPrincipalCompoundingDisabledForOverdueLoans,
            final Boolean enableDownPayment, final BigDecimal disbursedAmountPercentageForDownPayment,
            final Boolean isAutoRepaymentForDownPaymentEnabled) {

        final LoanRescheduleStrategyMethod rescheduleStrategyMethod = null;
        final CalendarHistoryDataWrapper calendarHistoryDataWrapper = null;
        return new LoanApplicationTerms(currency, loanTermFrequency, loanTermPeriodFrequencyType, numberOfRepayments, repaymentEvery,
                repaymentPeriodFrequencyType, nthDay, weekDayType, amortizationMethod, interestMethod, interestRatePerPeriod,
                interestRatePeriodFrequencyType, annualNominalInterestRate, interestCalculationPeriodMethod,
                allowPartialPeriodInterestCalcualtion, principalMoney, expectedDisbursementDate, repaymentsStartingFromDate,
                calculatedRepaymentsStartingFromDate, graceOnPrincipalPayment, recurringMoratoriumOnPrincipalPeriods,
                graceOnInterestPayment, graceOnInterestCharged, interestChargedFromDate, inArrearsTolerance, multiDisburseLoan, emiAmount,
                disbursementDatas, maxOutstandingBalance, graceOnArrearsAgeing, daysInMonthType, daysInYearType,
                isInterestRecalculationEnabled, rescheduleStrategyMethod, interestRecalculationCompoundingMethod, restCalendarInstance,
                recalculationFrequencyType, compoundingCalendarInstance, compoundingFrequencyType, principalThresholdForLastInstalment,
                installmentAmountInMultiplesOf, preClosureInterestCalculationStrategy, loanCalendar, approvedAmount, loanTermVariations,
                calendarHistoryDataWrapper, isInterestChargedFromDateSameAsDisbursalDateEnabled, numberOfDays,
                isSkipRepaymentOnFirstDayOfMonth, holidayDetailDTO, allowCompoundingOnEod, isEqualAmortization, false,
                isInterestToBeRecoveredFirstWhenGreaterThanEMI, fixedPrincipalPercentagePerInstallment,
                isPrincipalCompoundingDisabledForOverdueLoans, enableDownPayment, disbursedAmountPercentageForDownPayment,
                isAutoRepaymentForDownPaymentEnabled);

    }

    public static LoanApplicationTerms assembleFrom(final ApplicationCurrency applicationCurrency, final Integer loanTermFrequency,
            final PeriodFrequencyType loanTermPeriodFrequencyType, NthDayType nthDay, DayOfWeekType dayOfWeek,
            final LocalDate expectedDisbursementDate, final LocalDate repaymentsStartingFromDate,
            final LocalDate calculatedRepaymentsStartingFromDate, final Money inArrearsTolerance,
            final LoanProductRelatedDetail loanProductRelatedDetail, final boolean multiDisburseLoan, final BigDecimal emiAmount,
            final List<DisbursementData> disbursementDatas, final BigDecimal maxOutstandingBalance, final LocalDate interestChargedFromDate,
            final BigDecimal principalThresholdForLastInstalment, final Integer installmentAmountInMultiplesOf,
            final RecalculationFrequencyType recalculationFrequencyType, final CalendarInstance restCalendarInstance,
            final InterestRecalculationCompoundingMethod compoundingMethod, final CalendarInstance compoundingCalendarInstance,
            final RecalculationFrequencyType compoundingFrequencyType,
            final LoanPreClosureInterestCalculationStrategy loanPreClosureInterestCalculationStrategy,
            final LoanRescheduleStrategyMethod rescheduleStrategyMethod, final Calendar loanCalendar, BigDecimal approvedAmount,
            BigDecimal annualNominalInterestRate, final List<LoanTermVariationsData> loanTermVariations,
            final CalendarHistoryDataWrapper calendarHistoryDataWrapper, final Integer numberOfDays,
            final boolean isSkipRepaymentOnFirstDayOfMonth, final HolidayDetailDTO holidayDetailDTO, final boolean allowCompoundingOnEod,
            final boolean isFirstRepaymentDateAllowedOnHoliday, final boolean isInterestToBeRecoveredFirstWhenGreaterThanEMI,
            final BigDecimal fixedPrincipalPercentagePerInstallment, final boolean isPrincipalCompoundingDisabledForOverdueLoans) {

        final Integer numberOfRepayments = loanProductRelatedDetail.getNumberOfRepayments();
        final Integer repaymentEvery = loanProductRelatedDetail.getRepayEvery();
        final PeriodFrequencyType repaymentPeriodFrequencyType = loanProductRelatedDetail.getRepaymentPeriodFrequencyType();
        final AmortizationMethod amortizationMethod = loanProductRelatedDetail.getAmortizationMethod();
        final InterestMethod interestMethod = loanProductRelatedDetail.getInterestMethod();
        final BigDecimal interestRatePerPeriod = loanProductRelatedDetail.getNominalInterestRatePerPeriod();
        final PeriodFrequencyType interestRatePeriodFrequencyType = loanProductRelatedDetail.getInterestPeriodFrequencyType();
        final InterestCalculationPeriodMethod interestCalculationPeriodMethod = loanProductRelatedDetail
                .getInterestCalculationPeriodMethod();
        final boolean allowPartialPeriodInterestCalcualtion = loanProductRelatedDetail.isAllowPartialPeriodInterestCalcualtion();
        final Money principalMoney = loanProductRelatedDetail.getPrincipal();

        //
        final Integer graceOnPrincipalPayment = loanProductRelatedDetail.graceOnPrincipalPayment();
        final Integer recurringMoratoriumOnPrincipalPeriods = loanProductRelatedDetail.recurringMoratoriumOnPrincipalPeriods();
        final Integer graceOnInterestPayment = loanProductRelatedDetail.graceOnInterestPayment();
        final Integer graceOnInterestCharged = loanProductRelatedDetail.graceOnInterestCharged();

        // Interest recalculation settings
        final DaysInMonthType daysInMonthType = loanProductRelatedDetail.fetchDaysInMonthType();
        final DaysInYearType daysInYearType = loanProductRelatedDetail.fetchDaysInYearType();
        final boolean isInterestRecalculationEnabled = loanProductRelatedDetail.isInterestRecalculationEnabled();
        final boolean isInterestChargedFromDateSameAsDisbursalDateEnabled = false;
        final boolean isEqualAmortization = loanProductRelatedDetail.isEqualAmortization();
        final boolean isDownPaymentEnabled = loanProductRelatedDetail.isEnableDownPayment();
        BigDecimal disbursedAmountPercentageForDownPayment = null;
        boolean isAutoRepaymentForDownPaymentEnabled = false;
        if (isDownPaymentEnabled) {
            disbursedAmountPercentageForDownPayment = loanProductRelatedDetail.getDisbursedAmountPercentageForDownPayment();
            isAutoRepaymentForDownPaymentEnabled = loanProductRelatedDetail.isEnableAutoRepaymentForDownPayment();
        }
        return new LoanApplicationTerms(applicationCurrency, loanTermFrequency, loanTermPeriodFrequencyType, numberOfRepayments,
                repaymentEvery, repaymentPeriodFrequencyType, ((nthDay != null) ? nthDay.getValue() : null), dayOfWeek, amortizationMethod,
                interestMethod, interestRatePerPeriod, interestRatePeriodFrequencyType, annualNominalInterestRate,
                interestCalculationPeriodMethod, allowPartialPeriodInterestCalcualtion, principalMoney, expectedDisbursementDate,
                repaymentsStartingFromDate, calculatedRepaymentsStartingFromDate, graceOnPrincipalPayment,
                recurringMoratoriumOnPrincipalPeriods, graceOnInterestPayment, graceOnInterestCharged, interestChargedFromDate,
                inArrearsTolerance, multiDisburseLoan, emiAmount, disbursementDatas, maxOutstandingBalance,
                loanProductRelatedDetail.getGraceOnDueDate(), daysInMonthType, daysInYearType, isInterestRecalculationEnabled,
                rescheduleStrategyMethod, compoundingMethod, restCalendarInstance, recalculationFrequencyType, compoundingCalendarInstance,
                compoundingFrequencyType, principalThresholdForLastInstalment, installmentAmountInMultiplesOf,
                loanPreClosureInterestCalculationStrategy, loanCalendar, approvedAmount, loanTermVariations, calendarHistoryDataWrapper,
                isInterestChargedFromDateSameAsDisbursalDateEnabled, numberOfDays, isSkipRepaymentOnFirstDayOfMonth, holidayDetailDTO,
                allowCompoundingOnEod, isEqualAmortization, isFirstRepaymentDateAllowedOnHoliday,
                isInterestToBeRecoveredFirstWhenGreaterThanEMI, fixedPrincipalPercentagePerInstallment,
                isPrincipalCompoundingDisabledForOverdueLoans, isDownPaymentEnabled, disbursedAmountPercentageForDownPayment,
                isAutoRepaymentForDownPaymentEnabled);
    }

    private LoanApplicationTerms(final ApplicationCurrency currency, final Integer loanTermFrequency,
            final PeriodFrequencyType loanTermPeriodFrequencyType, final Integer numberOfRepayments, final Integer repaymentEvery,
            final PeriodFrequencyType repaymentPeriodFrequencyType, final Integer nthDay, final DayOfWeekType weekDayType,
            final AmortizationMethod amortizationMethod, final InterestMethod interestMethod, final BigDecimal interestRatePerPeriod,
            final PeriodFrequencyType interestRatePeriodFrequencyType, final BigDecimal annualNominalInterestRate,
            final InterestCalculationPeriodMethod interestCalculationPeriodMethod, final boolean allowPartialPeriodInterestCalcualtion,
            final Money principal, final LocalDate expectedDisbursementDate, final LocalDate repaymentsStartingFromDate,
            final LocalDate calculatedRepaymentsStartingFromDate, final Integer principalGrace,
            final Integer recurringMoratoriumOnPrincipalPeriods, final Integer interestPaymentGrace, final Integer interestChargingGrace,
            final LocalDate interestChargedFromDate, final Money inArrearsTolerance, final boolean multiDisburseLoan,
            final BigDecimal emiAmount, final List<DisbursementData> disbursementDatas, final BigDecimal maxOutstandingBalance,
            final Integer graceOnArrearsAgeing, final DaysInMonthType daysInMonthType, final DaysInYearType daysInYearType,
            final boolean isInterestRecalculationEnabled, final LoanRescheduleStrategyMethod rescheduleStrategyMethod,
            final InterestRecalculationCompoundingMethod interestRecalculationCompoundingMethod,
            final CalendarInstance restCalendarInstance, final RecalculationFrequencyType recalculationFrequencyType,
            final CalendarInstance compoundingCalendarInstance, final RecalculationFrequencyType compoundingFrequencyType,
            final BigDecimal principalThresholdForLastInstalment, final Integer installmentAmountInMultiplesOf,
            final LoanPreClosureInterestCalculationStrategy preClosureInterestCalculationStrategy, final Calendar loanCalendar,
            BigDecimal approvedAmount, List<LoanTermVariationsData> loanTermVariations,
            final CalendarHistoryDataWrapper calendarHistoryDataWrapper, Boolean isInterestChargedFromDateSameAsDisbursalDateEnabled,
            final Integer numberOfDays, final boolean isSkipRepaymentOnFirstDayOfMonth, final HolidayDetailDTO holidayDetailDTO,
            final boolean allowCompoundingOnEod, final boolean isEqualAmortization, final boolean isFirstRepaymentDateAllowedOnHoliday,
            final boolean isInterestToBeRecoveredFirstWhenGreaterThanEMI, final BigDecimal fixedPrincipalPercentagePerInstallment,
            final boolean isPrincipalCompoundingDisabledForOverdueLoans, final boolean isDownPaymentEnabled,
            final BigDecimal disbursedAmountPercentageForDownPayment, final boolean isAutoRepaymentForDownPaymentEnabled) {

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
        this.allowPartialPeriodInterestCalcualtion = allowPartialPeriodInterestCalcualtion;

        this.principal = principal;
        this.expectedDisbursementDate = expectedDisbursementDate;
        this.repaymentsStartingFromDate = repaymentsStartingFromDate;
        this.calculatedRepaymentsStartingFromDate = calculatedRepaymentsStartingFromDate;

        this.principalGrace = principalGrace;
        this.recurringMoratoriumOnPrincipalPeriods = recurringMoratoriumOnPrincipalPeriods;
        this.interestPaymentGrace = interestPaymentGrace;
        this.interestChargingGrace = interestChargingGrace;
        this.interestChargedFromDate = interestChargedFromDate;

        this.inArrearsTolerance = inArrearsTolerance;
        this.multiDisburseLoan = multiDisburseLoan;
        this.fixedEmiAmount = emiAmount;
        this.actualFixedEmiAmount = emiAmount;
        this.disbursementDatas = disbursementDatas;
        this.maxOutstandingBalance = maxOutstandingBalance;
        this.graceOnArrearsAgeing = graceOnArrearsAgeing;
        this.daysInMonthType = daysInMonthType;
        this.daysInYearType = daysInYearType;
        this.interestRecalculationEnabled = isInterestRecalculationEnabled;
        this.rescheduleStrategyMethod = rescheduleStrategyMethod;
        this.interestRecalculationCompoundingMethod = interestRecalculationCompoundingMethod;
        this.restCalendarInstance = restCalendarInstance;
        this.compoundingCalendarInstance = compoundingCalendarInstance;
        this.recalculationFrequencyType = recalculationFrequencyType;
        this.compoundingFrequencyType = compoundingFrequencyType;
        this.principalThresholdForLastInstalment = principalThresholdForLastInstalment;
        this.installmentAmountInMultiplesOf = installmentAmountInMultiplesOf;
        this.preClosureInterestCalculationStrategy = preClosureInterestCalculationStrategy;
        this.isSkipRepaymentOnFirstDayOfMonth = isSkipRepaymentOnFirstDayOfMonth;
        this.numberOfDays = numberOfDays;

        this.loanCalendar = loanCalendar;
        this.approvedPrincipal = Money.of(principal.getCurrency(), approvedAmount);
        this.variationsDataWrapper = new LoanTermVariationsDataWrapper(loanTermVariations);
        this.actualNumberOfRepayments = numberOfRepayments + getLoanTermVariations().adjustNumberOfRepayments();
        this.adjustPrincipalForFlatLoans = principal.zero();
        if (this.calculatedRepaymentsStartingFromDate == null) {
            this.seedDate = this.expectedDisbursementDate;
        } else {
            this.seedDate = this.calculatedRepaymentsStartingFromDate;
        }
        this.calendarHistoryDataWrapper = calendarHistoryDataWrapper;
        this.isInterestChargedFromDateSameAsDisbursalDateEnabled = isInterestChargedFromDateSameAsDisbursalDateEnabled;
        this.holidayDetailDTO = holidayDetailDTO;
        this.allowCompoundingOnEod = allowCompoundingOnEod;
        Integer periodNumber = 1;
        updatePeriodNumberApplicableForPrincipalOrInterestGrace(periodNumber);
        updateRecurringMoratoriumOnPrincipalPeriods(periodNumber);
        this.totalPrincipalAccountedForInterestCalcualtion = principal.zero();
        this.totalInterestAccounted = principal.zero();
        this.totalPrincipalAccounted = principal.zero();
        this.isEqualAmortization = isEqualAmortization;
        this.isFirstRepaymentDateAllowedOnHoliday = isFirstRepaymentDateAllowedOnHoliday;
        this.isInterestToBeRecoveredFirstWhenGreaterThanEMI = isInterestToBeRecoveredFirstWhenGreaterThanEMI;
        this.fixedPrincipalPercentagePerInstallment = fixedPrincipalPercentagePerInstallment;
        this.isPrincipalCompoundingDisabledForOverdueLoans = isPrincipalCompoundingDisabledForOverdueLoans;
        this.isDownPaymentEnabled = isDownPaymentEnabled;
        this.disbursedAmountPercentageForDownPayment = disbursedAmountPercentageForDownPayment;
        this.isAutoRepaymentForDownPaymentEnabled = isAutoRepaymentForDownPaymentEnabled;
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
                    MoneyHelper.getRoundingMode());
            if (difference.isLessThan(principalThreshold)) {
                adjusted = principalForPeriod.plus(difference.abs());
            }
        } else if (isLastRepaymentPeriod(this.actualNumberOfRepayments, periodNumber)) {

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
        } else if (isLastRepaymentPeriod(this.actualNumberOfRepayments, periodNumber)) {
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
     * Calculates the total interest to be charged on loan taking into account grace settings.
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

    public Money calculateTotalPrincipalForPeriod(final PaymentPeriodsInOneYearCalculator calculator, final Money outstandingBalance,
            final int periodNumber, final MathContext mc, Money interestForThisInstallment) {

        Money principalForInstallment = this.principal.zero();

        switch (this.interestMethod) {
            case FLAT:
                principalForInstallment = calculateTotalPrincipalPerPeriodWithoutGrace(mc, periodNumber, interestForThisInstallment);
            break;
            case DECLINING_BALANCE:
                switch (this.amortizationMethod) {
                    case EQUAL_INSTALLMENTS:
                        Money totalPmtForThisInstallment = pmtForInstallment(calculator, outstandingBalance, periodNumber, mc);
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

    public Money pmtForInstallment(final PaymentPeriodsInOneYearCalculator calculator, final Money outstandingBalance,
            final int periodNumber, final MathContext mc) {
        // Calculate exact period from disbursement date
        final LocalDate periodStartDate = getExpectedDisbursementDate().withDayOfMonth(1);
        final LocalDate periodEndDate = getPeriodEndDate(periodStartDate);
        // equal installments
        final int periodsElapsed = periodNumber - 1;
        // with periodic interest for default month and year for
        // equal installment
        final BigDecimal periodicInterestRateForRepaymentPeriod = periodicInterestRate(calculator, mc, DaysInMonthType.DAYS_30,
                DaysInYearType.DAYS_365, periodStartDate, periodEndDate, true);
        Money totalPmtForThisInstallment = calculateTotalDueForEqualInstallmentRepaymentPeriod(periodicInterestRateForRepaymentPeriod,
                outstandingBalance, periodsElapsed);
        return totalPmtForThisInstallment;
    }

    private LocalDate getPeriodEndDate(final LocalDate startDate) {
        LocalDate dueRepaymentPeriodDate = startDate;
        switch (this.repaymentPeriodFrequencyType) {
            case DAYS:
                dueRepaymentPeriodDate = startDate.plusDays(this.repaymentEvery);
            break;
            case WEEKS:
                dueRepaymentPeriodDate = startDate.plusWeeks(this.repaymentEvery);
            break;
            case MONTHS:
                dueRepaymentPeriodDate = startDate.plusMonths(this.repaymentEvery);
            break;
            case YEARS:
                dueRepaymentPeriodDate = startDate.plusYears(this.repaymentEvery);
            break;
            case INVALID:
            break;
            case WHOLE_TERM:
                log.error("TODO Implement getPeriodEndDate for WHOLE_TERM");
            break;
        }
        return dueRepaymentPeriodDate;
    }

    public PrincipalInterest calculateTotalInterestForPeriod(final PaymentPeriodsInOneYearCalculator calculator,
            final double interestCalculationGraceOnRepaymentPeriodFraction, final int periodNumber, final MathContext mc,
            final Money cumulatingInterestPaymentDueToGrace, final Money outstandingBalance, final LocalDate periodStartDate,
            final LocalDate periodEndDate) {

        Money interestForInstallment = this.principal.zero();
        Money interestBroughtForwardDueToGrace = cumulatingInterestPaymentDueToGrace.copy();
        InterestMethod interestMethod = this.interestMethod;

        if (this.isEqualAmortization() && this.totalInterestDue != null) {
            interestMethod = InterestMethod.FLAT;
        }
        switch (interestMethod) {
            case FLAT:
                if (this.isEqualAmortization() && this.totalInterestDue != null && this.interestMethod.isDecliningBalance()) {
                    interestForInstallment = flatInterestPerInstallment(mc, this.totalInterestDue);
                } else {
                    switch (this.amortizationMethod) {
                        case EQUAL_INSTALLMENTS:
                            // average out outstanding interest over remaining
                            // instalments where interest is applicable
                            interestForInstallment = calculateTotalFlatInterestForInstallmentAveragingOutGracePeriods(calculator,
                                    periodNumber, mc);
                        break;
                        case EQUAL_PRINCIPAL:
                            // interest follows time-value of money and is
                            // brought
                            // forward to next applicable interest payment
                            // period
                            final PrincipalInterest result = calculateTotalFlatInterestForPeriod(calculator, periodNumber, mc,
                                    interestBroughtForwardDueToGrace);
                            interestForInstallment = result.interest();
                            interestBroughtForwardDueToGrace = result.interestPaymentDueToGrace();
                        break;
                        case INVALID:
                        break;
                    }
                }
            break;
            case DECLINING_BALANCE:

                final Money interestForThisInstallmentBeforeGrace = calculateDecliningInterestDueForInstallmentBeforeApplyingGrace(
                        calculator, mc, outstandingBalance, periodStartDate, periodEndDate);

                final Money interestForThisInstallmentAfterGrace = calculateDecliningInterestDueForInstallmentAfterApplyingGrace(calculator,
                        interestCalculationGraceOnRepaymentPeriodFraction, mc, outstandingBalance, periodNumber, periodStartDate,
                        periodEndDate);

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

    private boolean isLastRepaymentPeriod(final int numberOfRepayments, final int periodNumber) {
        return periodNumber == numberOfRepayments;
    }

    public boolean isLastRepaymentPeriod(final int periodNumber) {
        return periodNumber == this.actualNumberOfRepayments;
    }

    /**
     * general method to calculate totalInterestDue discounting any grace settings
     */
    private Money calculateTotalFlatInterestDueWithoutGrace(final PaymentPeriodsInOneYearCalculator calculator, final MathContext mc) {

        Money totalInterestDue = this.principal.zero();

        switch (this.interestMethod) {
            case FLAT:
                final BigDecimal interestRateForLoanTerm = calculateFlatInterestRateForLoanTerm(calculator, mc);
                totalInterestDue = this.principal.minus(totalPrincipalAccountedForInterestCalcualtion)
                        .multiplyRetainScale(interestRateForLoanTerm, mc.getRoundingMode());

            break;
            case DECLINING_BALANCE:
            break;
            case INVALID:
            break;
        }

        return totalInterestDue;
    }

    private BigDecimal calculateFlatInterestRateForLoanTerm(final PaymentPeriodsInOneYearCalculator calculator, final MathContext mc) {

        final BigDecimal divisor = BigDecimal.valueOf(Double.parseDouble("100.0"));

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

                final int periodsInLoanTermInteger = Math.toIntExact(ChronoUnit.DAYS.between(loanStartDate, this.loanEndDate));
                periodsInLoanTerm = BigDecimal.valueOf(periodsInLoanTermInteger);
            break;
            case INVALID:
            break;
            case SAME_AS_REPAYMENT_PERIOD:
                if (this.allowPartialPeriodInterestCalcualtion) {
                    LocalDate startDate = getExpectedDisbursementDate();
                    if (getInterestChargedFromDate() != null) {
                        startDate = getInterestChargedFromLocalDate();
                    }
                    periodsInLoanTerm = calculatePeriodsBetweenDates(startDate, this.loanEndDate);
                }
            break;
        }

        return periodsInLoanTerm;
    }

    public BigDecimal calculatePeriodsBetweenDates(final LocalDate startDate, final LocalDate endDate) {
        BigDecimal numberOfPeriods = BigDecimal.ZERO;
        switch (this.repaymentPeriodFrequencyType) {
            case DAYS:
                int numOfDays = Math.toIntExact(ChronoUnit.DAYS.between(startDate, endDate));
                numberOfPeriods = BigDecimal.valueOf((double) numOfDays);
            break;
            case WEEKS:
                int numberOfWeeks = Math.toIntExact(ChronoUnit.WEEKS.between(startDate, endDate));
                int daysLeftAfterWeeks = Math.toIntExact(ChronoUnit.DAYS.between(startDate.plusWeeks(numberOfWeeks), endDate));
                numberOfPeriods = numberOfPeriods.add(BigDecimal.valueOf(numberOfWeeks))
                        .add(BigDecimal.valueOf((double) daysLeftAfterWeeks / 7));
            break;
            case MONTHS:
                int numberOfMonths = Math.toIntExact(ChronoUnit.MONTHS.between(startDate, endDate));
                LocalDate startDateAfterConsideringMonths = null;
                LocalDate endDateAfterConsideringMonths = null;
                int diffDays = 0;
                if (this.loanCalendar == null) {
                    startDateAfterConsideringMonths = startDate.plusMonths(numberOfMonths);
                    startDateAfterConsideringMonths = (LocalDate) CalendarUtils.adjustDate(startDateAfterConsideringMonths, getSeedDate(),
                            this.repaymentPeriodFrequencyType);
                    endDateAfterConsideringMonths = startDate.plusMonths(numberOfMonths + 1);
                    endDateAfterConsideringMonths = (LocalDate) CalendarUtils.adjustDate(endDateAfterConsideringMonths, getSeedDate(),
                            this.repaymentPeriodFrequencyType);
                } else {
                    LocalDate expectedStartDate = startDate;
                    if (!CalendarUtils.isValidRedurringDate(loanCalendar.getRecurrence(),
                            loanCalendar.getStartDateLocalDate().minusMonths(getRepaymentEvery()), startDate)) {
                        expectedStartDate = CalendarUtils.getNewRepaymentMeetingDate(loanCalendar.getRecurrence(),
                                startDate.minusMonths(getRepaymentEvery()), startDate.minusMonths(getRepaymentEvery()), getRepaymentEvery(),
                                CalendarUtils.getMeetingFrequencyFromPeriodFrequencyType(getLoanTermPeriodFrequencyType()),
                                this.holidayDetailDTO.getWorkingDays(), isSkipRepaymentOnFirstDayOfMonth, numberOfDays);
                    }
                    if (!expectedStartDate.isEqual(startDate)) {
                        diffDays = Math.toIntExact(ChronoUnit.DAYS.between(startDate, expectedStartDate));
                    }
                    if (numberOfMonths == 0) {
                        startDateAfterConsideringMonths = expectedStartDate;
                    } else {
                        startDateAfterConsideringMonths = CalendarUtils.getNewRepaymentMeetingDate(loanCalendar.getRecurrence(),
                                expectedStartDate, expectedStartDate.plusMonths(numberOfMonths), getRepaymentEvery(),
                                CalendarUtils.getMeetingFrequencyFromPeriodFrequencyType(getLoanTermPeriodFrequencyType()),
                                this.holidayDetailDTO.getWorkingDays(), isSkipRepaymentOnFirstDayOfMonth, numberOfDays);
                    }
                    endDateAfterConsideringMonths = CalendarUtils.getNewRepaymentMeetingDate(loanCalendar.getRecurrence(),
                            startDateAfterConsideringMonths, startDateAfterConsideringMonths.plusDays(1), getRepaymentEvery(),
                            CalendarUtils.getMeetingFrequencyFromPeriodFrequencyType(getLoanTermPeriodFrequencyType()),
                            this.holidayDetailDTO.getWorkingDays(), isSkipRepaymentOnFirstDayOfMonth, numberOfDays);
                }
                int daysLeftAfterMonths = Math.toIntExact(ChronoUnit.DAYS.between(startDateAfterConsideringMonths, endDate)) + diffDays;
                int daysInPeriodAfterMonths = Math
                        .toIntExact(ChronoUnit.DAYS.between(startDateAfterConsideringMonths, endDateAfterConsideringMonths));
                numberOfPeriods = numberOfPeriods.add(BigDecimal.valueOf(numberOfMonths))
                        .add(BigDecimal.valueOf((double) daysLeftAfterMonths / daysInPeriodAfterMonths));
            break;
            case YEARS:
                int numberOfYears = Math.toIntExact(ChronoUnit.YEARS.between(startDate, endDate));
                LocalDate startDateAfterConsideringYears = startDate.plusYears(numberOfYears);
                LocalDate endDateAfterConsideringYears = startDate.plusYears(numberOfYears + 1);
                int daysLeftAfterYears = Math.toIntExact(ChronoUnit.DAYS.between(startDateAfterConsideringYears, endDate));
                int daysInPeriodAfterYears = Math
                        .toIntExact(ChronoUnit.DAYS.between(startDateAfterConsideringYears, endDateAfterConsideringYears));
                numberOfPeriods = numberOfPeriods.add(BigDecimal.valueOf(numberOfYears))
                        .add(BigDecimal.valueOf((double) daysLeftAfterYears / daysInPeriodAfterYears));
            break;
            default:
            break;
        }
        return numberOfPeriods;
    }

    public void updateLoanEndDate(final LocalDate loanEndDate) {
        this.loanEndDate = loanEndDate;
    }

    private Money calculateTotalInterestPerInstallmentWithoutGrace(final PaymentPeriodsInOneYearCalculator calculator,
            final MathContext mc) {

        final Money totalInterestForLoanTerm = calculateTotalFlatInterestDueWithoutGrace(calculator, mc);
        return flatInterestPerInstallment(mc, totalInterestForLoanTerm);
    }

    private Money flatInterestPerInstallment(final MathContext mc, final Money totalInterestForLoanTerm) {
        Money interestPerInstallment = totalInterestForLoanTerm.dividedBy(
                Long.valueOf(this.actualNumberOfRepayments) - defaultToZeroIfNull(this.excludePeriodsForCalculation), mc.getRoundingMode());
        if (this.excludePeriodsForCalculation < this.periodsCompleted) {
            Money interestLeft = this.totalInterestDue.minus(this.totalInterestAccounted);
            interestPerInstallment = interestLeft.dividedBy(
                    Long.valueOf(this.actualNumberOfRepayments) - defaultToZeroIfNull(this.periodsCompleted), mc.getRoundingMode());
        }

        return interestPerInstallment;
    }

    private Money calculateTotalPrincipalPerPeriodWithoutGrace(final MathContext mc, final int periodNumber,
            Money interestForThisInstallment) {
        final int totalRepaymentsWithCapitalPayment = calculateNumberOfRepaymentsWithPrincipalPayment();
        Money principalPerPeriod = null;
        if (getFixedEmiAmount() == null) {
            if (this.fixedPrincipalPercentagePerInstallment != null) {
                principalPerPeriod = this.principal.minus(totalPrincipalAccounted)
                        .percentageOf(this.fixedPrincipalPercentagePerInstallment, mc.getRoundingMode())
                        .plus(this.adjustPrincipalForFlatLoans);
            } else {
                principalPerPeriod = this.principal.minus(totalPrincipalAccounted)
                        .dividedBy(totalRepaymentsWithCapitalPayment, mc.getRoundingMode()).plus(this.adjustPrincipalForFlatLoans);
            }
            if (isPrincipalGraceApplicableForThisPeriod(periodNumber)) {
                principalPerPeriod = principalPerPeriod.zero();
            }
            if (!isPrincipalGraceApplicableForThisPeriod(periodNumber) && currentPeriodFixedPrincipalAmount != null) {
                this.adjustPrincipalForFlatLoans = this.adjustPrincipalForFlatLoans
                        .plus(principalPerPeriod.minus(currentPeriodFixedPrincipalAmount)
                                .dividedBy(this.actualNumberOfRepayments - periodNumber, mc.getRoundingMode()));
                principalPerPeriod = this.principal.zero().plus(currentPeriodFixedPrincipalAmount);

            }
        } else {
            principalPerPeriod = Money.of(this.getCurrency(), getFixedEmiAmount()).minus(interestForThisInstallment);
            return principalPerPeriod;
        }

        return principalPerPeriod;
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
     * calculates the interest that should be due for a given scheduled loan repayment period. It takes into account
     * GRACE periods and calculates how much interest is due per period by averaging the number of periods where
     * interest is due and should be paid against the total known interest that is due without grace.
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

            Integer interestPaymentDuePeriods = calculateNumberOfRemainingInterestPaymentPeriods(this.actualNumberOfRepayments,
                    this.excludePeriodsForCalculation);
            interestForInstallment = realTotalInterestForLoan.dividedBy(BigDecimal.valueOf(interestPaymentDuePeriods),
                    mc.getRoundingMode());
            if (this.excludePeriodsForCalculation < this.periodsCompleted) {
                Money interestLeft = this.totalInterestDue.minus(this.totalInterestAccounted);
                Integer interestDuePeriods = calculateNumberOfRemainingInterestPaymentPeriods(this.actualNumberOfRepayments,
                        this.periodsCompleted);
                interestForInstallment = interestLeft.dividedBy(Long.valueOf(interestDuePeriods), mc.getRoundingMode());
            }
            if (!this.periodNumbersApplicableForInterestGrace.isEmpty()) {
                int periodsElapsed = calculateLastInterestGracePeriod(periodNumber);
                if (periodsElapsed > this.excludePeriodsForCalculation && periodsElapsed > this.periodsCompleted) {
                    Money interestLeft = this.totalInterestDue.minus(this.totalInterestAccounted);
                    Integer interestDuePeriods = calculateNumberOfRemainingInterestPaymentPeriods(this.actualNumberOfRepayments,
                            periodsElapsed);
                    interestForInstallment = interestLeft.dividedBy(Long.valueOf(interestDuePeriods), mc.getRoundingMode());
                }
            }

        }

        return interestForInstallment;
    }

    private BigDecimal periodicInterestRate(final PaymentPeriodsInOneYearCalculator calculator, final MathContext mc,
            final DaysInMonthType daysInMonthType, final DaysInYearType daysInYearType, LocalDate periodStartDate,
            LocalDate periodEndDate) {
        return periodicInterestRate(calculator, mc, daysInMonthType, daysInYearType, periodStartDate, periodEndDate, false);
    }

    private BigDecimal periodicInterestRate(final PaymentPeriodsInOneYearCalculator calculator, final MathContext mc,
            final DaysInMonthType daysInMonthType, final DaysInYearType daysInYearType, LocalDate periodStartDate, LocalDate periodEndDate,
            boolean isForPMT) {

        final long loanTermPeriodsInOneYear = calculatePeriodsInOneYear(calculator);

        final BigDecimal divisor = BigDecimal.valueOf(Double.parseDouble("100.0"));
        final BigDecimal loanTermPeriodsInYearBigDecimal = BigDecimal.valueOf(loanTermPeriodsInOneYear);

        BigDecimal periodicInterestRate = BigDecimal.ZERO;
        BigDecimal loanTermFrequencyBigDecimal = BigDecimal.ONE;
        if (isForPMT) {
            loanTermFrequencyBigDecimal = BigDecimal.valueOf(this.repaymentEvery);
        } else {
            loanTermFrequencyBigDecimal = calculateLoanTermFrequency(periodStartDate, periodEndDate);
        }
        switch (this.interestCalculationPeriodMethod) {
            case INVALID:
            break;
            case DAILY:
                // For daily work out number of days in the period
                BigDecimal numberOfDaysInPeriod = BigDecimal.valueOf(ChronoUnit.DAYS.between(periodStartDate, periodEndDate));

                final BigDecimal oneDayOfYearInterestRate = this.annualNominalInterestRate.divide(loanTermPeriodsInYearBigDecimal, mc)
                        .divide(divisor, mc);

                switch (this.repaymentPeriodFrequencyType) {
                    case INVALID:
                    break;
                    case DAYS:
                        periodicInterestRate = oneDayOfYearInterestRate.multiply(numberOfDaysInPeriod, mc);
                    break;
                    case WEEKS:
                        periodicInterestRate = oneDayOfYearInterestRate.multiply(numberOfDaysInPeriod, mc);
                    break;
                    case MONTHS:
                        if (daysInMonthType.isDaysInMonth_30()) {
                            numberOfDaysInPeriod = loanTermFrequencyBigDecimal.multiply(BigDecimal.valueOf(30), mc);
                        }
                        periodicInterestRate = oneDayOfYearInterestRate.multiply(numberOfDaysInPeriod, mc);
                    break;
                    case YEARS:
                        switch (daysInYearType) {
                            case DAYS_360:
                                numberOfDaysInPeriod = loanTermFrequencyBigDecimal.multiply(BigDecimal.valueOf(360), mc);
                            break;
                            case DAYS_364:
                                numberOfDaysInPeriod = loanTermFrequencyBigDecimal.multiply(BigDecimal.valueOf(364), mc);
                            break;
                            case DAYS_365:
                                numberOfDaysInPeriod = loanTermFrequencyBigDecimal.multiply(BigDecimal.valueOf(365), mc);
                            break;
                            default:
                            break;
                        }
                        periodicInterestRate = oneDayOfYearInterestRate.multiply(numberOfDaysInPeriod, mc);
                    break;
                    case WHOLE_TERM:
                        log.error("TODO Implement periodicInterestRate for WHOLE_TERM");
                    break;
                }
            break;
            case SAME_AS_REPAYMENT_PERIOD:
                periodicInterestRate = this.annualNominalInterestRate.divide(loanTermPeriodsInYearBigDecimal, mc).divide(divisor, mc)
                        .multiply(loanTermFrequencyBigDecimal);
            break;
        }

        return periodicInterestRate;
    }

    private BigDecimal calculateLoanTermFrequency(final LocalDate periodStartDate, final LocalDate periodEndDate) {
        BigDecimal loanTermFrequencyBigDecimal = BigDecimal.valueOf(this.repaymentEvery);
        if (this.interestCalculationPeriodMethod.isDaily() || this.allowPartialPeriodInterestCalcualtion) {
            loanTermFrequencyBigDecimal = calculatePeriodsBetweenDates(periodStartDate, periodEndDate);
        }
        return loanTermFrequencyBigDecimal;
    }

    public BigDecimal interestRateFor(final PaymentPeriodsInOneYearCalculator calculator, final MathContext mc,
            final Money outstandingBalance, final LocalDate fromDate, final LocalDate toDate) {

        long loanTermPeriodsInOneYear = calculator.calculate(PeriodFrequencyType.DAYS).longValue();
        int repaymentEvery = Math.toIntExact(ChronoUnit.DAYS.between(fromDate, toDate));
        if (isFallingInRepaymentPeriod(fromDate, toDate)) {
            loanTermPeriodsInOneYear = calculatePeriodsInOneYear(calculator);
            repaymentEvery = getPeriodsBetween(fromDate, toDate);
        }

        final BigDecimal divisor = BigDecimal.valueOf(Double.parseDouble("100.0"));
        final BigDecimal loanTermPeriodsInYearBigDecimal = BigDecimal.valueOf(loanTermPeriodsInOneYear);
        final BigDecimal oneDayOfYearInterestRate = this.annualNominalInterestRate.divide(loanTermPeriodsInYearBigDecimal, mc)
                .divide(divisor, mc);
        BigDecimal interestRate = oneDayOfYearInterestRate.multiply(BigDecimal.valueOf(repaymentEvery), mc);
        return outstandingBalance.getAmount().multiply(interestRate, mc);
    }

    private long calculatePeriodsInOneYear(final PaymentPeriodsInOneYearCalculator calculator) {

        // check if daysInYears is set if so change periodsInOneYear to days set
        // in db
        long periodsInOneYear;
        boolean daysInYearToUse = (this.repaymentPeriodFrequencyType.getCode().equalsIgnoreCase("periodFrequencyType.days")
                && !this.daysInYearType.getCode().equalsIgnoreCase("DaysInYearType.actual"));
        if (daysInYearToUse) {
            periodsInOneYear = this.daysInYearType.getValue().longValue();
        } else {
            periodsInOneYear = calculator.calculate(this.repaymentPeriodFrequencyType).longValue();
        }
        switch (this.interestCalculationPeriodMethod) {
            case DAILY:
                periodsInOneYear = !this.daysInYearType.getCode().equalsIgnoreCase("DaysInYearType.actual")
                        ? this.daysInYearType.getValue().longValue()
                        : calculator.calculate(PeriodFrequencyType.DAYS).longValue();
            break;
            case INVALID:
            break;
            case SAME_AS_REPAYMENT_PERIOD:
            break;
        }

        return periodsInOneYear;
    }

    private int calculateNumberOfRepaymentsWithPrincipalPayment() {
        int numPeriods = calculateNumberOfRemainingPrincipalPaymentPeriods(this.actualNumberOfRepayments, this.periodsCompleted);
        // numPeriods = numPeriods - this.periodsCompleted;
        return numPeriods;
    }

    private Integer calculateNumberOfRemainingInterestPaymentPeriods(final Integer totalNumberOfRepaymentPeriods, int periodsElapsed) {
        int principalFeePeriods = 0;
        for (Integer intNumber : this.periodNumbersApplicableForInterestGrace) {
            if (intNumber > periodsElapsed) {
                principalFeePeriods++;
            }
        }
        Integer periodsRemaining = totalNumberOfRepaymentPeriods - periodsElapsed - principalFeePeriods;
        /**
         * if grace period available then need to be subtracted
         */
        if (this.interestChargingGrace != null) {
            periodsRemaining = periodsRemaining - this.interestChargingGrace;
        }
        return periodsRemaining;
    }

    private Integer calculateLastInterestGracePeriod(int periodNumber) {
        int lastGracePeriod = 0;
        for (Integer grace : this.periodNumbersApplicableForInterestGrace) {
            if (grace < periodNumber && lastGracePeriod < grace) {
                lastGracePeriod = grace;
            }
        }
        return lastGracePeriod;
    }

    public boolean isPrincipalGraceApplicableForThisPeriod(final int periodNumber) {
        boolean isPrincipalGraceApplicableForThisPeriod = false;
        if (this.periodNumbersApplicableForPrincipalGrace.contains(periodNumber)) {
            isPrincipalGraceApplicableForThisPeriod = true;
        }
        return isPrincipalGraceApplicableForThisPeriod;
    }

    public boolean isInterestPaymentGraceApplicableForThisPeriod(final int periodNumber) {
        boolean isInterestPaymentGraceApplicableForThisPeriod = false;
        if (this.periodNumbersApplicableForInterestGrace.contains(periodNumber)) {
            isInterestPaymentGraceApplicableForThisPeriod = true;
        }
        return isInterestPaymentGraceApplicableForThisPeriod;
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

    public Integer getRecurringMoratoriumOnPrincipalPeriods() {
        Integer recurringMoratoriumOnPrincipalPeriods = Integer.valueOf(0);
        if (this.recurringMoratoriumOnPrincipalPeriods != null) {
            recurringMoratoriumOnPrincipalPeriods = this.recurringMoratoriumOnPrincipalPeriods;
        }
        return recurringMoratoriumOnPrincipalPeriods;
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

        if (getFixedEmiAmount() == null) {
            final double futureValue = 0;
            final double principalDouble = balance.getAmount().multiply(BigDecimal.valueOf(-1)).doubleValue();

            final Integer periodsRemaining = calculateNumberOfRemainingPrincipalPaymentPeriods(this.actualNumberOfRepayments,
                    periodsElapsed);

            double installmentAmount = FinanicalFunctions.pmt(periodicInterestRate.doubleValue(), periodsRemaining.doubleValue(),
                    principalDouble, futureValue, false);

            if (this.installmentAmountInMultiplesOf != null) {
                installmentAmount = Money.roundToMultiplesOf(installmentAmount, this.installmentAmountInMultiplesOf);
            }
            setFixedEmiAmount(BigDecimal.valueOf(installmentAmount));
        }
        return getFixedEmiAmount().doubleValue();
    }

    private Money calculateDecliningInterestDueForInstallmentBeforeApplyingGrace(final PaymentPeriodsInOneYearCalculator calculator,
            final MathContext mc, final Money outstandingBalance, LocalDate periodStartDate, LocalDate periodEndDate) {

        Money interestDue = Money.zero(outstandingBalance.getCurrency());

        final BigDecimal periodicInterestRate = periodicInterestRate(calculator, mc, this.daysInMonthType, this.daysInYearType,
                periodStartDate, periodEndDate);// 0.021232877 ob:14911.64
        interestDue = outstandingBalance.multiplyRetainScale(periodicInterestRate, mc.getRoundingMode());

        return interestDue;
    }

    private Money calculateDecliningInterestDueForInstallmentAfterApplyingGrace(final PaymentPeriodsInOneYearCalculator calculator,
            final double interestCalculationGraceOnRepaymentPeriodFraction, final MathContext mc, final Money outstandingBalance,
            final int periodNumber, LocalDate periodStartDate, LocalDate periodEndDate) {

        Money interest = calculateDecliningInterestDueForInstallmentBeforeApplyingGrace(calculator, mc, outstandingBalance, periodStartDate,
                periodEndDate);

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

            } else if (interestCalculationGraceOnRepaymentPeriodFraction > Double.parseDouble("0.25")
                    && interestCalculationGraceOnRepaymentPeriodFraction < Integer.valueOf(1).doubleValue()) {

                final Money graceOnInterestForRepaymentPeriod = interest.multipliedBy(interestCalculationGraceOnRepaymentPeriodFraction);
                interest = interest.minus(graceOnInterestForRepaymentPeriod);
                fraction = Double.valueOf("0");
            }
        }

        return interest;
    }

    private boolean isInterestFreeGracePeriodFromDate(final double interestCalculationGraceOnRepaymentPeriodFraction) {
        return this.interestChargedFromDate != null && interestCalculationGraceOnRepaymentPeriodFraction > Double.parseDouble("0.0");
    }

    private Money calculateEqualPrincipalDueForInstallment(final MathContext mc, final int periodNumber) {
        Money principal = this.principal;
        if (this.fixedPrincipalAmount == null) {
            final Integer numberOfPrincipalPaymentPeriods = calculateNumberOfRemainingPrincipalPaymentPeriods(this.actualNumberOfRepayments,
                    periodNumber);
            principal = this.principal.dividedBy(numberOfPrincipalPaymentPeriods, mc.getRoundingMode());
            this.fixedPrincipalAmount = principal.getAmount();
        }
        if (this.fixedPrincipalPercentagePerInstallment != null) {
            principal = this.principal.percentageOf(this.fixedPrincipalPercentagePerInstallment, mc.getRoundingMode());
        } else {
            principal = Money.of(getCurrency(), getFixedPrincipalAmount());
        }
        if (isPrincipalGraceApplicableForThisPeriod(periodNumber)) {
            principal = principal.zero();
        }
        return principal;
    }

    public void updateFixedPrincipalAmount(final MathContext mc, final int periodNumber, final Money outstandingAmount) {
        final Integer numberOfPrincipalPaymentPeriods = calculateNumberOfRemainingPrincipalPaymentPeriods(this.actualNumberOfRepayments,
                periodNumber - 1);
        Money principal = outstandingAmount.dividedBy(numberOfPrincipalPaymentPeriods, mc.getRoundingMode());
        this.fixedPrincipalAmount = principal.getAmount();
    }

    private Integer calculateNumberOfRemainingPrincipalPaymentPeriods(final Integer totalNumberOfRepaymentPeriods, int periodsElapsed) {
        int principalFeePeriods = 0;
        for (Integer intNumber : this.periodNumbersApplicableForPrincipalGrace) {
            if (intNumber > periodsElapsed) {
                principalFeePeriods++;
            }
        }
        Integer periodsRemaining = totalNumberOfRepaymentPeriods - periodsElapsed - principalFeePeriods;
        return periodsRemaining;
    }

    public void setFixedPrincipalAmount(BigDecimal fixedPrincipalAmount) {
        this.fixedPrincipalAmount = fixedPrincipalAmount;
    }

    private Money calculatePrincipalDueForInstallment(final int periodNumber, final Money totalDuePerInstallment,
            final Money periodInterest) {
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
                this.interestCalculationPeriodMethod, this.allowPartialPeriodInterestCalcualtion, this.repaymentEvery,
                this.repaymentPeriodFrequencyType, this.numberOfRepayments, this.principalGrace, this.recurringMoratoriumOnPrincipalPeriods,
                this.interestPaymentGrace, this.interestChargingGrace, this.amortizationMethod, this.inArrearsTolerance.getAmount(),
                this.graceOnArrearsAgeing, this.daysInMonthType.getValue(), this.daysInYearType.getValue(),
                this.interestRecalculationEnabled, this.isEqualAmortization, this.isDownPaymentEnabled,
                this.disbursedAmountPercentageForDownPayment, this.isAutoRepaymentForDownPaymentEnabled);
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

    public LocalDate getRepaymentStartFromDate() {
        return this.repaymentsStartingFromDate;
    }

    public LocalDate getInterestChargedFromDate() {
        return this.interestChargedFromDate;
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

    public Money getApprovedPrincipal() {
        return this.approvedPrincipal;
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
        BigDecimal fixedEmiAmount = this.fixedEmiAmount;
        if (getCurrentPeriodFixedEmiAmount() != null) {
            fixedEmiAmount = getCurrentPeriodFixedEmiAmount();
        }
        return fixedEmiAmount;
    }

    public Integer getNthDay() {
        return this.nthDay;
    }

    public DayOfWeekType getWeekDayType() {
        return this.weekDayType;
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
                    int days = Math.toIntExact(ChronoUnit.DAYS.between(fromDate, toDate));
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
        switch (this.repaymentPeriodFrequencyType) {
            case DAYS:
                numberOfPeriods = Math.toIntExact(ChronoUnit.DAYS.between(fromDate, toDate));
            break;
            case WEEKS:
                numberOfPeriods = Math.toIntExact(ChronoUnit.WEEKS.between(fromDate, toDate));
            break;
            case MONTHS:
                numberOfPeriods = Math.toIntExact(ChronoUnit.MONTHS.between(fromDate, toDate));
            break;
            case YEARS:
                numberOfPeriods = Math.toIntExact(ChronoUnit.YEARS.between(fromDate, toDate));
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
        this.actualNumberOfRepayments = numberOfRepayments + getLoanTermVariations().adjustNumberOfRepayments();

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
        this.totalInterestDue = totalInterestDue;
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

    public CalendarInstance getCompoundingCalendarInstance() {
        return this.compoundingCalendarInstance;
    }

    public RecalculationFrequencyType getCompoundingFrequencyType() {
        return this.compoundingFrequencyType;
    }

    public BigDecimal getActualFixedEmiAmount() {
        return this.actualFixedEmiAmount;
    }

    public Calendar getLoanCalendar() {
        return loanCalendar;
    }

    public BigDecimal getFixedPrincipalAmount() {
        BigDecimal fixedPrincipalAmount = this.fixedPrincipalAmount;
        if (getCurrentPeriodFixedPrincipalAmount() != null) {
            fixedPrincipalAmount = getCurrentPeriodFixedPrincipalAmount();
        }
        return fixedPrincipalAmount;
    }

    public LoanTermVariationsDataWrapper getLoanTermVariations() {
        return this.variationsDataWrapper;
    }

    public BigDecimal getCurrentPeriodFixedEmiAmount() {
        return this.currentPeriodFixedEmiAmount;
    }

    public void setCurrentPeriodFixedEmiAmount(BigDecimal currentPeriodFixedEmiAmount) {
        this.currentPeriodFixedEmiAmount = currentPeriodFixedEmiAmount;
    }

    public BigDecimal getCurrentPeriodFixedPrincipalAmount() {
        return this.currentPeriodFixedPrincipalAmount;
    }

    public void setCurrentPeriodFixedPrincipalAmount(BigDecimal currentPeriodFixedPrincipalAmount) {
        this.currentPeriodFixedPrincipalAmount = currentPeriodFixedPrincipalAmount;
    }

    public Integer fetchNumberOfRepaymentsAfterExceptions() {
        return this.actualNumberOfRepayments;
    }

    public LocalDate getSeedDate() {
        return this.seedDate;
    }

    public CalendarHistoryDataWrapper getCalendarHistoryDataWrapper() {
        return this.calendarHistoryDataWrapper;
    }

    public Boolean isInterestChargedFromDateSameAsDisbursalDateEnabled() {
        return this.isInterestChargedFromDateSameAsDisbursalDateEnabled;
    }

    public Integer getNumberOfdays() {
        return numberOfDays;
    }

    public boolean isSkipRepaymentOnFirstDayofMonth() {
        return isSkipRepaymentOnFirstDayOfMonth;
    }

    public HolidayDetailDTO getHolidayDetailDTO() {
        return this.holidayDetailDTO;
    }

    public boolean allowCompoundingOnEod() {
        return this.allowCompoundingOnEod;
    }

    public Money getTotalDisbursedAmount() {
        Money disbursedAmount = Money.zero(getCurrency());
        if (isMultiDisburseLoan()) {
            for (DisbursementData disbursement : getDisbursementDatas()) {
                if (disbursement.isDisbursed()) {
                    disbursedAmount = disbursedAmount.plus(disbursement.getPrincipal());
                }
            }
        } else {
            disbursedAmount = getPrincipal();
        }
        return disbursedAmount;
    }

    public Money getTotalMultiDisbursedAmount() {
        Money disbursedAmount = Money.zero(getCurrency());
        if (isMultiDisburseLoan()) {
            for (DisbursementData disbursement : getDisbursementDatas()) {
                disbursedAmount = disbursedAmount.plus(disbursement.getPrincipal());
            }
        } else {
            disbursedAmount = getPrincipal();
        }
        return disbursedAmount;
    }

    public void updatePeriodNumberApplicableForPrincipalOrInterestGrace(final Integer periodsApplicationForGrace) {
        int applicablePeriodNumber = periodsApplicationForGrace;
        int graceOnPrincipal = defaultToZeroIfNull(this.principalGrace);
        int graceOnInterest = defaultToZeroIfNull(this.interestPaymentGrace);

        while (graceOnPrincipal > 0 || graceOnInterest > 0) {
            if (graceOnPrincipal > 0) {
                this.periodNumbersApplicableForPrincipalGrace.add(applicablePeriodNumber);
            }
            if (graceOnInterest > 0) {
                this.periodNumbersApplicableForInterestGrace.add(applicablePeriodNumber);
            }
            applicablePeriodNumber++;
            graceOnPrincipal--;
            graceOnInterest--;
        }
    }

    /**
     * set the value to zero if the provided value is null
     *
     * @return integer value equal/greater than 0
     **/
    private Integer defaultToZeroIfNull(Integer value) {

        return (value != null) ? value : 0;
    }

    public void updateExcludePeriodsForCalculation(Integer excludePeriodsForCalculation) {
        this.excludePeriodsForCalculation = excludePeriodsForCalculation;
        this.extraPeriods = 0;
    }

    public Integer getActualNoOfRepaymnets() {
        return this.actualNumberOfRepayments;
    }

    public Money getTotalInterestDue() {
        return this.totalInterestDue;
    }

    private void updateRecurringMoratoriumOnPrincipalPeriods(Integer periodNumber) {
        Boolean isPrincipalGraceApplicableForThisPeriod = false;
        Integer numberOfRepayments = this.actualNumberOfRepayments;
        if (this.getRecurringMoratoriumOnPrincipalPeriods() > 0) {
            while (numberOfRepayments > 0) {
                isPrincipalGraceApplicableForThisPeriod = ((periodNumber > 0 && periodNumber <= getPrincipalGrace()) || (periodNumber > 0
                        && (((periodNumber - getPrincipalGrace()) % (this.getRecurringMoratoriumOnPrincipalPeriods() + 1)) != 1)));
                if (isPrincipalGraceApplicableForThisPeriod) {
                    this.periodNumbersApplicableForPrincipalGrace.add(periodNumber);
                }
                numberOfRepayments--;
                periodNumber++;
            }
        }

    }

    public void setTotalPrincipalAccountedForInterestCalculation(Money totalPrincipalAccounted) {
        this.totalPrincipalAccountedForInterestCalcualtion = totalPrincipalAccounted;
    }

    // Used for FLAT loans to calculate principal and interest per installment
    public void updateAccountedTillPeriod(int periodNumber, Money totalPrincipalAccounted, Money totalInterestAccounted,
            int extendPeriods) {
        this.periodsCompleted = periodNumber;
        this.totalPrincipalAccounted = totalPrincipalAccounted;
        this.totalInterestAccounted = totalInterestAccounted;
        this.extraPeriods = this.extraPeriods + extendPeriods;
    }

    public void updateTotalInterestAccounted(Money totalInterestAccounted) {
        this.totalInterestAccounted = totalInterestAccounted;
    }

    public void setSeedDate(LocalDate seedDate) {
        this.seedDate = seedDate;
    }

    public boolean isEqualAmortization() {
        return isEqualAmortization;
    }

    public void setEqualAmortization(boolean isEqualAmortization) {
        this.isEqualAmortization = isEqualAmortization;
    }

    public boolean isFirstRepaymentDateAllowedOnHoliday() {
        return isFirstRepaymentDateAllowedOnHoliday;
    }

    public Money getInterestTobeApproppriated() {
        return interestTobeApproppriated == null ? this.principal.zero() : interestTobeApproppriated;
    }

    public void setInterestTobeApproppriated(Money interestTobeApproppriated) {
        this.interestTobeApproppriated = interestTobeApproppriated;
    }

    public Boolean isInterestTobeApproppriated() {
        return interestTobeApproppriated != null && interestTobeApproppriated.isGreaterThanZero();
    }

    public boolean isInterestToBeRecoveredFirstWhenGreaterThanEMIEnabled() {
        return isInterestToBeRecoveredFirstWhenGreaterThanEMI;
    }

    public boolean isPrincipalCompoundingDisabledForOverdueLoans() {
        return isPrincipalCompoundingDisabledForOverdueLoans;
    }

    public LocalDate getNewScheduledDueDateStart() {
        return newScheduledDueDateStart;
    }

    public void setNewScheduledDueDateStart(LocalDate newScheduledDueDateStart) {
        this.newScheduledDueDateStart = newScheduledDueDateStart;
    }

    public boolean isDownPaymentEnabled() {
        return isDownPaymentEnabled;
    }

    public BigDecimal getDisbursedAmountPercentageForDownPayment() {
        return disbursedAmountPercentageForDownPayment;
    }
}
