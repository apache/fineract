/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanproduct.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.common.domain.DaysInMonthType;
import org.mifosplatform.portfolio.common.domain.DaysInYearType;
import org.mifosplatform.portfolio.common.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.AprCalculator;
import org.mifosplatform.portfolio.loanproduct.LoanProductConstants;

/**
 * LoanRepaymentScheduleDetail encapsulates all the details of a
 * {@link LoanProduct} that are also used and persisted by a {@link Loan}.
 */
@Embeddable
public class LoanProductRelatedDetail implements LoanProductMinimumRepaymentScheduleRelatedDetail {

    @Embedded
    private MonetaryCurrency currency;

    @Column(name = "principal_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal principal;

    @Column(name = "nominal_interest_rate_per_period", scale = 6, precision = 19, nullable = false)
    private BigDecimal nominalInterestRatePerPeriod;

    // FIXME - move away form JPA ordinal use for enums using just integer -
    // requires sql patch for existing users of software.
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "interest_period_frequency_enum", nullable = false)
    private PeriodFrequencyType interestPeriodFrequencyType;

    @Column(name = "annual_nominal_interest_rate", scale = 6, precision = 19, nullable = false)
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

    @Column(name = "repay_every", nullable = false)
    private Integer repayEvery;

    // FIXME - move away form JPA ordinal use for enums using just integer -
    // requires sql patch for existing users of software.
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "repayment_period_frequency_enum", nullable = false)
    private PeriodFrequencyType repaymentPeriodFrequencyType;

    @Column(name = "number_of_repayments", nullable = false)
    private Integer numberOfRepayments;

    @Column(name = "grace_on_principal_periods", nullable = true)
    private Integer graceOnPrincipalPayment;

    @Column(name = "grace_on_interest_periods", nullable = true)
    private Integer graceOnInterestPayment;

    @Column(name = "grace_interest_free_periods", nullable = true)
    private Integer graceOnInterestCharged;

    // FIXME - move away form JPA ordinal use for enums using just integer -
    // requires sql patch for existing users of software.
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "amortization_method_enum", nullable = false)
    private AmortizationMethod amortizationMethod;

    @Column(name = "arrearstolerance_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal inArrearsTolerance;

    @Column(name = "grace_on_arrears_ageing", nullable = true)
    private Integer graceOnArrearsAgeing;

    @Column(name = "days_in_month_enum", nullable = false)
    private Integer daysInMonthType;

    @Column(name = "days_in_year_enum", nullable = false)
    private Integer daysInYearType;

    @Column(name = "interest_recalculation_enabled")
    private boolean isInterestRecalculationEnabled;

    public static LoanProductRelatedDetail createFrom(final MonetaryCurrency currency, final BigDecimal principal,
            final BigDecimal nominalInterestRatePerPeriod, final PeriodFrequencyType interestRatePeriodFrequencyType,
            final BigDecimal nominalAnnualInterestRate, final InterestMethod interestMethod,
            final InterestCalculationPeriodMethod interestCalculationPeriodMethod, final Integer repaymentEvery,
            final PeriodFrequencyType repaymentPeriodFrequencyType, final Integer numberOfRepayments,
            final Integer graceOnPrincipalPayment, final Integer graceOnInterestPayment, final Integer graceOnInterestCharged,
            final AmortizationMethod amortizationMethod, final BigDecimal inArrearsTolerance, final Integer graceOnArrearsAgeing,
            final Integer daysInMonthType, final Integer daysInYearType, final boolean isInterestRecalculationEnabled) {

        return new LoanProductRelatedDetail(currency, principal, nominalInterestRatePerPeriod, interestRatePeriodFrequencyType,
                nominalAnnualInterestRate, interestMethod, interestCalculationPeriodMethod, repaymentEvery, repaymentPeriodFrequencyType,
                numberOfRepayments, graceOnPrincipalPayment, graceOnInterestPayment, graceOnInterestCharged, amortizationMethod,
                inArrearsTolerance, graceOnArrearsAgeing, daysInMonthType, daysInYearType, isInterestRecalculationEnabled);
    }

    protected LoanProductRelatedDetail() {
        //
    }

    public LoanProductRelatedDetail(final MonetaryCurrency currency, final BigDecimal defaultPrincipal,
            final BigDecimal defaultNominalInterestRatePerPeriod, final PeriodFrequencyType interestPeriodFrequencyType,
            final BigDecimal defaultAnnualNominalInterestRate, final InterestMethod interestMethod,
            final InterestCalculationPeriodMethod interestCalculationPeriodMethod, final Integer repayEvery,
            final PeriodFrequencyType repaymentFrequencyType, final Integer defaultNumberOfRepayments,
            final Integer graceOnPrincipalPayment, final Integer graceOnInterestPayment, final Integer graceOnInterestCharged,
            final AmortizationMethod amortizationMethod, final BigDecimal inArrearsTolerance, final Integer graceOnArrearsAgeing,
            final Integer daysInMonthType, final Integer daysInYearType, final boolean isInterestRecalculationEnabled) {
        this.currency = currency;
        this.principal = defaultPrincipal;
        this.nominalInterestRatePerPeriod = defaultNominalInterestRatePerPeriod;
        this.interestPeriodFrequencyType = interestPeriodFrequencyType;
        this.annualNominalInterestRate = defaultAnnualNominalInterestRate;
        this.interestMethod = interestMethod;
        this.interestCalculationPeriodMethod = interestCalculationPeriodMethod;
        this.repayEvery = repayEvery;
        this.repaymentPeriodFrequencyType = repaymentFrequencyType;
        this.numberOfRepayments = defaultNumberOfRepayments;
        this.graceOnPrincipalPayment = defaultToNullIfZero(graceOnPrincipalPayment);
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
    }

    private Integer defaultToNullIfZero(final Integer value) {
        Integer defaultTo = value;
        if (value != null && Integer.valueOf(0).equals(value)) {
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
    public Money getInArrearsTolerance() {
        return Money.of(this.currency, this.inArrearsTolerance);
    }

    @Override
    public BigDecimal getNominalInterestRatePerPeriod() {
        return BigDecimal.valueOf(Double.valueOf(this.nominalInterestRatePerPeriod.stripTrailingZeros().toString()));
    }

    @Override
    public PeriodFrequencyType getInterestPeriodFrequencyType() {
        return this.interestPeriodFrequencyType;
    }

    @Override
    public BigDecimal getAnnualNominalInterestRate() {
        return BigDecimal.valueOf(Double.valueOf(this.annualNominalInterestRate.stripTrailingZeros().toString()));
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

            if (this.repaymentPeriodFrequencyType == PeriodFrequencyType.MONTHS) {
                final String repaymentFrequencyNthDayTypeParamName = "repaymentFrequencyNthDayType";
                newValue = command.integerValueOfParameterNamed(repaymentFrequencyNthDayTypeParamName);
                actualChanges.put(repaymentFrequencyNthDayTypeParamName, newValue);

                final String repaymentFrequencyDayOfWeekTypeParamName = "repaymentFrequencyDayOfWeekType";
                newValue = command.integerValueOfParameterNamed(repaymentFrequencyDayOfWeekTypeParamName);
                actualChanges.put(repaymentFrequencyDayOfWeekTypeParamName, newValue);

                actualChanges.put("locale", localeAsInput);
            }
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
        if (command.isChangeInIntegerParameterNamed(interestRateFrequencyTypeParamName, this.interestPeriodFrequencyType.getValue())) {
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
        if (command
                .isChangeInIntegerParameterNamed(interestCalculationPeriodTypeParamName, this.interestCalculationPeriodMethod.getValue())) {
            final Integer newValue = command.integerValueOfParameterNamed(interestCalculationPeriodTypeParamName);
            actualChanges.put(interestCalculationPeriodTypeParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.interestCalculationPeriodMethod = InterestCalculationPeriodMethod.fromInt(newValue);
        }

        final String graceOnPrincipalPaymentParamName = "graceOnPrincipalPayment";
        if (command.isChangeInIntegerParameterNamed(graceOnPrincipalPaymentParamName, this.graceOnPrincipalPayment)) {
            final Integer newValue = command.integerValueOfParameterNamed(graceOnPrincipalPaymentParamName);
            actualChanges.put(graceOnPrincipalPaymentParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.graceOnPrincipalPayment = newValue;
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

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.graceOnArrearsAgeingParameterName, this.graceOnArrearsAgeing)) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.graceOnArrearsAgeingParameterName);
            actualChanges.put(LoanProductConstants.graceOnArrearsAgeingParameterName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.graceOnArrearsAgeing = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.daysInMonthTypeParameterName, this.daysInMonthType)) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.daysInMonthTypeParameterName);
            actualChanges.put(LoanProductConstants.daysInMonthTypeParameterName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.daysInMonthType = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.daysInYearTypeParameterName, this.daysInYearType)) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.daysInYearTypeParameterName);
            actualChanges.put(LoanProductConstants.daysInYearTypeParameterName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.daysInYearType = newValue;
        }

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.isInterestRecalculationEnabledParameterName,
                this.isInterestRecalculationEnabled)) {
            final boolean newValue = command
                    .booleanPrimitiveValueOfParameterNamed(LoanProductConstants.isInterestRecalculationEnabledParameterName);
            actualChanges.put(LoanProductConstants.isInterestRecalculationEnabledParameterName, newValue);
            this.isInterestRecalculationEnabled = newValue;
        }

        validateRepaymentPeriodWithGraceSettings();

        return actualChanges;
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

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }

    private Integer defaultToZeroIfNull(final Integer value) {
        Integer result = value;
        if (value == null) {
            result = Integer.valueOf(0);
        }
        return result;
    }

    private void updateInterestRateDerivedFields(final AprCalculator aprCalculator) {
        this.annualNominalInterestRate = aprCalculator.calculateFrom(this.interestPeriodFrequencyType, this.nominalInterestRatePerPeriod);
    }

    public boolean hasCurrencyCodeOf(final String currencyCode) {
        return this.currency.getCode().equalsIgnoreCase(currencyCode);
    }

    public void updatenterestPeriodFrequencyType(final PeriodFrequencyType interestPeriodFrequencyType) {
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

}