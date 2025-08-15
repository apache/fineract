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
package org.apache.fineract.portfolio.loanproduct.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.common.domain.DaysInMonthType;
import org.apache.fineract.portfolio.common.domain.DaysInYearCustomStrategyType;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanBuyDownFeeCalculationType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanBuyDownFeeIncomeType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanBuyDownFeeStrategy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCapitalizedIncomeCalculationType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCapitalizedIncomeStrategy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCapitalizedIncomeType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargeOffBehaviour;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;

/**
 * LoanRepaymentScheduleDetail encapsulates all the details of a {@link LoanProduct} that are also used and persisted by
 * a {@link Loan}.
 */
@Embeddable
@Getter
@Setter
public class LoanProductRelatedDetail implements LoanProductMinimumRepaymentScheduleRelatedDetail {

    @Embedded
    private MonetaryCurrency currency;

    @Column(name = "principal_amount", scale = 6, precision = 19)
    private BigDecimal principal;

    @Column(name = "nominal_interest_rate_per_period", scale = 6, precision = 19)
    private BigDecimal nominalInterestRatePerPeriod;

    // FIXME - move away form JPA ordinal use for enums using just integer -
    // requires sql patch for existing users of software.
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "interest_period_frequency_enum")
    private PeriodFrequencyType interestPeriodFrequencyType;

    @Column(name = "annual_nominal_interest_rate", scale = 6, precision = 19)
    private BigDecimal annualNominalInterestRate;

    // FIXME - move away form JPA ordinal use for enums using just integer -
    // requires sql patch for existing users of software.
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "interest_method_enum", nullable = false)
    private InterestMethod interestMethod;

    // FIXME - move away form JPA ordinal use for enums using just integer -
    // requires sql patch for existing users of software.
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "interest_calculated_in_period_enum", nullable = false)
    private InterestCalculationPeriodMethod interestCalculationPeriodMethod;

    @Column(name = "allow_partial_period_interest_calcualtion", nullable = false)
    private boolean allowPartialPeriodInterestCalculation;

    @Column(name = "repay_every", nullable = false)
    private Integer repayEvery;

    // FIXME - move away form JPA ordinal use for enums using just integer -
    // requires sql patch for existing users of software.
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "repayment_period_frequency_enum", nullable = false)
    private PeriodFrequencyType repaymentPeriodFrequencyType;

    @Column(name = "fixed_length", nullable = false)
    private Integer fixedLength;

    @Column(name = "number_of_repayments", nullable = false)
    private Integer numberOfRepayments;

    @Column(name = "grace_on_principal_periods")
    private Integer graceOnPrincipalPayment;

    @Column(name = "recurring_moratorium_principal_periods")
    private Integer recurringMoratoriumOnPrincipalPeriods;

    @Column(name = "grace_on_interest_periods")
    private Integer graceOnInterestPayment;

    @Column(name = "grace_interest_free_periods")
    private Integer graceOnInterestCharged;

    // FIXME - move away form JPA ordinal use for enums using just integer -
    // requires sql patch for existing users of software.
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "amortization_method_enum", nullable = false)
    private AmortizationMethod amortizationMethod;

    @Column(name = "arrearstolerance_amount", scale = 6, precision = 19)
    private BigDecimal inArrearsTolerance;

    @Column(name = "grace_on_arrears_ageing")
    private Integer graceOnArrearsAgeing;

    @Column(name = "days_in_month_enum", nullable = false)
    private Integer daysInMonthType;

    @Column(name = "days_in_year_enum", nullable = false)
    private Integer daysInYearType;

    @Column(name = "interest_recalculation_enabled")
    private boolean isInterestRecalculationEnabled;

    @Column(name = "is_equal_amortization", nullable = false)
    private boolean isEqualAmortization = false;

    @Column(name = "enable_down_payment", nullable = false)
    private boolean enableDownPayment;

    @Column(name = "disbursed_amount_percentage_for_down_payment", scale = 6, precision = 9)
    private BigDecimal disbursedAmountPercentageForDownPayment;

    @Column(name = "enable_auto_repayment_for_down_payment", nullable = false)
    private boolean enableAutoRepaymentForDownPayment;

    @Column(name = "loan_schedule_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private LoanScheduleType loanScheduleType;

    @Column(name = "loan_schedule_processing_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private LoanScheduleProcessingType loanScheduleProcessingType;

    @Column(name = "enable_accrual_activity_posting", nullable = false)
    private boolean enableAccrualActivityPosting = false;

    @Convert(converter = SupportedInterestRefundTypesListConverter.class)
    @Column(name = "supported_interest_refund_types")
    private List<LoanSupportedInterestRefundTypes> supportedInterestRefundTypes = List.of();

    @Column(name = "charge_off_behaviour")
    @Enumerated(EnumType.STRING)
    private LoanChargeOffBehaviour chargeOffBehaviour;

    @Column(name = "interest_recognition_on_disbursement_date", nullable = false)
    private boolean interestRecognitionOnDisbursementDate = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "days_in_year_custom_strategy")
    private DaysInYearCustomStrategyType daysInYearCustomStrategy;

    @Column(name = "enable_income_capitalization")
    private boolean enableIncomeCapitalization = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "capitalized_income_calculation_type")
    private LoanCapitalizedIncomeCalculationType capitalizedIncomeCalculationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "capitalized_income_strategy")
    private LoanCapitalizedIncomeStrategy capitalizedIncomeStrategy;

    @Enumerated(EnumType.STRING)
    @Column(name = "capitalized_income_type")
    private LoanCapitalizedIncomeType capitalizedIncomeType;

    @Column(name = "enable_buy_down_fee")
    private boolean enableBuyDownFee = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "buy_down_fee_calculation_type")
    private LoanBuyDownFeeCalculationType buyDownFeeCalculationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "buy_down_fee_strategy")
    private LoanBuyDownFeeStrategy buyDownFeeStrategy;

    @Enumerated(EnumType.STRING)
    @Column(name = "buy_down_fee_income_type")
    private LoanBuyDownFeeIncomeType buyDownFeeIncomeType;

    @Column(name = "is_merchant_buy_down_fee")
    private boolean merchantBuyDownFee = true;

    @Column(name = "installment_amount_in_multiples_of")
    private Integer installmentAmountInMultiplesOf;

    public static LoanProductRelatedDetail createFrom(final CurrencyData currencyData, final BigDecimal principal,
            final BigDecimal nominalInterestRatePerPeriod, final PeriodFrequencyType interestRatePeriodFrequencyType,
            final BigDecimal nominalAnnualInterestRate, final InterestMethod interestMethod,
            final InterestCalculationPeriodMethod interestCalculationPeriodMethod, final boolean allowPartialPeriodInterestCalcualtion,
            final Integer repaymentEvery, final PeriodFrequencyType repaymentPeriodFrequencyType, final Integer numberOfRepayments,
            final Integer graceOnPrincipalPayment, final Integer recurringMoratoriumOnPrincipalPeriods,
            final Integer graceOnInterestPayment, final Integer graceOnInterestCharged, final AmortizationMethod amortizationMethod,
            final BigDecimal inArrearsTolerance, final Integer graceOnArrearsAgeing, final Integer daysInMonthType,
            final Integer daysInYearType, final boolean isInterestRecalculationEnabled, final boolean isEqualAmortization,
            final boolean enableDownPayment, final BigDecimal disbursedAmountPercentageForDownPayment,
            final boolean enableAutoRepaymentForDownPayment, final LoanScheduleType loanScheduleType,
            final LoanScheduleProcessingType loanScheduleProcessingType, final Integer fixedLength,
            final boolean enableAccrualActivityPosting, final List<LoanSupportedInterestRefundTypes> supportedInterestRefundTypes,
            final LoanChargeOffBehaviour chargeOffBehaviour, final boolean interestRecognitionOnDisbursementDate,
            final DaysInYearCustomStrategyType daysInYearCustomStrategy, final boolean enableIncomeCapitalization,
            final LoanCapitalizedIncomeCalculationType capitalizedIncomeCalculationType,
            final LoanCapitalizedIncomeStrategy capitalizedIncomeStrategy, final LoanCapitalizedIncomeType capitalizedIncomeType,
            final Integer installmentAmountInMultiplesOf, final boolean enableBuyDownFee,
            final LoanBuyDownFeeCalculationType buyDownFeeCalculationType, final LoanBuyDownFeeStrategy buyDownFeeStrategy,
            final LoanBuyDownFeeIncomeType buyDownFeeIncomeType, final boolean merchantBuyDownFee) {

        final MonetaryCurrency currency = MonetaryCurrency.fromCurrencyData(currencyData);
        return new LoanProductRelatedDetail(currency, principal, nominalInterestRatePerPeriod, interestRatePeriodFrequencyType,
                nominalAnnualInterestRate, interestMethod, interestCalculationPeriodMethod, allowPartialPeriodInterestCalcualtion,
                repaymentEvery, repaymentPeriodFrequencyType, numberOfRepayments, graceOnPrincipalPayment,
                recurringMoratoriumOnPrincipalPeriods, graceOnInterestPayment, graceOnInterestCharged, amortizationMethod,
                inArrearsTolerance, graceOnArrearsAgeing, daysInMonthType, daysInYearType, isInterestRecalculationEnabled,
                isEqualAmortization, enableDownPayment, disbursedAmountPercentageForDownPayment, enableAutoRepaymentForDownPayment,
                loanScheduleType, loanScheduleProcessingType, fixedLength, enableAccrualActivityPosting, supportedInterestRefundTypes,
                chargeOffBehaviour, interestRecognitionOnDisbursementDate, daysInYearCustomStrategy, enableIncomeCapitalization,
                capitalizedIncomeCalculationType, capitalizedIncomeStrategy, capitalizedIncomeType, installmentAmountInMultiplesOf,
                enableBuyDownFee, buyDownFeeCalculationType, buyDownFeeStrategy, buyDownFeeIncomeType, merchantBuyDownFee);
    }

    protected LoanProductRelatedDetail() {
        //
    }

    public LoanProductRelatedDetail(final MonetaryCurrency currency, final BigDecimal defaultPrincipal,
            final BigDecimal defaultNominalInterestRatePerPeriod, final PeriodFrequencyType interestPeriodFrequencyType,
            final BigDecimal defaultAnnualNominalInterestRate, final InterestMethod interestMethod,
            final InterestCalculationPeriodMethod interestCalculationPeriodMethod, final boolean allowPartialPeriodInterestCalculation,
            final Integer repayEvery, final PeriodFrequencyType repaymentFrequencyType, final Integer defaultNumberOfRepayments,
            final Integer graceOnPrincipalPayment, final Integer recurringMoratoriumOnPrincipalPeriods,
            final Integer graceOnInterestPayment, final Integer graceOnInterestCharged, final AmortizationMethod amortizationMethod,
            final BigDecimal inArrearsTolerance, final Integer graceOnArrearsAgeing, final Integer daysInMonthType,
            final Integer daysInYearType, final boolean isInterestRecalculationEnabled, final boolean isEqualAmortization,
            final boolean enableDownPayment, final BigDecimal disbursedAmountPercentageForDownPayment,
            final boolean enableAutoRepaymentForDownPayment, final LoanScheduleType loanScheduleType,
            final LoanScheduleProcessingType loanScheduleProcessingType, final Integer fixedLength,
            final boolean enableAccrualActivityPosting, List<LoanSupportedInterestRefundTypes> supportedInterestRefundTypes,
            final LoanChargeOffBehaviour chargeOffBehaviour, final boolean interestRecognitionOnDisbursementDate,
            final DaysInYearCustomStrategyType daysInYearCustomStrategy, final boolean enableIncomeCapitalization,
            final LoanCapitalizedIncomeCalculationType capitalizedIncomeCalculationType,
            final LoanCapitalizedIncomeStrategy capitalizedIncomeStrategy, final LoanCapitalizedIncomeType capitalizedIncomeType,
            final Integer installmentAmountInMultiplesOf, final boolean enableBuyDownFee,
            final LoanBuyDownFeeCalculationType buyDownFeeCalculationType, final LoanBuyDownFeeStrategy buyDownFeeStrategy,
            final LoanBuyDownFeeIncomeType buyDownFeeIncomeType, final boolean merchantBuyDownFee) {
        this.currency = currency;
        this.principal = defaultPrincipal;
        this.nominalInterestRatePerPeriod = defaultNominalInterestRatePerPeriod;
        this.interestPeriodFrequencyType = interestPeriodFrequencyType;
        this.annualNominalInterestRate = defaultAnnualNominalInterestRate;
        this.interestMethod = interestMethod;
        this.interestCalculationPeriodMethod = interestCalculationPeriodMethod;
        this.allowPartialPeriodInterestCalculation = allowPartialPeriodInterestCalculation;
        this.repayEvery = repayEvery;
        this.repaymentPeriodFrequencyType = repaymentFrequencyType;
        this.numberOfRepayments = defaultNumberOfRepayments;
        this.fixedLength = fixedLength;
        this.graceOnPrincipalPayment = defaultToNullIfZero(graceOnPrincipalPayment);
        this.recurringMoratoriumOnPrincipalPeriods = recurringMoratoriumOnPrincipalPeriods;
        this.graceOnInterestPayment = defaultToNullIfZero(graceOnInterestPayment);
        this.graceOnInterestCharged = defaultToNullIfZero(graceOnInterestCharged);
        this.amortizationMethod = amortizationMethod;
        if (inArrearsTolerance != null && BigDecimal.ZERO.compareTo(inArrearsTolerance) == 0) {
            this.inArrearsTolerance = null;
        } else {
            this.inArrearsTolerance = inArrearsTolerance;
        }
        this.graceOnArrearsAgeing = graceOnArrearsAgeing;
        this.daysInMonthType = daysInMonthType;
        this.daysInYearType = daysInYearType;
        this.isInterestRecalculationEnabled = isInterestRecalculationEnabled;
        this.isEqualAmortization = isEqualAmortization;
        this.enableDownPayment = enableDownPayment;
        this.disbursedAmountPercentageForDownPayment = disbursedAmountPercentageForDownPayment;
        this.enableAutoRepaymentForDownPayment = enableAutoRepaymentForDownPayment;
        this.loanScheduleType = loanScheduleType;
        this.loanScheduleProcessingType = loanScheduleProcessingType;
        this.enableAccrualActivityPosting = enableAccrualActivityPosting;
        this.supportedInterestRefundTypes = supportedInterestRefundTypes;
        this.chargeOffBehaviour = chargeOffBehaviour;
        this.interestRecognitionOnDisbursementDate = interestRecognitionOnDisbursementDate;
        this.daysInYearCustomStrategy = daysInYearCustomStrategy;
        this.enableIncomeCapitalization = enableIncomeCapitalization;
        this.capitalizedIncomeCalculationType = capitalizedIncomeCalculationType;
        this.capitalizedIncomeStrategy = capitalizedIncomeStrategy;
        this.capitalizedIncomeType = capitalizedIncomeType;
        this.installmentAmountInMultiplesOf = installmentAmountInMultiplesOf;
        this.enableBuyDownFee = enableBuyDownFee;
        this.buyDownFeeCalculationType = buyDownFeeCalculationType;
        this.buyDownFeeStrategy = buyDownFeeStrategy;
        this.buyDownFeeIncomeType = buyDownFeeIncomeType;
        this.merchantBuyDownFee = merchantBuyDownFee;
    }

    private Integer defaultToNullIfZero(final Integer value) {
        Integer defaultTo = value;
        if (Integer.valueOf(0).equals(value)) {
            defaultTo = null;
        }
        return defaultTo;
    }

    public MonetaryCurrency getCurrency() {
        return this.currency.copy();
    }

    @Override
    public CurrencyData getCurrencyData() {
        return currency.toData();
    }

    public Money getPrincipal() {
        return Money.of(getCurrencyData(), this.principal);
    }

    public Money getInArrearsTolerance() {
        return Money.of(getCurrencyData(), this.inArrearsTolerance);
    }

    // TODO: REVIEW
    @Override
    public BigDecimal getNominalInterestRatePerPeriod() {
        return this.nominalInterestRatePerPeriod == null ? null
                : BigDecimal.valueOf(Double.parseDouble(this.nominalInterestRatePerPeriod.stripTrailingZeros().toString()));
    }

    // TODO: REVIEW
    @Override
    public PeriodFrequencyType getInterestPeriodFrequencyType() {
        return this.interestPeriodFrequencyType == null ? PeriodFrequencyType.INVALID : this.interestPeriodFrequencyType;
    }

    // TODO: REVIEW
    @Override
    public BigDecimal getAnnualNominalInterestRate() {
        return this.annualNominalInterestRate == null ? null
                : BigDecimal.valueOf(Double.parseDouble(this.annualNominalInterestRate.stripTrailingZeros().toString()));
    }

    @Override
    public DaysInYearCustomStrategyType getDaysInYearCustomStrategy() {
        return daysInYearCustomStrategy;
    }

    public boolean hasCurrencyCodeOf(final String currencyCode) {
        return this.currency.getCode().equalsIgnoreCase(currencyCode);
    }

    public DaysInMonthType fetchDaysInMonthType() {
        return DaysInMonthType.fromInt(this.daysInMonthType);
    }

    public DaysInYearType fetchDaysInYearType() {
        return DaysInYearType.fromInt(this.daysInYearType);
    }

    public void updateForFloatingInterestRates() {
        this.nominalInterestRatePerPeriod = null;
        this.interestPeriodFrequencyType = PeriodFrequencyType.INVALID;
        this.annualNominalInterestRate = null;
    }

    public void updateInterestRecognitionOnDisbursementDate(boolean interestRecognitionOnDisbursementDate) {
        this.interestRecognitionOnDisbursementDate = interestRecognitionOnDisbursementDate;
    }
}
