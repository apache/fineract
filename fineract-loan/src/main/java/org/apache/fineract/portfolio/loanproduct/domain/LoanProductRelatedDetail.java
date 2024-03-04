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
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.common.domain.DaysInMonthType;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.AprCalculator;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanproduct.LoanProductConstants;

/**
 * LoanRepaymentScheduleDetail encapsulates all the details of a {@link LoanProduct} that are also used and persisted by
 * a {@link Loan}.
 */
@Embeddable
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
    private boolean allowPartialPeriodInterestCalcualtion;

    @Column(name = "repay_every", nullable = false)
    private Integer repayEvery;

    // FIXME - move away form JPA ordinal use for enums using just integer -
    // requires sql patch for existing users of software.
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "repayment_period_frequency_enum", nullable = false)
    private PeriodFrequencyType repaymentPeriodFrequencyType;

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

    public static LoanProductRelatedDetail createFrom(final MonetaryCurrency currency, final BigDecimal principal,
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
            final LoanScheduleProcessingType loanScheduleProcessingType) {

        return new LoanProductRelatedDetail(currency, principal, nominalInterestRatePerPeriod, interestRatePeriodFrequencyType,
                nominalAnnualInterestRate, interestMethod, interestCalculationPeriodMethod, allowPartialPeriodInterestCalcualtion,
                repaymentEvery, repaymentPeriodFrequencyType, numberOfRepayments, graceOnPrincipalPayment,
                recurringMoratoriumOnPrincipalPeriods, graceOnInterestPayment, graceOnInterestCharged, amortizationMethod,
                inArrearsTolerance, graceOnArrearsAgeing, daysInMonthType, daysInYearType, isInterestRecalculationEnabled,
                isEqualAmortization, enableDownPayment, disbursedAmountPercentageForDownPayment, enableAutoRepaymentForDownPayment,
                loanScheduleType, loanScheduleProcessingType);
    }

    protected LoanProductRelatedDetail() {
        //
    }

    public LoanProductRelatedDetail(final MonetaryCurrency currency, final BigDecimal defaultPrincipal,
            final BigDecimal defaultNominalInterestRatePerPeriod, final PeriodFrequencyType interestPeriodFrequencyType,
            final BigDecimal defaultAnnualNominalInterestRate, final InterestMethod interestMethod,
            final InterestCalculationPeriodMethod interestCalculationPeriodMethod, final boolean allowPartialPeriodInterestCalcualtion,
            final Integer repayEvery, final PeriodFrequencyType repaymentFrequencyType, final Integer defaultNumberOfRepayments,
            final Integer graceOnPrincipalPayment, final Integer recurringMoratoriumOnPrincipalPeriods,
            final Integer graceOnInterestPayment, final Integer graceOnInterestCharged, final AmortizationMethod amortizationMethod,
            final BigDecimal inArrearsTolerance, final Integer graceOnArrearsAgeing, final Integer daysInMonthType,
            final Integer daysInYearType, final boolean isInterestRecalculationEnabled, final boolean isEqualAmortization,
            final boolean enableDownPayment, final BigDecimal disbursedAmountPercentageForDownPayment,
            final boolean enableAutoRepaymentForDownPayment, final LoanScheduleType loanScheduleType,
            final LoanScheduleProcessingType loanScheduleProcessingType) {
        this.currency = currency;
        this.principal = defaultPrincipal;
        this.nominalInterestRatePerPeriod = defaultNominalInterestRatePerPeriod;
        this.interestPeriodFrequencyType = interestPeriodFrequencyType;
        this.annualNominalInterestRate = defaultAnnualNominalInterestRate;
        this.interestMethod = interestMethod;
        this.interestCalculationPeriodMethod = interestCalculationPeriodMethod;
        this.allowPartialPeriodInterestCalcualtion = allowPartialPeriodInterestCalcualtion;
        this.repayEvery = repayEvery;
        this.repaymentPeriodFrequencyType = repaymentFrequencyType;
        this.numberOfRepayments = defaultNumberOfRepayments;
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
    }

    private Integer defaultToNullIfZero(final Integer value) {
        Integer defaultTo = value;
        if (Integer.valueOf(0).equals(value)) {
            defaultTo = null;
        }
        return defaultTo;
    }

    @Override
    public MonetaryCurrency getCurrency() {
        return this.currency.copy();
    }

    @Override
    public Money getPrincipal() {
        return Money.of(this.currency, this.principal);
    }

    public void setPrincipal(BigDecimal principal) {
        this.principal = principal;
    }

    @Override
    public Integer graceOnInterestCharged() {
        return this.graceOnInterestCharged;
    }

    @Override
    public Integer graceOnInterestPayment() {
        return this.graceOnInterestPayment;
    }

    @Override
    public Integer graceOnPrincipalPayment() {
        return this.graceOnPrincipalPayment;
    }

    @Override
    public Integer recurringMoratoriumOnPrincipalPeriods() {
        return this.recurringMoratoriumOnPrincipalPeriods;
    }

    @Override
    public Money getInArrearsTolerance() {
        return Money.of(this.currency, this.inArrearsTolerance);
    }

    @Override
    public BigDecimal getNominalInterestRatePerPeriod() {
        return this.nominalInterestRatePerPeriod == null ? null
                : BigDecimal.valueOf(Double.parseDouble(this.nominalInterestRatePerPeriod.stripTrailingZeros().toString()));
    }

    @Override
    public PeriodFrequencyType getInterestPeriodFrequencyType() {
        return this.interestPeriodFrequencyType == null ? PeriodFrequencyType.INVALID : this.interestPeriodFrequencyType;
    }

    @Override
    public BigDecimal getAnnualNominalInterestRate() {
        return this.annualNominalInterestRate == null ? null
                : BigDecimal.valueOf(Double.parseDouble(this.annualNominalInterestRate.stripTrailingZeros().toString()));
    }

    @Override
    public InterestMethod getInterestMethod() {
        return this.interestMethod;
    }

    @Override
    public InterestCalculationPeriodMethod getInterestCalculationPeriodMethod() {
        return this.interestCalculationPeriodMethod;
    }

    @Override
    public Integer getRepayEvery() {
        return this.repayEvery;
    }

    @Override
    public PeriodFrequencyType getRepaymentPeriodFrequencyType() {
        return this.repaymentPeriodFrequencyType;
    }

    @Override
    public Integer getNumberOfRepayments() {
        return this.numberOfRepayments;
    }

    @Override
    public AmortizationMethod getAmortizationMethod() {
        return this.amortizationMethod;
    }

    public Map<String, Object> update(final JsonCommand command, final AprCalculator aprCalculator) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(20);

        final String localeAsInput = command.locale();

        String currencyCode = this.currency.getCode();
        Integer digitsAfterDecimal = this.currency.getDigitsAfterDecimal();
        Integer inMultiplesOf = this.currency.getCurrencyInMultiplesOf();

        final String digitsAfterDecimalParamName = "digitsAfterDecimal";
        if (command.isChangeInIntegerParameterNamed(digitsAfterDecimalParamName, digitsAfterDecimal)) {
            final Integer newValue = command.integerValueOfParameterNamed(digitsAfterDecimalParamName);
            actualChanges.put(digitsAfterDecimalParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            digitsAfterDecimal = newValue;
            this.currency = new MonetaryCurrency(currencyCode, digitsAfterDecimal, inMultiplesOf);
        }

        final String currencyCodeParamName = "currencyCode";
        if (command.isChangeInStringParameterNamed(currencyCodeParamName, currencyCode)) {
            final String newValue = command.stringValueOfParameterNamed(currencyCodeParamName);
            actualChanges.put(currencyCodeParamName, newValue);
            currencyCode = newValue;
            this.currency = new MonetaryCurrency(currencyCode, digitsAfterDecimal, inMultiplesOf);
        }

        final String inMultiplesOfParamName = "inMultiplesOf";
        if (command.isChangeInStringParameterNamed(inMultiplesOfParamName, currencyCode)) {
            final Integer newValue = command.integerValueOfParameterNamed(inMultiplesOfParamName);
            actualChanges.put(inMultiplesOfParamName, newValue);
            inMultiplesOf = newValue;
            this.currency = new MonetaryCurrency(currencyCode, digitsAfterDecimal, inMultiplesOf);
        }

        final String loanScheduleTypeParamName = LoanProductConstants.LOAN_SCHEDULE_TYPE;
        if (command.isChangeInStringParameterNamed(loanScheduleTypeParamName, loanScheduleType.toString())) {
            LoanScheduleType newLoanScheduleType = LoanScheduleType.valueOf(command.stringValueOfParameterNamed(loanScheduleTypeParamName));
            actualChanges.put(loanScheduleTypeParamName, newLoanScheduleType);
            loanScheduleType = newLoanScheduleType;
        }

        final String loanScheduleProcessingTypeParamName = LoanProductConstants.LOAN_SCHEDULE_PROCESSING_TYPE;
        if (command.isChangeInStringParameterNamed(loanScheduleProcessingTypeParamName, loanScheduleProcessingType.toString())) {
            LoanScheduleProcessingType newLoanScheduleProcessingType = LoanScheduleProcessingType
                    .valueOf(command.stringValueOfParameterNamed(loanScheduleProcessingTypeParamName));
            actualChanges.put(loanScheduleProcessingTypeParamName, newLoanScheduleProcessingType);
            loanScheduleProcessingType = newLoanScheduleProcessingType;
        }

        final Map<String, Object> loanApplicationAttributeChanges = updateLoanApplicationAttributes(command, aprCalculator);

        actualChanges.putAll(loanApplicationAttributeChanges);

        return actualChanges;
    }

    public Map<String, Object> updateLoanApplicationAttributes(final JsonCommand command, final AprCalculator aprCalculator) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(20);

        final String localeAsInput = command.locale();

        final String principalParamName = "principal";
        if (command.isChangeInBigDecimalParameterNamed(principalParamName, this.principal)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(principalParamName);
            actualChanges.put(principalParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.principal = newValue;
        }

        final String repaymentEveryParamName = "repaymentEvery";
        if (command.isChangeInIntegerParameterNamed(repaymentEveryParamName, this.repayEvery)) {
            final Integer newValue = command.integerValueOfParameterNamed(repaymentEveryParamName);
            actualChanges.put(repaymentEveryParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.repayEvery = newValue;
        }

        final String repaymentFrequencyTypeParamName = "repaymentFrequencyType";
        if (command.isChangeInIntegerParameterNamed(repaymentFrequencyTypeParamName, this.repaymentPeriodFrequencyType.getValue())) {
            Integer newValue = command.integerValueOfParameterNamed(repaymentFrequencyTypeParamName);
            actualChanges.put(repaymentFrequencyTypeParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.repaymentPeriodFrequencyType = PeriodFrequencyType.fromInt(newValue);
        }
        if (this.repaymentPeriodFrequencyType == PeriodFrequencyType.MONTHS) {
            Integer newValue = null;
            final String repaymentFrequencyNthDayTypeParamName = "repaymentFrequencyNthDayType";
            newValue = command.integerValueOfParameterNamed(repaymentFrequencyNthDayTypeParamName);
            actualChanges.put(repaymentFrequencyNthDayTypeParamName, newValue);

            final String repaymentFrequencyDayOfWeekTypeParamName = "repaymentFrequencyDayOfWeekType";
            newValue = command.integerValueOfParameterNamed(repaymentFrequencyDayOfWeekTypeParamName);
            actualChanges.put(repaymentFrequencyDayOfWeekTypeParamName, newValue);

            actualChanges.put("locale", localeAsInput);
        }

        final String numberOfRepaymentsParamName = "numberOfRepayments";
        if (command.isChangeInIntegerParameterNamed(numberOfRepaymentsParamName, this.numberOfRepayments)) {
            final Integer newValue = command.integerValueOfParameterNamed(numberOfRepaymentsParamName);
            actualChanges.put(numberOfRepaymentsParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.numberOfRepayments = newValue;
        }

        final String amortizationTypeParamName = "amortizationType";
        if (command.isChangeInIntegerParameterNamed(amortizationTypeParamName, this.amortizationMethod.getValue())) {
            final Integer newValue = command.integerValueOfParameterNamed(amortizationTypeParamName);
            actualChanges.put(amortizationTypeParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.amortizationMethod = AmortizationMethod.fromInt(newValue);
        }

        final String inArrearsToleranceParamName = "inArrearsTolerance";
        if (command.isChangeInBigDecimalParameterNamed(inArrearsToleranceParamName, this.inArrearsTolerance)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(inArrearsToleranceParamName);
            actualChanges.put(inArrearsToleranceParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.inArrearsTolerance = newValue;
        }

        final String interestRatePerPeriodParamName = "interestRatePerPeriod";
        if (command.isChangeInBigDecimalParameterNamed(interestRatePerPeriodParamName, this.nominalInterestRatePerPeriod)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(interestRatePerPeriodParamName);
            actualChanges.put(interestRatePerPeriodParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.nominalInterestRatePerPeriod = newValue;
            updateInterestRateDerivedFields(aprCalculator);
        }

        final String interestRateFrequencyTypeParamName = "interestRateFrequencyType";
        final int interestPeriodFrequencyType = this.interestPeriodFrequencyType == null ? PeriodFrequencyType.INVALID.getValue()
                : this.interestPeriodFrequencyType.getValue();
        if (command.isChangeInIntegerParameterNamed(interestRateFrequencyTypeParamName, interestPeriodFrequencyType)) {
            final Integer newValue = command.integerValueOfParameterNamed(interestRateFrequencyTypeParamName);
            actualChanges.put(interestRateFrequencyTypeParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.interestPeriodFrequencyType = PeriodFrequencyType.fromInt(newValue);
            updateInterestRateDerivedFields(aprCalculator);
        }

        final String interestTypeParamName = "interestType";
        if (command.isChangeInIntegerParameterNamed(interestTypeParamName, this.interestMethod.getValue())) {
            final Integer newValue = command.integerValueOfParameterNamed(interestTypeParamName);
            actualChanges.put(interestTypeParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.interestMethod = InterestMethod.fromInt(newValue);
        }

        final String interestCalculationPeriodTypeParamName = "interestCalculationPeriodType";
        if (command.isChangeInIntegerParameterNamed(interestCalculationPeriodTypeParamName,
                this.interestCalculationPeriodMethod.getValue())) {
            final Integer newValue = command.integerValueOfParameterNamed(interestCalculationPeriodTypeParamName);
            actualChanges.put(interestCalculationPeriodTypeParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.interestCalculationPeriodMethod = InterestCalculationPeriodMethod.fromInt(newValue);
        }

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.ALLOW_PARTIAL_PERIOD_INTEREST_CALCUALTION_PARAM_NAME,
                this.allowPartialPeriodInterestCalcualtion)) {
            final boolean newValue = command
                    .booleanPrimitiveValueOfParameterNamed(LoanProductConstants.ALLOW_PARTIAL_PERIOD_INTEREST_CALCUALTION_PARAM_NAME);
            actualChanges.put(LoanProductConstants.ALLOW_PARTIAL_PERIOD_INTEREST_CALCUALTION_PARAM_NAME, newValue);
            this.allowPartialPeriodInterestCalcualtion = newValue;
        }

        if (this.interestCalculationPeriodMethod.isDaily()) {
            this.allowPartialPeriodInterestCalcualtion = false;
        }

        final String graceOnPrincipalPaymentParamName = "graceOnPrincipalPayment";
        if (command.isChangeInIntegerParameterNamed(graceOnPrincipalPaymentParamName, this.graceOnPrincipalPayment)) {
            final Integer newValue = command.integerValueOfParameterNamed(graceOnPrincipalPaymentParamName);
            actualChanges.put(graceOnPrincipalPaymentParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.graceOnPrincipalPayment = newValue;
        }

        final String recurringMoratoriumOnPrincipalPeriodsParamName = "recurringMoratoriumOnPrincipalPeriods";
        if (command.isChangeInIntegerParameterNamed(recurringMoratoriumOnPrincipalPeriodsParamName,
                this.recurringMoratoriumOnPrincipalPeriods)) {
            final Integer newValue = command.integerValueOfParameterNamed(recurringMoratoriumOnPrincipalPeriodsParamName);
            actualChanges.put(recurringMoratoriumOnPrincipalPeriodsParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.recurringMoratoriumOnPrincipalPeriods = newValue;
        }

        final String graceOnInterestPaymentParamName = "graceOnInterestPayment";
        if (command.isChangeInIntegerParameterNamed(graceOnInterestPaymentParamName, this.graceOnInterestPayment)) {
            final Integer newValue = command.integerValueOfParameterNamed(graceOnInterestPaymentParamName);
            actualChanges.put(graceOnInterestPaymentParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.graceOnInterestPayment = newValue;
        }

        final String graceOnInterestChargedParamName = "graceOnInterestCharged";
        if (command.isChangeInIntegerParameterNamed(graceOnInterestChargedParamName, this.graceOnInterestCharged)) {
            final Integer newValue = command.integerValueOfParameterNamed(graceOnInterestChargedParamName);
            actualChanges.put(graceOnInterestChargedParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.graceOnInterestCharged = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.GRACE_ON_ARREARS_AGEING_PARAMETER_NAME,
                this.graceOnArrearsAgeing)) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.GRACE_ON_ARREARS_AGEING_PARAMETER_NAME);
            actualChanges.put(LoanProductConstants.GRACE_ON_ARREARS_AGEING_PARAMETER_NAME, newValue);
            actualChanges.put("locale", localeAsInput);
            this.graceOnArrearsAgeing = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.DAYS_IN_MONTH_TYPE_PARAMETER_NAME, this.daysInMonthType)) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.DAYS_IN_MONTH_TYPE_PARAMETER_NAME);
            actualChanges.put(LoanProductConstants.DAYS_IN_MONTH_TYPE_PARAMETER_NAME, newValue);
            actualChanges.put("locale", localeAsInput);
            this.daysInMonthType = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.DAYS_IN_YEAR_TYPE_PARAMETER_NAME, this.daysInYearType)) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.DAYS_IN_YEAR_TYPE_PARAMETER_NAME);
            actualChanges.put(LoanProductConstants.DAYS_IN_YEAR_TYPE_PARAMETER_NAME, newValue);
            actualChanges.put("locale", localeAsInput);
            this.daysInYearType = newValue;
        }

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.IS_INTEREST_RECALCULATION_ENABLED_PARAMETER_NAME,
                this.isInterestRecalculationEnabled)) {
            final boolean newValue = command
                    .booleanPrimitiveValueOfParameterNamed(LoanProductConstants.IS_INTEREST_RECALCULATION_ENABLED_PARAMETER_NAME);
            actualChanges.put(LoanProductConstants.IS_INTEREST_RECALCULATION_ENABLED_PARAMETER_NAME, newValue);
            this.isInterestRecalculationEnabled = newValue;
        }

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.IS_EQUAL_AMORTIZATION_PARAM, this.isEqualAmortization)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.IS_EQUAL_AMORTIZATION_PARAM);
            actualChanges.put(LoanProductConstants.IS_EQUAL_AMORTIZATION_PARAM, newValue);
            this.isEqualAmortization = newValue;
        }

        validateRepaymentPeriodWithGraceSettings();

        return actualChanges;
    }

    public void updateCurrency(final MonetaryCurrency currency) {
        this.currency = currency;
    }

    public void validateRepaymentPeriodWithGraceSettings() {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loanproduct");

        if (this.numberOfRepayments <= defaultToZeroIfNull(this.graceOnPrincipalPayment)) {
            baseDataValidator.reset().parameter("graceOnPrincipalPayment").value(this.graceOnPrincipalPayment)
                    .failWithCode(".mustBeLessThan.numberOfRepayments");
        }

        if (this.numberOfRepayments <= defaultToZeroIfNull(this.graceOnInterestPayment)) {
            baseDataValidator.reset().parameter("graceOnInterestPayment").value(this.graceOnInterestPayment)
                    .failWithCode(".mustBeLessThan.numberOfRepayments");
        }

        if (this.numberOfRepayments < defaultToZeroIfNull(this.graceOnInterestCharged)) {
            baseDataValidator.reset().parameter("graceOnInterestCharged").value(this.graceOnInterestCharged)
                    .failWithCode(".mustBeLessThan.numberOfRepayments");
        }

        int graceOnPrincipal = 0;
        if (this.getGraceOnPrincipalPayment() != null) {
            graceOnPrincipal = this.getGraceOnPrincipalPayment();
        }
        int recurMoratoriumOnPrincipal = 0;
        if (this.recurringMoratoriumOnPrincipalPeriods() != null) {
            recurMoratoriumOnPrincipal = this.recurringMoratoriumOnPrincipalPeriods();
        }

        if ((recurMoratoriumOnPrincipal > 0) && ((this.numberOfRepayments - graceOnPrincipal) % (recurMoratoriumOnPrincipal + 1) != 1)) {
            baseDataValidator.reset().parameter("graceOnPrincipalPayments.and.recurringMoratoriumOnPrincipalPeriods")
                    .value(graceOnPrincipal).value(recurMoratoriumOnPrincipal)
                    .failWithCode("causes.principal.moratorium.for.last.installment");
        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    private Integer defaultToZeroIfNull(final Integer value) {
        Integer result = value;
        if (value == null) {
            result = 0;
        }
        return result;
    }

    private void updateInterestRateDerivedFields(final AprCalculator aprCalculator) {
        this.annualNominalInterestRate = aprCalculator.calculateFrom(this.interestPeriodFrequencyType, this.nominalInterestRatePerPeriod,
                this.numberOfRepayments, this.repayEvery, this.repaymentPeriodFrequencyType);

    }

    public boolean hasCurrencyCodeOf(final String currencyCode) {
        return this.currency.getCode().equalsIgnoreCase(currencyCode);
    }

    public void updateInterestPeriodFrequencyType(final PeriodFrequencyType interestPeriodFrequencyType) {
        this.interestPeriodFrequencyType = interestPeriodFrequencyType;
    }

    @Override
    public Integer getGraceOnDueDate() {
        return this.graceOnArrearsAgeing;
    }

    public DaysInMonthType fetchDaysInMonthType() {
        return DaysInMonthType.fromInt(this.daysInMonthType);
    }

    public DaysInYearType fetchDaysInYearType() {
        return DaysInYearType.fromInt(this.daysInYearType);
    }

    public boolean isInterestRecalculationEnabled() {
        return this.isInterestRecalculationEnabled;
    }

    public void updateIsInterestRecalculationEnabled(final boolean isInterestRecalculationEnabled) {
        this.isInterestRecalculationEnabled = isInterestRecalculationEnabled;
    }

    public void updateNumberOfRepayments(Integer numberOfRepayments) {
        this.numberOfRepayments = numberOfRepayments;
    }

    public Integer getGraceOnPrincipalPayment() {
        return graceOnPrincipalPayment;
    }

    public void setGraceOnPrincipalPayment(Integer graceOnPrincipalPayment) {
        this.graceOnPrincipalPayment = graceOnPrincipalPayment;
    }

    public Integer getGraceOnInterestPayment() {
        return graceOnInterestPayment;
    }

    public void setGraceOnInterestPayment(Integer graceOnInterestPayment) {
        this.graceOnInterestPayment = graceOnInterestPayment;
    }

    public Integer getGraceOnArrearsAgeing() {
        return graceOnArrearsAgeing;
    }

    public void setGraceOnArrearsAgeing(Integer graceOnArrearsAgeing) {
        this.graceOnArrearsAgeing = graceOnArrearsAgeing;
    }

    public void setInterestMethod(InterestMethod interestMethod) {
        this.interestMethod = interestMethod;
    }

    public void setInterestCalculationPeriodMethod(InterestCalculationPeriodMethod interestCalculationPeriodMethod) {
        this.interestCalculationPeriodMethod = interestCalculationPeriodMethod;
    }

    public void setRepayEvery(Integer repayEvery) {
        this.repayEvery = repayEvery;
    }

    public void setRepaymentPeriodFrequencyType(PeriodFrequencyType repaymentPeriodFrequencyType) {
        this.repaymentPeriodFrequencyType = repaymentPeriodFrequencyType;
    }

    public void setAmortizationMethod(AmortizationMethod amortizationMethod) {
        this.amortizationMethod = amortizationMethod;
    }

    public void setInArrearsTolerance(BigDecimal inArrearsTolerance) {
        this.inArrearsTolerance = inArrearsTolerance;
    }

    public BigDecimal getArrearsTolerance() {
        return this.inArrearsTolerance;
    }

    public void updateForFloatingInterestRates() {
        this.nominalInterestRatePerPeriod = null;
        this.interestPeriodFrequencyType = PeriodFrequencyType.INVALID;
        this.annualNominalInterestRate = null;
    }

    public boolean isAllowPartialPeriodInterestCalcualtion() {
        return this.allowPartialPeriodInterestCalcualtion;
    }

    public boolean isEqualAmortization() {
        return isEqualAmortization;
    }

    public void setEqualAmortization(boolean isEqualAmortization) {
        this.isEqualAmortization = isEqualAmortization;
    }

    public void setNominalInterestRatePerPeriod(BigDecimal nominalInterestRatePerPeriod) {
        this.nominalInterestRatePerPeriod = nominalInterestRatePerPeriod;
    }

    public boolean isEnableDownPayment() {
        return enableDownPayment;
    }

    public BigDecimal getDisbursedAmountPercentageForDownPayment() {
        return disbursedAmountPercentageForDownPayment;
    }

    public void updateEnableDownPayment(boolean enableDownPayment) {
        this.enableDownPayment = enableDownPayment;
    }

    public void updateDisbursedAmountPercentageForDownPayment(BigDecimal disbursedAmountPercentageForDownPayment) {
        this.disbursedAmountPercentageForDownPayment = disbursedAmountPercentageForDownPayment;
    }

    public Boolean isEnableAutoRepaymentForDownPayment() {
        return enableAutoRepaymentForDownPayment;
    }

    public void updateEnableAutoRepaymentForDownPayment(boolean enableAutoRepaymentForDownPayment) {
        this.enableAutoRepaymentForDownPayment = enableAutoRepaymentForDownPayment;
    }

    public LoanScheduleType getLoanScheduleType() {
        return loanScheduleType;
    }

    public LoanScheduleProcessingType getLoanScheduleProcessingType() {
        return loanScheduleProcessingType;
    }
}
