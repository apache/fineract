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

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.calendar.data.CalendarHistoryDataWrapper;
import org.apache.fineract.portfolio.calendar.domain.Calendar;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstance;
import org.apache.fineract.portfolio.calendar.service.CalendarUtils;
import org.apache.fineract.portfolio.common.domain.DayOfWeekType;
import org.apache.fineract.portfolio.common.domain.DaysInMonthType;
import org.apache.fineract.portfolio.common.domain.DaysInYearCustomStrategyType;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.common.domain.NthDayType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanaccount.data.DisbursementData;
import org.apache.fineract.portfolio.loanaccount.data.HolidayDetailDTO;
import org.apache.fineract.portfolio.loanaccount.data.LoanTermVariationsData;
import org.apache.fineract.portfolio.loanaccount.data.LoanTermVariationsDataWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanBuyDownFeeCalculationType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanBuyDownFeeIncomeType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanBuyDownFeeStrategy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCapitalizedIncomeCalculationType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCapitalizedIncomeStrategy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCapitalizedIncomeType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargeOffBehaviour;
import org.apache.fineract.portfolio.loanproduct.data.LoanConfigurationDetails;
import org.apache.fineract.portfolio.loanproduct.domain.AmortizationMethod;
import org.apache.fineract.portfolio.loanproduct.domain.ILoanConfigurationDetails;
import org.apache.fineract.portfolio.loanproduct.domain.InterestCalculationPeriodMethod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestMethod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestRecalculationCompoundingMethod;
import org.apache.fineract.portfolio.loanproduct.domain.LoanPreCloseInterestCalculationStrategy;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.apache.fineract.portfolio.loanproduct.domain.LoanRescheduleStrategyMethod;
import org.apache.fineract.portfolio.loanproduct.domain.LoanSupportedInterestRefundTypes;
import org.apache.fineract.portfolio.loanproduct.domain.RecalculationFrequencyType;
import org.apache.fineract.portfolio.loanproduct.domain.RepaymentStartDateType;

public final class LoanApplicationTerms {

    @Getter
    private CurrencyData currency;

    @Getter
    private Calendar loanCalendar;
    @Getter
    private Integer loanTermFrequency;
    @Getter
    private PeriodFrequencyType loanTermPeriodFrequencyType;
    @Getter
    private Integer numberOfRepayments;
    private Integer actualNumberOfRepayments;
    @Getter
    private Integer repaymentEvery;
    @Getter
    private PeriodFrequencyType repaymentPeriodFrequencyType;

    private long variationDays = 0L;
    @Getter
    private Integer fixedLength;
    @Getter
    private Integer nthDay;

    @Getter
    private DayOfWeekType weekDayType;
    @Getter
    private AmortizationMethod amortizationMethod;

    @Getter
    private InterestMethod interestMethod;
    private BigDecimal interestRatePerPeriod;
    private PeriodFrequencyType interestRatePeriodFrequencyType;
    @Getter
    private BigDecimal annualNominalInterestRate;
    @Getter
    private InterestCalculationPeriodMethod interestCalculationPeriodMethod;
    private boolean allowPartialPeriodInterestCalculation;

    @Setter
    @Getter
    private Money principal;
    @Getter
    private LocalDate expectedDisbursementDate;
    private LocalDate repaymentsStartingFromDate;
    private LocalDate calculatedRepaymentsStartingFromDate;
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
    private Integer interestChargingGrace;

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
    @Getter
    private LocalDate interestChargedFromDate;
    private Money inArrearsTolerance;

    private Integer graceOnArrearsAgeing;

    // added
    private LocalDate loanEndDate;

    @Getter
    private List<DisbursementData> disbursementDatas;

    private boolean multiDisburseLoan;

    @Setter
    private BigDecimal fixedEmiAmount;

    @Setter
    private BigDecimal fixedPrincipalAmount;

    @Getter
    @Setter
    private BigDecimal currentPeriodFixedEmiAmount;

    @Getter
    @Setter
    private BigDecimal currentPeriodFixedPrincipalAmount;

    @Getter
    private BigDecimal actualFixedEmiAmount;

    @Getter
    private BigDecimal maxOutstandingBalance;

    private Money totalInterestDue;

    private DaysInMonthType daysInMonthType;

    private DaysInYearType daysInYearType;

    @Getter
    private boolean interestRecalculationEnabled;

    @Getter
    private LoanRescheduleStrategyMethod rescheduleStrategyMethod;

    @Getter
    private InterestRecalculationCompoundingMethod interestRecalculationCompoundingMethod;

    @Getter
    private CalendarInstance restCalendarInstance;

    @Getter
    private RecalculationFrequencyType recalculationFrequencyType;

    @Getter
    private CalendarInstance compoundingCalendarInstance;

    @Getter
    private RecalculationFrequencyType compoundingFrequencyType;
    private boolean allowCompoundingOnEod;

    private BigDecimal principalThresholdForLastInstalment;
    @Getter
    private Integer installmentAmountInMultiplesOf;

    @Getter
    private LoanPreCloseInterestCalculationStrategy preClosureInterestCalculationStrategy;

    @Getter
    private Money approvedPrincipal;

    private LoanTermVariationsDataWrapper variationsDataWrapper;

    private Money adjustPrincipalForFlatLoans;

    @Getter
    @Setter
    private LocalDate seedDate;

    @Getter
    private CalendarHistoryDataWrapper calendarHistoryDataWrapper;

    private Boolean isInterestChargedFromDateSameAsDisbursalDateEnabled;

    private Integer numberOfDays;

    private boolean isSkipRepaymentOnFirstDayOfMonth;

    private boolean isFirstRepaymentDateAllowedOnHoliday;

    private boolean isInterestToBeRecoveredFirstWhenGreaterThanEMI;

    private boolean isPrincipalCompoundingDisabledForOverdueLoans;

    private HolidayDetailDTO holidayDetailDTO;

    private Set<Integer> periodNumbersApplicableForPrincipalGrace = new HashSet<>();

    private Set<Integer> periodNumbersApplicableForInterestGrace = new HashSet<>();

    // used for FLAT loans when interest rate changed
    private Integer excludePeriodsForCalculation = 0;
    private Money totalPrincipalAccountedForInterestCalcualtion;

    // used for FLAT loans generation on modifying terms
    private Money totalPrincipalAccounted;
    private Money totalInterestAccounted;
    private int periodsCompleted = 0;
    private int extraPeriods = 0;
    private boolean isEqualAmortization;
    @Setter
    private Money interestTobeApproppriated;
    private BigDecimal fixedPrincipalPercentagePerInstallment;

    @Getter
    @Setter
    private LocalDate newScheduledDueDateStart;
    private boolean isDownPaymentEnabled;
    @Getter
    private BigDecimal disbursedAmountPercentageForDownPayment;
    @Getter
    private Money downPaymentAmount;
    private boolean isAutoRepaymentForDownPaymentEnabled;

    @Getter
    private RepaymentStartDateType repaymentStartDateType;
    @Getter
    private LocalDate submittedOnDate;
    @Setter
    private Money disbursedPrincipal;
    @Getter
    private LoanScheduleType loanScheduleType;
    private LoanScheduleProcessingType loanScheduleProcessingType;
    private boolean enableAccrualActivityPosting;
    private List<LoanSupportedInterestRefundTypes> supportedInterestRefundTypes;
    private LoanChargeOffBehaviour chargeOffBehaviour;
    private boolean interestRecognitionOnDisbursementDate;
    private DaysInYearCustomStrategyType daysInYearCustomStrategy;
    private boolean enableIncomeCapitalization;
    private LoanCapitalizedIncomeCalculationType capitalizedIncomeCalculationType;
    private LoanCapitalizedIncomeStrategy capitalizedIncomeStrategy;
    private LoanCapitalizedIncomeType capitalizedIncomeType;
    private boolean enableBuyDownFee;
    private LoanBuyDownFeeCalculationType buyDownFeeCalculationType;
    private LoanBuyDownFeeStrategy buyDownFeeStrategy;
    private LoanBuyDownFeeIncomeType buyDownFeeIncomeType;
    private boolean merchantBuyDownFee;
    @Getter
    private boolean allowFullTermForTranche = false;

    private LoanApplicationTerms(Builder builder) {
        this.currency = builder.currency;
        this.loanTermFrequency = builder.loanTermFrequency;
        this.loanTermPeriodFrequencyType = builder.loanTermPeriodFrequencyType;
        this.numberOfRepayments = builder.numberOfRepayments;
        this.repaymentEvery = builder.repaymentEvery;
        this.repaymentPeriodFrequencyType = builder.repaymentPeriodFrequencyType;
        this.interestRatePerPeriod = builder.interestRatePerPeriod;
        this.interestRatePeriodFrequencyType = builder.interestRatePeriodFrequencyType;
        this.annualNominalInterestRate = builder.annualNominalInterestRate;
        this.principal = builder.principal;
        this.expectedDisbursementDate = builder.expectedDisbursementDate;
        this.repaymentsStartingFromDate = builder.repaymentsStartingFromDate;
        this.daysInMonthType = builder.daysInMonthType;
        this.daysInYearType = builder.daysInYearType;
        this.variationsDataWrapper = builder.variationsDataWrapper;
        this.fixedLength = builder.fixedLength;
        this.inArrearsTolerance = builder.inArrearsTolerance;
        this.disbursementDatas = builder.disbursementDatas;
        this.submittedOnDate = builder.submittedOnDate;
        this.seedDate = builder.seedDate;
        this.isDownPaymentEnabled = builder.isDownPaymentEnabled;
        this.disbursedAmountPercentageForDownPayment = builder.downPaymentPercentage;
        this.interestRecognitionOnDisbursementDate = builder.interestRecognitionOnDisbursementDate != null
                && builder.interestRecognitionOnDisbursementDate;
        if (isDownPaymentEnabled) {
            this.downPaymentAmount = Money.of(getCurrency(),
                    MathUtil.percentageOf(getPrincipal().getAmount(), getDisbursedAmountPercentageForDownPayment(), builder.mc),
                    builder.mc);
            if (getInstallmentAmountInMultiplesOf() != null) {
                this.downPaymentAmount = Money.roundToMultiplesOf(this.downPaymentAmount, getInstallmentAmountInMultiplesOf(), builder.mc);
            }
        } else {
            this.downPaymentAmount = Money.zero(getCurrency(), builder.mc);
        }
        this.enableIncomeCapitalization = builder.enableIncomeCapitalization;
        this.capitalizedIncomeCalculationType = builder.capitalizedIncomeCalculationType;
        this.capitalizedIncomeStrategy = builder.capitalizedIncomeStrategy;
        this.capitalizedIncomeType = builder.capitalizedIncomeType;
        this.enableBuyDownFee = builder.enableBuyDownFee;
        this.buyDownFeeCalculationType = builder.buyDownFeeCalculationType;
        this.buyDownFeeStrategy = builder.buyDownFeeStrategy;
        this.buyDownFeeIncomeType = builder.buyDownFeeIncomeType;
        this.merchantBuyDownFee = builder.merchantBuyDownFee;
        this.allowFullTermForTranche = builder.allowFullTermForTranche;
        this.interestMethod = builder.interestMethod;
        this.allowPartialPeriodInterestCalculation = builder.allowPartialPeriodInterestCalculation;
    }

    public static class Builder {

        private InterestMethod interestMethod;
        private CurrencyData currency;
        private Integer loanTermFrequency;
        private PeriodFrequencyType loanTermPeriodFrequencyType;
        private Integer numberOfRepayments;
        private Integer repaymentEvery;
        private PeriodFrequencyType repaymentPeriodFrequencyType;
        private BigDecimal interestRatePerPeriod;
        private PeriodFrequencyType interestRatePeriodFrequencyType;
        private BigDecimal annualNominalInterestRate;
        private Money principal;
        private LocalDate expectedDisbursementDate;
        private LocalDate repaymentsStartingFromDate;
        private DaysInMonthType daysInMonthType;
        private DaysInYearType daysInYearType;
        private LoanTermVariationsDataWrapper variationsDataWrapper;
        private Integer fixedLength;
        private Money inArrearsTolerance;
        private List<DisbursementData> disbursementDatas;
        private BigDecimal downPaymentPercentage;
        private boolean isDownPaymentEnabled;
        private LocalDate submittedOnDate;
        private LocalDate seedDate;
        private MathContext mc;
        private Boolean interestRecognitionOnDisbursementDate;
        private DaysInYearCustomStrategyType daysInYearCustomStrategy;
        private boolean enableIncomeCapitalization;
        private LoanCapitalizedIncomeCalculationType capitalizedIncomeCalculationType;
        private LoanCapitalizedIncomeStrategy capitalizedIncomeStrategy;
        private LoanCapitalizedIncomeType capitalizedIncomeType;
        private boolean enableBuyDownFee;
        private LoanBuyDownFeeCalculationType buyDownFeeCalculationType;
        private LoanBuyDownFeeStrategy buyDownFeeStrategy;
        private LoanBuyDownFeeIncomeType buyDownFeeIncomeType;
        private boolean merchantBuyDownFee;
        private boolean allowFullTermForTranche;
        private boolean allowPartialPeriodInterestCalculation;

        public Builder interestMethod(InterestMethod interestMethod) {
            this.interestMethod = interestMethod;
            return this;
        }

        public Builder currency(CurrencyData currency) {
            this.currency = currency;
            return this;
        }

        public Builder loanTermFrequency(Integer loanTermFrequency) {
            this.loanTermFrequency = loanTermFrequency;
            return this;
        }

        public Builder loanTermPeriodFrequencyType(PeriodFrequencyType loanTermPeriodFrequencyType) {
            this.loanTermPeriodFrequencyType = loanTermPeriodFrequencyType;
            return this;
        }

        public Builder numberOfRepayments(Integer numberOfRepayments) {
            this.numberOfRepayments = numberOfRepayments;
            return this;
        }

        public Builder repaymentEvery(Integer repaymentEvery) {
            this.repaymentEvery = repaymentEvery;
            return this;
        }

        public Builder repaymentPeriodFrequencyType(PeriodFrequencyType repaymentPeriodFrequencyType) {
            this.repaymentPeriodFrequencyType = repaymentPeriodFrequencyType;
            return this;
        }

        public Builder interestRatePerPeriod(BigDecimal interestRatePerPeriod) {
            this.interestRatePerPeriod = interestRatePerPeriod;
            return this;
        }

        public Builder interestRatePeriodFrequencyType(PeriodFrequencyType interestRatePeriodFrequencyType) {
            this.interestRatePeriodFrequencyType = interestRatePeriodFrequencyType;
            return this;
        }

        public Builder annualNominalInterestRate(BigDecimal annualNominalInterestRate) {
            this.annualNominalInterestRate = annualNominalInterestRate;
            return this;
        }

        public Builder principal(Money principal) {
            this.principal = principal;
            return this;
        }

        public Builder expectedDisbursementDate(LocalDate expectedDisbursementDate) {
            this.expectedDisbursementDate = expectedDisbursementDate;
            return this;
        }

        public Builder repaymentsStartingFromDate(LocalDate repaymentsStartingFromDate) {
            this.repaymentsStartingFromDate = repaymentsStartingFromDate;
            return this;
        }

        public Builder daysInMonthType(DaysInMonthType daysInMonthType) {
            this.daysInMonthType = daysInMonthType;
            return this;
        }

        public Builder daysInYearType(DaysInYearType daysInYearType) {
            this.daysInYearType = daysInYearType;
            return this;
        }

        public Builder fixedLength(Integer fixedLength) {
            this.fixedLength = fixedLength;
            return this;
        }

        public Builder inArrearsTolerance(Money inArrearsTolerance) {
            this.inArrearsTolerance = inArrearsTolerance;
            return this;
        }

        public Builder disbursementDatas(List<DisbursementData> disbursementDatas) {
            this.disbursementDatas = disbursementDatas;
            return this;
        }

        public Builder downPaymentPercentage(BigDecimal downPaymentPercentage) {
            this.downPaymentPercentage = downPaymentPercentage;
            return this;
        }

        public Builder submittedOnDate(LocalDate submittedOnDate) {
            this.submittedOnDate = submittedOnDate;
            return this;
        }

        public Builder seedDate(LocalDate seedDate) {
            this.seedDate = seedDate;
            return this;
        }

        public Builder isDownPaymentEnabled(boolean isDownPaymentEnabled) {
            this.isDownPaymentEnabled = isDownPaymentEnabled;
            return this;
        }

        public Builder mc(MathContext mc) {
            this.mc = mc;
            return this;
        }

        public Builder interestRecognitionOnDisbursementDate(boolean value) {
            this.interestRecognitionOnDisbursementDate = value;
            return this;
        }

        public Builder enableIncomeCapitalization(boolean value) {
            this.enableIncomeCapitalization = value;
            return this;
        }

        public Builder capitalizedIncomeCalculationType(LoanCapitalizedIncomeCalculationType value) {
            this.capitalizedIncomeCalculationType = value;
            return this;
        }

        public Builder capitalizedIncomeStrategy(LoanCapitalizedIncomeStrategy value) {
            this.capitalizedIncomeStrategy = value;
            return this;
        }

        public Builder capitalizedIncomeType(LoanCapitalizedIncomeType value) {
            this.capitalizedIncomeType = value;
            return this;
        }

        public Builder enableBuyDownFee(boolean value) {
            this.enableBuyDownFee = value;
            return this;
        }

        public Builder buyDownFeeCalculationType(LoanBuyDownFeeCalculationType value) {
            this.buyDownFeeCalculationType = value;
            return this;
        }

        public Builder buyDownFeeStrategy(LoanBuyDownFeeStrategy value) {
            this.buyDownFeeStrategy = value;
            return this;
        }

        public Builder buyDownFeeIncomeType(LoanBuyDownFeeIncomeType value) {
            this.buyDownFeeIncomeType = value;
            return this;
        }

        public Builder merchantBuyDownFee(boolean value) {
            this.merchantBuyDownFee = value;
            return this;
        }

        public Builder allowFullTermForTranche(boolean value) {
            this.allowFullTermForTranche = value;
            return this;
        }

        public LoanApplicationTerms build() {
            return new LoanApplicationTerms(this);
        }

        public Builder daysInYearCustomStrategy(DaysInYearCustomStrategyType daysInYearCustomStrategy) {
            this.daysInYearCustomStrategy = daysInYearCustomStrategy;
            return this;
        }

        public Builder allowPartialPeriodInterestCalculation(boolean allowPartialPeriodInterestCalculation) {
            this.allowPartialPeriodInterestCalculation = allowPartialPeriodInterestCalculation;
            return this;
        }

    }

    public static LoanApplicationTerms assembleFrom(LoanRepaymentScheduleModelData modelData, MathContext mc) {
        Money principal = Money.of(modelData.currency(), modelData.disbursementAmount(), mc);
        BigDecimal downPaymentPercentage = modelData.downPaymentPercentage();

        LocalDate seedDate;

        if (modelData.disbursementDate() != null) {
            seedDate = modelData.disbursementDate();
        } else {
            seedDate = modelData.scheduleGenerationStartDate();
        }

        return new Builder().currency(modelData.currency()).loanTermFrequency(modelData.numberOfRepayments())
                .loanTermPeriodFrequencyType(PeriodFrequencyType.valueOf(modelData.repaymentFrequencyType()))
                .numberOfRepayments(modelData.numberOfRepayments()).repaymentEvery(modelData.repaymentFrequency())
                .repaymentPeriodFrequencyType(PeriodFrequencyType.valueOf(modelData.repaymentFrequencyType()))
                .interestRatePerPeriod(modelData.annualNominalInterestRate())
                .interestRatePeriodFrequencyType(PeriodFrequencyType.valueOf(modelData.repaymentFrequencyType()))
                .annualNominalInterestRate(modelData.annualNominalInterestRate()).principal(principal)
                .expectedDisbursementDate(modelData.disbursementDate()).repaymentsStartingFromDate(modelData.scheduleGenerationStartDate())
                .daysInMonthType(modelData.daysInMonth()).daysInYearType(modelData.daysInYear()).fixedLength(modelData.fixedLength())
                .inArrearsTolerance(Money.zero(modelData.currency(), mc)).disbursementDatas(new ArrayList<>())
                .isDownPaymentEnabled(modelData.downPaymentEnabled()).downPaymentPercentage(downPaymentPercentage)
                .submittedOnDate(modelData.scheduleGenerationStartDate()).seedDate(seedDate)
                .interestRecognitionOnDisbursementDate(modelData.interestRecognitionOnDisbursementDate())
                .daysInYearCustomStrategy(modelData.daysInYearCustomStrategy()).interestMethod(modelData.interestMethod())
                .allowPartialPeriodInterestCalculation(modelData.allowPartialPeriodInterestCalculation())
                .allowFullTermForTranche(modelData.allowFullTermForTranche()).mc(mc).build();
    }

    public static LoanApplicationTerms assembleFrom(final CurrencyData currency, final Integer loanTermFrequency,
            final PeriodFrequencyType loanTermPeriodFrequencyType, final Integer numberOfRepayments, final Integer repaymentEvery,
            final PeriodFrequencyType repaymentPeriodFrequencyType, Integer nthDay, DayOfWeekType weekDayType,
            final AmortizationMethod amortizationMethod, final InterestMethod interestMethod, final BigDecimal interestRatePerPeriod,
            final PeriodFrequencyType interestRatePeriodFrequencyType, final BigDecimal annualNominalInterestRate,
            final InterestCalculationPeriodMethod interestCalculationPeriodMethod, final boolean allowPartialPeriodInterestCalculation,
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
            final LoanPreCloseInterestCalculationStrategy preClosureInterestCalculationStrategy, final Calendar loanCalendar,
            BigDecimal approvedAmount, List<LoanTermVariationsData> loanTermVariations,
            Boolean isInterestChargedFromDateSameAsDisbursalDateEnabled, final Integer numberOfDays,
            boolean isSkipRepaymentOnFirstDayOfMonth, final HolidayDetailDTO holidayDetailDTO, final boolean allowCompoundingOnEod,
            final boolean isEqualAmortization, final boolean isInterestToBeRecoveredFirstWhenGreaterThanEMI,
            final BigDecimal fixedPrincipalPercentagePerInstallment, final boolean isPrincipalCompoundingDisabledForOverdueLoans,
            final Boolean enableDownPayment, final BigDecimal disbursedAmountPercentageForDownPayment,
            final Boolean isAutoRepaymentForDownPaymentEnabled, final RepaymentStartDateType repaymentStartDateType,
            final LocalDate submittedOnDate, final LoanScheduleType loanScheduleType,
            final LoanScheduleProcessingType loanScheduleProcessingType, final Integer fixedLength,
            final boolean enableAccrualActivityPosting, final List<LoanSupportedInterestRefundTypes> supportedInterestRefundTypes,
            final LoanChargeOffBehaviour chargeOffBehaviour, final boolean interestRecognitionOnDisbursementDate,
            final DaysInYearCustomStrategyType daysInYearCustomStrategy, final boolean enableIncomeCapitalization,
            final LoanCapitalizedIncomeCalculationType capitalizedIncomeCalculationType,
            final LoanCapitalizedIncomeStrategy capitalizedIncomeStrategy, final LoanCapitalizedIncomeType capitalizedIncomeType,
            final boolean enableBuyDownFee, final LoanBuyDownFeeCalculationType buyDownFeeCalculationType,
            final LoanBuyDownFeeStrategy buyDownFeeStrategy, final LoanBuyDownFeeIncomeType buyDownFeeIncomeType,
            final boolean merchantBuyDownFee, final boolean allowFullTermForTranche) {

        final LoanRescheduleStrategyMethod rescheduleStrategyMethod = null;
        final CalendarHistoryDataWrapper calendarHistoryDataWrapper = null;
        return new LoanApplicationTerms(currency, loanTermFrequency, loanTermPeriodFrequencyType, numberOfRepayments, repaymentEvery,
                repaymentPeriodFrequencyType, nthDay, weekDayType, amortizationMethod, interestMethod, interestRatePerPeriod,
                interestRatePeriodFrequencyType, annualNominalInterestRate, interestCalculationPeriodMethod,
                allowPartialPeriodInterestCalculation, principalMoney, expectedDisbursementDate, repaymentsStartingFromDate,
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
                isAutoRepaymentForDownPaymentEnabled, repaymentStartDateType, submittedOnDate, loanScheduleType, loanScheduleProcessingType,
                fixedLength, enableAccrualActivityPosting, supportedInterestRefundTypes, chargeOffBehaviour,
                interestRecognitionOnDisbursementDate, daysInYearCustomStrategy, enableIncomeCapitalization,
                capitalizedIncomeCalculationType, capitalizedIncomeStrategy, capitalizedIncomeType, enableBuyDownFee,
                buyDownFeeCalculationType, buyDownFeeStrategy, buyDownFeeIncomeType, merchantBuyDownFee, allowFullTermForTranche);

    }

    public static LoanApplicationTerms assembleFrom(final CurrencyData currency, final Integer loanTermFrequency,
            final PeriodFrequencyType loanTermPeriodFrequencyType, NthDayType nthDay, DayOfWeekType dayOfWeek,
            final LocalDate expectedDisbursementDate, final LocalDate repaymentsStartingFromDate,
            final LocalDate calculatedRepaymentsStartingFromDate, final Money inArrearsTolerance,
            final LoanProductRelatedDetail loanProductRelatedDetail, final boolean multiDisburseLoan, final BigDecimal emiAmount,
            final List<DisbursementData> disbursementDatas, final BigDecimal maxOutstandingBalance, final LocalDate interestChargedFromDate,
            final BigDecimal principalThresholdForLastInstalment, final Integer installmentAmountInMultiplesOf,
            final RecalculationFrequencyType recalculationFrequencyType, final CalendarInstance restCalendarInstance,
            final InterestRecalculationCompoundingMethod compoundingMethod, final CalendarInstance compoundingCalendarInstance,
            final RecalculationFrequencyType compoundingFrequencyType,
            final LoanPreCloseInterestCalculationStrategy loanPreClosureInterestCalculationStrategy,
            final LoanRescheduleStrategyMethod rescheduleStrategyMethod, final Calendar loanCalendar, BigDecimal approvedAmount,
            BigDecimal annualNominalInterestRate, final List<LoanTermVariationsData> loanTermVariations,
            final CalendarHistoryDataWrapper calendarHistoryDataWrapper, final Integer numberOfDays,
            final boolean isSkipRepaymentOnFirstDayOfMonth, final HolidayDetailDTO holidayDetailDTO, final boolean allowCompoundingOnEod,
            final boolean isFirstRepaymentDateAllowedOnHoliday, final boolean isInterestToBeRecoveredFirstWhenGreaterThanEMI,
            final BigDecimal fixedPrincipalPercentagePerInstallment, final boolean isPrincipalCompoundingDisabledForOverdueLoans,
            final RepaymentStartDateType repaymentStartDateType, final LocalDate submittedOnDate, final boolean allowFullTermForTranche) {

        final Integer numberOfRepayments = loanProductRelatedDetail.getNumberOfRepayments();
        final Integer repaymentEvery = loanProductRelatedDetail.getRepayEvery();
        final PeriodFrequencyType repaymentPeriodFrequencyType = loanProductRelatedDetail.getRepaymentPeriodFrequencyType();
        final AmortizationMethod amortizationMethod = loanProductRelatedDetail.getAmortizationMethod();
        final InterestMethod interestMethod = loanProductRelatedDetail.getInterestMethod();
        final BigDecimal interestRatePerPeriod = loanProductRelatedDetail.getNominalInterestRatePerPeriod();
        final PeriodFrequencyType interestRatePeriodFrequencyType = loanProductRelatedDetail.getInterestPeriodFrequencyType();
        final InterestCalculationPeriodMethod interestCalculationPeriodMethod = loanProductRelatedDetail
                .getInterestCalculationPeriodMethod();
        final boolean allowPartialPeriodInterestCalculation = loanProductRelatedDetail.isAllowPartialPeriodInterestCalculation();
        final Money principalMoney = loanProductRelatedDetail.getPrincipal();

        //
        final Integer graceOnPrincipalPayment = loanProductRelatedDetail.getGraceOnPrincipalPayment();
        final Integer recurringMoratoriumOnPrincipalPeriods = loanProductRelatedDetail.getRecurringMoratoriumOnPrincipalPeriods();
        final Integer graceOnInterestPayment = loanProductRelatedDetail.getGraceOnInterestPayment();
        final Integer graceOnInterestCharged = loanProductRelatedDetail.getGraceOnInterestCharged();

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
        LoanScheduleType loanScheduleType = loanProductRelatedDetail.getLoanScheduleType();
        LoanScheduleProcessingType loanScheduleProcessingType = loanProductRelatedDetail.getLoanScheduleProcessingType();
        final Integer fixedLength = loanProductRelatedDetail.getFixedLength();
        return new LoanApplicationTerms(currency, loanTermFrequency, loanTermPeriodFrequencyType, numberOfRepayments, repaymentEvery,
                repaymentPeriodFrequencyType, ((nthDay != null) ? nthDay.getValue() : null), dayOfWeek, amortizationMethod, interestMethod,
                interestRatePerPeriod, interestRatePeriodFrequencyType, annualNominalInterestRate, interestCalculationPeriodMethod,
                allowPartialPeriodInterestCalculation, principalMoney, expectedDisbursementDate, repaymentsStartingFromDate,
                calculatedRepaymentsStartingFromDate, graceOnPrincipalPayment, recurringMoratoriumOnPrincipalPeriods,
                graceOnInterestPayment, graceOnInterestCharged, interestChargedFromDate, inArrearsTolerance, multiDisburseLoan, emiAmount,
                disbursementDatas, maxOutstandingBalance, loanProductRelatedDetail.getGraceOnArrearsAgeing(), daysInMonthType,
                daysInYearType, isInterestRecalculationEnabled, rescheduleStrategyMethod, compoundingMethod, restCalendarInstance,
                recalculationFrequencyType, compoundingCalendarInstance, compoundingFrequencyType, principalThresholdForLastInstalment,
                installmentAmountInMultiplesOf, loanPreClosureInterestCalculationStrategy, loanCalendar, approvedAmount, loanTermVariations,
                calendarHistoryDataWrapper, isInterestChargedFromDateSameAsDisbursalDateEnabled, numberOfDays,
                isSkipRepaymentOnFirstDayOfMonth, holidayDetailDTO, allowCompoundingOnEod, isEqualAmortization,
                isFirstRepaymentDateAllowedOnHoliday, isInterestToBeRecoveredFirstWhenGreaterThanEMI,
                fixedPrincipalPercentagePerInstallment, isPrincipalCompoundingDisabledForOverdueLoans, isDownPaymentEnabled,
                disbursedAmountPercentageForDownPayment, isAutoRepaymentForDownPaymentEnabled, repaymentStartDateType, submittedOnDate,
                loanScheduleType, loanScheduleProcessingType, fixedLength, loanProductRelatedDetail.isEnableAccrualActivityPosting(),
                loanProductRelatedDetail.getSupportedInterestRefundTypes(), loanProductRelatedDetail.getChargeOffBehaviour(),
                loanProductRelatedDetail.isInterestRecognitionOnDisbursementDate(), loanProductRelatedDetail.getDaysInYearCustomStrategy(),
                loanProductRelatedDetail.isEnableIncomeCapitalization(), loanProductRelatedDetail.getCapitalizedIncomeCalculationType(),
                loanProductRelatedDetail.getCapitalizedIncomeStrategy(), loanProductRelatedDetail.getCapitalizedIncomeType(),
                loanProductRelatedDetail.isEnableBuyDownFee(), loanProductRelatedDetail.getBuyDownFeeCalculationType(),
                loanProductRelatedDetail.getBuyDownFeeStrategy(), loanProductRelatedDetail.getBuyDownFeeIncomeType(),
                loanProductRelatedDetail.isMerchantBuyDownFee(), allowFullTermForTranche);
    }

    private LoanApplicationTerms(final CurrencyData currency, final Integer loanTermFrequency,
            final PeriodFrequencyType loanTermPeriodFrequencyType, final Integer numberOfRepayments, final Integer repaymentEvery,
            final PeriodFrequencyType repaymentPeriodFrequencyType, final Integer nthDay, final DayOfWeekType weekDayType,
            final AmortizationMethod amortizationMethod, final InterestMethod interestMethod, final BigDecimal interestRatePerPeriod,
            final PeriodFrequencyType interestRatePeriodFrequencyType, final BigDecimal annualNominalInterestRate,
            final InterestCalculationPeriodMethod interestCalculationPeriodMethod, final boolean allowPartialPeriodInterestCalculation,
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
            final LoanPreCloseInterestCalculationStrategy preClosureInterestCalculationStrategy, final Calendar loanCalendar,
            BigDecimal approvedAmount, List<LoanTermVariationsData> loanTermVariations,
            final CalendarHistoryDataWrapper calendarHistoryDataWrapper, Boolean isInterestChargedFromDateSameAsDisbursalDateEnabled,
            final Integer numberOfDays, final boolean isSkipRepaymentOnFirstDayOfMonth, final HolidayDetailDTO holidayDetailDTO,
            final boolean allowCompoundingOnEod, final boolean isEqualAmortization, final boolean isFirstRepaymentDateAllowedOnHoliday,
            final boolean isInterestToBeRecoveredFirstWhenGreaterThanEMI, final BigDecimal fixedPrincipalPercentagePerInstallment,
            final boolean isPrincipalCompoundingDisabledForOverdueLoans, final boolean isDownPaymentEnabled,
            final BigDecimal disbursedAmountPercentageForDownPayment, final boolean isAutoRepaymentForDownPaymentEnabled,
            final RepaymentStartDateType repaymentStartDateType, final LocalDate submittedOnDate, final LoanScheduleType loanScheduleType,
            final LoanScheduleProcessingType loanScheduleProcessingType, final Integer fixedLength, boolean enableAccrualActivityPosting,
            final List<LoanSupportedInterestRefundTypes> supportedInterestRefundTypes, final LoanChargeOffBehaviour chargeOffBehaviour,
            final boolean interestRecognitionOnDisbursementDate, final DaysInYearCustomStrategyType daysInYearCustomStrategy,
            final boolean enableIncomeCapitalization, final LoanCapitalizedIncomeCalculationType capitalizedIncomeCalculationType,
            final LoanCapitalizedIncomeStrategy capitalizedIncomeStrategy, final LoanCapitalizedIncomeType capitalizedIncomeType,
            final boolean enableBuyDownFee, final LoanBuyDownFeeCalculationType buyDownFeeCalculationType,
            final LoanBuyDownFeeStrategy buyDownFeeStrategy, final LoanBuyDownFeeIncomeType buyDownFeeIncomeType,
            final boolean merchantBuyDownFee, final boolean allowFullTermForTranche) {

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
        this.allowPartialPeriodInterestCalculation = allowPartialPeriodInterestCalculation;

        this.principal = principal;
        this.disbursedPrincipal = principal;
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
        this.enableAccrualActivityPosting = enableAccrualActivityPosting;
        this.approvedPrincipal = Money.of(principal.getCurrency(), approvedAmount);
        this.variationsDataWrapper = new LoanTermVariationsDataWrapper(loanTermVariations);
        this.actualNumberOfRepayments = numberOfRepayments + getLoanTermVariations().adjustNumberOfRepayments();
        this.adjustPrincipalForFlatLoans = principal.zero();
        // We only change the seed date if `repaymentStartingFromDate was provided`
        if (this.repaymentsStartingFromDate == null) {
            this.seedDate = repaymentStartDateType.isDisbursementDate() ? expectedDisbursementDate : submittedOnDate;
        } else {
            // When we change the seed date we are taking the `repaymentsStartingFromDate`
            this.seedDate = repaymentsStartingFromDate;
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
        this.downPaymentAmount = Money.zero(getCurrency());
        if (isDownPaymentEnabled) {
            this.downPaymentAmount = Money.of(getCurrency(),
                    MathUtil.percentageOf(getPrincipal().getAmount(), getDisbursedAmountPercentageForDownPayment(), 19));
            if (getInstallmentAmountInMultiplesOf() != null) {
                downPaymentAmount = Money.roundToMultiplesOf(downPaymentAmount, getInstallmentAmountInMultiplesOf());
            }
        }

        this.isAutoRepaymentForDownPaymentEnabled = isAutoRepaymentForDownPaymentEnabled;
        this.repaymentStartDateType = repaymentStartDateType;
        this.submittedOnDate = submittedOnDate;
        this.loanScheduleType = loanScheduleType;
        this.loanScheduleProcessingType = loanScheduleProcessingType;
        this.fixedLength = fixedLength;
        this.supportedInterestRefundTypes = supportedInterestRefundTypes;
        this.chargeOffBehaviour = chargeOffBehaviour;
        this.interestRecognitionOnDisbursementDate = interestRecognitionOnDisbursementDate;
        this.daysInYearCustomStrategy = daysInYearCustomStrategy;
        this.enableIncomeCapitalization = enableIncomeCapitalization;
        this.capitalizedIncomeCalculationType = capitalizedIncomeCalculationType;
        this.capitalizedIncomeStrategy = capitalizedIncomeStrategy;
        this.capitalizedIncomeType = capitalizedIncomeType;
        this.enableBuyDownFee = enableBuyDownFee;
        this.buyDownFeeCalculationType = buyDownFeeCalculationType;
        this.buyDownFeeStrategy = buyDownFeeStrategy;
        this.buyDownFeeIncomeType = buyDownFeeIncomeType;
        this.merchantBuyDownFee = merchantBuyDownFee;
        this.allowFullTermForTranche = allowFullTermForTranche;
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
                    MoneyHelper.getMathContext());
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

                final Money totalGraceOnInterestCharged = totalInterestPerInstallment.multiplyRetainScale(getInterestChargingGrace(), mc);

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
            // TODO: Implement getPeriodEndDate for WHOLE_TERM
            break;
        }
        return dueRepaymentPeriodDate;
    }

    public PrincipalInterest calculateTotalInterestForPeriod(final PaymentPeriodsInOneYearCalculator calculator,
            final BigDecimal interestCalculationGraceOnRepaymentPeriodFraction, final int periodNumber, final MathContext mc,
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
                totalInterestDue = this.disbursedPrincipal.minus(totalPrincipalAccountedForInterestCalcualtion)
                        .multiplyRetainScale(interestRateForLoanTerm, mc);

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
                .multiply(loanTermFrequencyBigDecimal, mc);
    }

    private BigDecimal calculatePeriodsInLoanTerm() {

        BigDecimal periodsInLoanTerm = BigDecimal.valueOf(this.loanTermFrequency);
        switch (this.interestCalculationPeriodMethod) {
            case DAILY:
                // number of days from 'ideal disbursement' to final date

                LocalDate loanStartDate = getExpectedDisbursementDate();
                if (DateUtils.isBefore(loanStartDate, getInterestChargedFromLocalDate())) {
                    loanStartDate = getInterestChargedFromLocalDate();
                }

                final int periodsInLoanTermInteger = DateUtils.getExactDifferenceInDays(loanStartDate, this.loanEndDate);
                periodsInLoanTerm = BigDecimal.valueOf(periodsInLoanTermInteger);
            break;
            case INVALID:
            break;
            case SAME_AS_REPAYMENT_PERIOD:
                if (this.allowPartialPeriodInterestCalculation) {
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
                int numOfDays = DateUtils.getExactDifferenceInDays(startDate, endDate);
                numberOfPeriods = BigDecimal.valueOf((double) numOfDays);
            break;
            case WEEKS:
                int numberOfWeeks = DateUtils.getExactDifference(startDate, endDate, ChronoUnit.WEEKS);
                int daysLeftAfterWeeks = DateUtils.getExactDifferenceInDays(startDate.plusWeeks(numberOfWeeks), endDate);
                numberOfPeriods = numberOfPeriods.add(BigDecimal.valueOf(numberOfWeeks))
                        .add(BigDecimal.valueOf((double) daysLeftAfterWeeks / 7));
            break;
            case MONTHS:
                int numberOfMonths = DateUtils.getExactDifference(startDate, endDate, ChronoUnit.MONTHS);
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
                    if (!CalendarUtils.isValidRecurringDate(loanCalendar.getRecurrence(),
                            loanCalendar.getStartDateLocalDate().minusMonths(getRepaymentEvery()), startDate)) {
                        expectedStartDate = CalendarUtils.getNewRepaymentMeetingDate(loanCalendar.getRecurrence(),
                                startDate.minusMonths(getRepaymentEvery()), startDate.minusMonths(getRepaymentEvery()), getRepaymentEvery(),
                                CalendarUtils.getMeetingFrequencyFromPeriodFrequencyType(getLoanTermPeriodFrequencyType()),
                                this.holidayDetailDTO.getWorkingDays(), isSkipRepaymentOnFirstDayOfMonth, numberOfDays);
                    }
                    if (!DateUtils.isEqual(expectedStartDate, startDate)) {
                        diffDays = DateUtils.getExactDifferenceInDays(startDate, expectedStartDate);
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
                int daysLeftAfterMonths = DateUtils.getExactDifferenceInDays(startDateAfterConsideringMonths, endDate) + diffDays;
                int daysInPeriodAfterMonths = DateUtils.getExactDifferenceInDays(startDateAfterConsideringMonths,
                        endDateAfterConsideringMonths);
                numberOfPeriods = numberOfPeriods.add(BigDecimal.valueOf(numberOfMonths))
                        .add(BigDecimal.valueOf((double) daysLeftAfterMonths / daysInPeriodAfterMonths));
            break;
            case YEARS:
                int numberOfYears = DateUtils.getExactDifference(startDate, endDate, ChronoUnit.YEARS);
                LocalDate startDateAfterConsideringYears = startDate.plusYears(numberOfYears);
                LocalDate endDateAfterConsideringYears = startDate.plusYears(numberOfYears + 1);
                int daysLeftAfterYears = DateUtils.getExactDifferenceInDays(startDateAfterConsideringYears, endDate);
                int daysInPeriodAfterYears = DateUtils.getExactDifferenceInDays(startDateAfterConsideringYears,
                        endDateAfterConsideringYears);
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
        Money interestPerInstallment = totalInterestForLoanTerm
                .dividedBy(Long.valueOf(this.actualNumberOfRepayments) - defaultToZeroIfNull(this.excludePeriodsForCalculation), mc);
        if (this.excludePeriodsForCalculation < this.periodsCompleted) {
            Money interestLeft = this.totalInterestDue.minus(this.totalInterestAccounted);
            interestPerInstallment = interestLeft
                    .dividedBy(Long.valueOf(this.actualNumberOfRepayments) - defaultToZeroIfNull(this.periodsCompleted), mc);
        }

        return interestPerInstallment;
    }

    private Money calculateTotalPrincipalPerPeriodWithoutGrace(final MathContext mc, final int periodNumber,
            Money interestForThisInstallment) {
        final int totalRepaymentsWithCapitalPayment = calculateNumberOfRepaymentsWithPrincipalPayment();
        Money principalPerPeriod;
        if (getFixedEmiAmount() == null) {
            if (this.fixedPrincipalPercentagePerInstallment != null) {
                principalPerPeriod = this.principal.minus(totalPrincipalAccounted)
                        .percentageOf(this.fixedPrincipalPercentagePerInstallment, mc).plus(this.adjustPrincipalForFlatLoans);
            } else {
                principalPerPeriod = this.principal.minus(totalPrincipalAccounted).dividedBy(totalRepaymentsWithCapitalPayment, mc)
                        .plus(this.adjustPrincipalForFlatLoans);
            }
            if (isPrincipalGraceApplicableForThisPeriod(periodNumber)) {
                principalPerPeriod = principalPerPeriod.zero();
            }
            if (!isPrincipalGraceApplicableForThisPeriod(periodNumber) && currentPeriodFixedPrincipalAmount != null) {
                this.adjustPrincipalForFlatLoans = this.adjustPrincipalForFlatLoans.plus(principalPerPeriod
                        .minus(currentPeriodFixedPrincipalAmount).dividedBy(this.actualNumberOfRepayments - periodNumber, mc));
                principalPerPeriod = this.principal.zero().plus(currentPeriodFixedPrincipalAmount);

            }

            if (this.installmentAmountInMultiplesOf != null) {
                Money roundedPrincipalPerPeriod = Money.roundToMultiplesOf(principalPerPeriod, this.installmentAmountInMultiplesOf, mc);
                if (interestForThisInstallment != null) {
                    Money roundedInterestForThisInstallment = Money.roundToMultiplesOf(interestForThisInstallment,
                            this.installmentAmountInMultiplesOf);

                    /*
                     * Thinking is
                     *
                     * principalPerPeriod 416.67 -> 417 interestForThisInstallment 12.50 -> 13
                     *
                     * Sum: 417 + 13 - 12.5 = 417.5 as principal so the total outstanding amount is in line with the
                     * installmentAmountInMultiplesOf setting
                     */
                    principalPerPeriod = roundedPrincipalPerPeriod.add(roundedInterestForThisInstallment).minus(interestForThisInstallment);
                } else {
                    principalPerPeriod = roundedPrincipalPerPeriod;
                }
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
            interestForInstallment = realTotalInterestForLoan.dividedBy(BigDecimal.valueOf(interestPaymentDuePeriods), mc);
            if (this.excludePeriodsForCalculation < this.periodsCompleted) {
                Money interestLeft = this.totalInterestDue.minus(this.totalInterestAccounted);
                Integer interestDuePeriods = calculateNumberOfRemainingInterestPaymentPeriods(this.actualNumberOfRepayments,
                        this.periodsCompleted);
                interestForInstallment = interestLeft.dividedBy(Long.valueOf(interestDuePeriods), mc);
            }
            if (!this.periodNumbersApplicableForInterestGrace.isEmpty()) {
                int periodsElapsed = calculateLastInterestGracePeriod(periodNumber);
                if (periodsElapsed > this.excludePeriodsForCalculation && periodsElapsed > this.periodsCompleted) {
                    Money interestLeft = this.totalInterestDue.minus(this.totalInterestAccounted);
                    Integer interestDuePeriods = calculateNumberOfRemainingInterestPaymentPeriods(this.actualNumberOfRepayments,
                            periodsElapsed);
                    interestForInstallment = interestLeft.dividedBy(Long.valueOf(interestDuePeriods), mc);
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
                BigDecimal numberOfDaysInPeriod = BigDecimal.valueOf(DateUtils.getDifferenceInDays(periodStartDate, periodEndDate));

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
                    // TODO: Implement getPeriodEndDate for WHOLE_TERM
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
        if (this.interestCalculationPeriodMethod.isDaily() || this.allowPartialPeriodInterestCalculation) {
            loanTermFrequencyBigDecimal = calculatePeriodsBetweenDates(periodStartDate, periodEndDate);
        }
        return loanTermFrequencyBigDecimal;
    }

    public BigDecimal interestRateFor(final PaymentPeriodsInOneYearCalculator calculator, final MathContext mc,
            final Money outstandingBalance, final LocalDate fromDate, final LocalDate toDate) {

        long loanTermPeriodsInOneYear = calculator.calculate(PeriodFrequencyType.DAYS).longValue();
        int repaymentEvery = DateUtils.getExactDifferenceInDays(fromDate, toDate);
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
        return this.periodNumbersApplicableForPrincipalGrace.contains(periodNumber);
    }

    public boolean isInterestPaymentGraceApplicableForThisPeriod(final int periodNumber) {
        return this.periodNumbersApplicableForInterestGrace.contains(periodNumber);
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

            BigDecimal fixedEmiAmount = BigDecimal.valueOf(installmentAmount);
            if (this.installmentAmountInMultiplesOf != null) {
                fixedEmiAmount = Money.roundToMultiplesOf(fixedEmiAmount, this.installmentAmountInMultiplesOf);
            }
            setFixedEmiAmount(fixedEmiAmount);
        }
        return getFixedEmiAmount().doubleValue();
    }

    private Money calculateDecliningInterestDueForInstallmentBeforeApplyingGrace(final PaymentPeriodsInOneYearCalculator calculator,
            final MathContext mc, final Money outstandingBalance, LocalDate periodStartDate, LocalDate periodEndDate) {

        Money interestDue = Money.zero(outstandingBalance.getCurrency());

        final BigDecimal periodicInterestRate = periodicInterestRate(calculator, mc, this.daysInMonthType, this.daysInYearType,
                periodStartDate, periodEndDate);// 0.021232877 ob:14911.64
        interestDue = outstandingBalance.multiplyRetainScale(periodicInterestRate, mc);

        return interestDue;
    }

    private Money calculateDecliningInterestDueForInstallmentAfterApplyingGrace(final PaymentPeriodsInOneYearCalculator calculator,
            final BigDecimal interestCalculationGraceOnRepaymentPeriodFraction, final MathContext mc, final Money outstandingBalance,
            final int periodNumber, LocalDate periodStartDate, LocalDate periodEndDate) {

        Money interest = calculateDecliningInterestDueForInstallmentBeforeApplyingGrace(calculator, mc, outstandingBalance, periodStartDate,
                periodEndDate);

        if (isInterestPaymentGraceApplicableForThisPeriod(periodNumber)) {
            interest = interest.zero();
        }

        BigDecimal fraction = interestCalculationGraceOnRepaymentPeriodFraction;

        if (isInterestFreeGracePeriod(periodNumber)) {
            interest = interest.zero();
        } else if (isInterestFreeGracePeriodFromDate(interestCalculationGraceOnRepaymentPeriodFraction)) {

            if (interestCalculationGraceOnRepaymentPeriodFraction.compareTo(BigDecimal.ZERO) > 0) {
                interest = interest.zero();
                fraction = fraction.subtract(BigDecimal.ONE);

            } else if (interestCalculationGraceOnRepaymentPeriodFraction.compareTo(BigDecimal.valueOf(0.25)) > 0
                    && interestCalculationGraceOnRepaymentPeriodFraction.compareTo(BigDecimal.ONE) < 0) {

                final Money graceOnInterestForRepaymentPeriod = interest.multipliedBy(interestCalculationGraceOnRepaymentPeriodFraction);
                interest = interest.minus(graceOnInterestForRepaymentPeriod);
                fraction = BigDecimal.ZERO;
            }
        }

        return interest;
    }

    private boolean isInterestFreeGracePeriodFromDate(BigDecimal interestCalculationGraceOnRepaymentPeriodFraction) {
        return this.interestChargedFromDate != null && interestCalculationGraceOnRepaymentPeriodFraction.compareTo(BigDecimal.ZERO) > 0;
    }

    private Money calculateEqualPrincipalDueForInstallment(final MathContext mc, final int periodNumber) {
        Money principal = this.principal;
        if (this.fixedPrincipalAmount == null) {
            final Integer numberOfPrincipalPaymentPeriods = calculateNumberOfRemainingPrincipalPaymentPeriods(this.actualNumberOfRepayments,
                    periodNumber);
            principal = this.principal.dividedBy(numberOfPrincipalPaymentPeriods, mc);
            this.fixedPrincipalAmount = principal.getAmount();
        }
        if (this.fixedPrincipalPercentagePerInstallment != null) {
            principal = this.principal.percentageOf(this.fixedPrincipalPercentagePerInstallment, mc);
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
        Money principal = outstandingAmount.dividedBy(numberOfPrincipalPaymentPeriods, mc);
        this.fixedPrincipalAmount = principal.getAmount();
    }

    private Integer calculateNumberOfRemainingPrincipalPaymentPeriods(final Integer totalNumberOfRepaymentPeriods, int periodsElapsed) {
        int principalFeePeriods = 0;
        for (Integer intNumber : this.periodNumbersApplicableForPrincipalGrace) {
            if (intNumber > periodsElapsed) {
                principalFeePeriods++;
            }
        }
        return totalNumberOfRepaymentPeriods - periodsElapsed - principalFeePeriods;
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
        final CurrencyData currency = new CurrencyData(this.currency.getCode(), this.currency.getDecimalPlaces(),
                this.currency.getInMultiplesOf());

        return LoanProductRelatedDetail.createFrom(currency, this.principal.getAmount(), this.interestRatePerPeriod,
                this.interestRatePeriodFrequencyType, this.annualNominalInterestRate, this.interestMethod,
                this.interestCalculationPeriodMethod, this.allowPartialPeriodInterestCalculation, this.repaymentEvery,
                this.repaymentPeriodFrequencyType, this.numberOfRepayments, this.principalGrace, this.recurringMoratoriumOnPrincipalPeriods,
                this.interestPaymentGrace, this.interestChargingGrace, this.amortizationMethod, this.inArrearsTolerance.getAmount(),
                this.graceOnArrearsAgeing, this.daysInMonthType.getValue(), this.daysInYearType.getValue(),
                this.interestRecalculationEnabled, this.isEqualAmortization, this.isDownPaymentEnabled,
                this.disbursedAmountPercentageForDownPayment, this.isAutoRepaymentForDownPaymentEnabled, this.loanScheduleType,
                this.loanScheduleProcessingType, this.fixedLength, this.enableAccrualActivityPosting, this.supportedInterestRefundTypes,
                this.chargeOffBehaviour, this.interestRecognitionOnDisbursementDate, this.daysInYearCustomStrategy,
                this.enableIncomeCapitalization, this.capitalizedIncomeCalculationType, this.capitalizedIncomeStrategy,
                this.capitalizedIncomeType, this.installmentAmountInMultiplesOf, this.enableBuyDownFee, this.buyDownFeeCalculationType,
                this.buyDownFeeStrategy, this.buyDownFeeIncomeType, this.merchantBuyDownFee);
    }

    public ILoanConfigurationDetails toLoanConfigurationDetails() {
        final CurrencyData currency = new CurrencyData(this.currency.getCode(), this.currency.getDecimalPlaces(),
                this.currency.getInMultiplesOf());
        return new LoanConfigurationDetails(currency, interestRatePerPeriod, annualNominalInterestRate, interestChargingGrace,
                interestPaymentGrace, principalGrace, recurringMoratoriumOnPrincipalPeriods, interestMethod,
                interestCalculationPeriodMethod, daysInYearType, daysInMonthType, amortizationMethod, repaymentPeriodFrequencyType,
                repaymentEvery, numberOfRepayments,
                isInterestChargedFromDateSameAsDisbursalDateEnabled != null && isInterestChargedFromDateSameAsDisbursalDateEnabled,
                daysInYearCustomStrategy, allowPartialPeriodInterestCalculation, interestRecalculationEnabled, recalculationFrequencyType,
                preClosureInterestCalculationStrategy, allowFullTermForTranche, loanScheduleProcessingType);
    }

    public LocalDate getRepaymentStartFromDate() {
        return this.repaymentsStartingFromDate;
    }

    public LocalDate getInterestChargedFromLocalDate() {
        return this.interestChargedFromDate;
    }

    public LocalDate getRepaymentsStartingFromLocalDate() {
        return this.repaymentsStartingFromDate;
    }

    public LocalDate getCalculatedRepaymentsStartingFromLocalDate() {
        return this.calculatedRepaymentsStartingFromDate;
    }

    public boolean isMultiDisburseLoan() {
        return this.multiDisburseLoan;
    }

    @NotNull
    public Money getMaxOutstandingBalanceMoney() {
        return Money.of(getCurrency(), this.maxOutstandingBalance);
    }

    public BigDecimal getFixedEmiAmount() {
        BigDecimal fixedEmiAmount = this.fixedEmiAmount;
        if (getCurrentPeriodFixedEmiAmount() != null) {
            fixedEmiAmount = getCurrentPeriodFixedEmiAmount();
        }
        return fixedEmiAmount;
    }

    public void resetFixedEmiAmount() {
        this.fixedEmiAmount = this.actualFixedEmiAmount;
    }

    public LoanRescheduleStrategyMethod getLoanRescheduleStrategyMethod() {
        return LoanRescheduleStrategyMethod.REDUCE_EMI_AMOUNT;
    }

    public boolean isInterestBearing() {
        return BigDecimal.ZERO.compareTo(getAnnualNominalInterestRate()) < 0;
    }

    public boolean isInterestBearingAndInterestRecalculationEnabled() {
        return isInterestBearing() && isInterestRecalculationEnabled();
    }

    private boolean isFallingInRepaymentPeriod(LocalDate fromDate, LocalDate toDate) {
        boolean isSameAsRepaymentPeriod = false;
        if (this.interestCalculationPeriodMethod.getValue().equals(InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD.getValue())) {
            switch (this.repaymentPeriodFrequencyType) {
                case WEEKS:
                    int days = DateUtils.getExactDifferenceInDays(fromDate, toDate);
                    isSameAsRepaymentPeriod = (days % 7) == 0;
                break;
                case MONTHS:
                    boolean isFromDateOnEndDate = fromDate.getDayOfMonth() > fromDate.plusDays(1).getDayOfMonth();
                    boolean isToDateOnEndDate = toDate.getDayOfMonth() > toDate.plusDays(1).getDayOfMonth();

                    if (isFromDateOnEndDate && isToDateOnEndDate) {
                        isSameAsRepaymentPeriod = true;
                    } else {

                        int months = getPeriodsBetween(fromDate, toDate);
                        fromDate = fromDate.plusMonths(months);
                        isSameAsRepaymentPeriod = DateUtils.isEqual(fromDate, toDate);
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
                numberOfPeriods = DateUtils.getExactDifferenceInDays(fromDate, toDate);
            break;
            case WEEKS:
                numberOfPeriods = DateUtils.getExactDifference(fromDate, toDate, ChronoUnit.WEEKS);
            break;
            case MONTHS:
                numberOfPeriods = DateUtils.getExactDifference(fromDate, toDate, ChronoUnit.MONTHS);
            break;
            case YEARS:
                numberOfPeriods = DateUtils.getExactDifference(fromDate, toDate, ChronoUnit.YEARS);
            break;
            default:
            break;
        }
        return numberOfPeriods;
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
            if (this.annualNominalInterestRate == null || annualNominalInterestRate.compareTo(this.annualNominalInterestRate) != 0) {
                this.fixedEmiAmount = null;
            }
            this.annualNominalInterestRate = annualNominalInterestRate;
        }
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

    public Integer fetchNumberOfRepaymentsAfterExceptions() {
        return this.actualNumberOfRepayments;
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

    public Boolean isInterestTobeApproppriated() {
        return interestTobeApproppriated != null && interestTobeApproppriated.isGreaterThanZero();
    }

    public boolean isInterestToBeRecoveredFirstWhenGreaterThanEMIEnabled() {
        return isInterestToBeRecoveredFirstWhenGreaterThanEMI;
    }

    public boolean isPrincipalCompoundingDisabledForOverdueLoans() {
        return isPrincipalCompoundingDisabledForOverdueLoans;
    }

    public boolean isDownPaymentEnabled() {
        return isDownPaymentEnabled;
    }

    public LocalDate calculateMaxDateForFixedLength() {
        final LocalDate startDate = getRepaymentStartDate();
        LocalDate maxDateForFixedLength = null;
        if (fixedLength == null) {
            return maxDateForFixedLength;
        }
        switch (repaymentPeriodFrequencyType) {
            case DAYS:
                maxDateForFixedLength = startDate.plusDays(fixedLength + variationDays);
            break;
            case WEEKS:
                maxDateForFixedLength = startDate.plusWeeks(fixedLength + variationDays);
            break;
            case MONTHS:
                maxDateForFixedLength = startDate.plusMonths(fixedLength + variationDays);
            break;
            case YEARS:
                maxDateForFixedLength = startDate.plusYears(fixedLength + variationDays);
            break;
            case INVALID:
            break;
            case WHOLE_TERM:
            // TODO: Implement getPeriodEndDate for WHOLE_TERM
            break;
        }
        return maxDateForFixedLength;
    }

    public LocalDate getRepaymentStartDate() {
        final RepaymentStartDateType repaymentStartDateType = getRepaymentStartDateType();
        return RepaymentStartDateType.DISBURSEMENT_DATE.equals(repaymentStartDateType) ? getExpectedDisbursementDate()
                : getSubmittedOnDate();
    }

    public boolean isLastPeriod(final Integer periodNumber) {
        return getNumberOfRepayments().equals(periodNumber);
    }

    public void updateVariationDays(final long daysToAdd) {
        this.variationDays += daysToAdd;
    }

}
