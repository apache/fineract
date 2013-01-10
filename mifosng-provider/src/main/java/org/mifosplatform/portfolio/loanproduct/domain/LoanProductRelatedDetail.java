package org.mifosplatform.portfolio.loanproduct.domain;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.AprCalculator;

/**
 * LoanRepaymentScheduleDetail encapsulates all the details of a
 * {@link LoanProduct} that are also used and persisted by a {@link Loan}.
 */
@Embeddable
public class LoanProductRelatedDetail implements LoanProductMinimumRepaymentScheduleRelatedDetail {

    @Embedded
    private MonetaryCurrency currency;

    @Column(name = "principal_amount", scale = 6, precision = 19, nullable = false)
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

    // FIXME - move away form JPA ordinal use for enums using just integer -
    // requires sql patch for existing users of software.
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "amortization_method_enum", nullable = false)
    private AmortizationMethod amortizationMethod;

    @Column(name = "arrearstolerance_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal inArrearsTolerance;

    public static LoanProductRelatedDetail createFrom(final MonetaryCurrency currency, final BigDecimal principal,
            final BigDecimal nominalInterestRatePerPeriod, final PeriodFrequencyType interestRatePeriodFrequencyType,
            final BigDecimal nominalAnnualInterestRate, final InterestMethod interestMethod,
            final InterestCalculationPeriodMethod interestCalculationPeriodMethod, final Integer repaymentEvery,
            final PeriodFrequencyType repaymentPeriodFrequencyType, final Integer numberOfRepayments,
            final AmortizationMethod amortizationMethod, final BigDecimal inArrearsTolerance) {

        return new LoanProductRelatedDetail(currency, principal, nominalInterestRatePerPeriod, interestRatePeriodFrequencyType,
                nominalAnnualInterestRate, interestMethod, interestCalculationPeriodMethod, repaymentEvery, repaymentPeriodFrequencyType,
                numberOfRepayments, amortizationMethod, inArrearsTolerance);
    }

    protected LoanProductRelatedDetail() {
        //
    }

    public LoanProductRelatedDetail(final MonetaryCurrency currency, final BigDecimal defaultPrincipal,
            final BigDecimal defaultNominalInterestRatePerPeriod, final PeriodFrequencyType interestPeriodFrequencyType,
            final BigDecimal defaultAnnualNominalInterestRate, final InterestMethod interestMethod,
            final InterestCalculationPeriodMethod interestCalculationPeriodMethod, final Integer repayEvery,
            final PeriodFrequencyType repaymentFrequencyType, final Integer defaultNumberOfRepayments,
            final AmortizationMethod amortizationMethod, final BigDecimal inArrearsTolerance) {
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
        this.amortizationMethod = amortizationMethod;
        if (inArrearsTolerance != null && BigDecimal.ZERO.compareTo(inArrearsTolerance) == 0) {
            this.inArrearsTolerance = null;
        } else {
            this.inArrearsTolerance = inArrearsTolerance;
        }
    }

    public MonetaryCurrency getCurrency() {
        return this.currency.copy();
    }

    public Money getPrincipal() {
        return Money.of(this.currency, this.principal);
    }

    public Money getInArrearsTolerance() {
        return Money.of(this.currency, this.inArrearsTolerance);
    }

    public BigDecimal getNominalInterestRatePerPeriod() {
        return BigDecimal.valueOf(Double.valueOf(this.nominalInterestRatePerPeriod.stripTrailingZeros().toString()));
    }

    public PeriodFrequencyType getInterestPeriodFrequencyType() {
        return interestPeriodFrequencyType;
    }

    public BigDecimal getAnnualNominalInterestRate() {
        return BigDecimal.valueOf(Double.valueOf(this.annualNominalInterestRate.stripTrailingZeros().toString()));
    }

    public InterestMethod getInterestMethod() {
        return interestMethod;
    }

    public InterestCalculationPeriodMethod getInterestCalculationPeriodMethod() {
        return interestCalculationPeriodMethod;
    }

    @Override
    public Integer getRepayEvery() {
        return repayEvery;
    }

    @Override
    public PeriodFrequencyType getRepaymentPeriodFrequencyType() {
        return repaymentPeriodFrequencyType;
    }

    @Override
    public Integer getNumberOfRepayments() {
        return numberOfRepayments;
    }

    public AmortizationMethod getAmortizationMethod() {
        return amortizationMethod;
    }

    public Map<String, Object> update(final JsonCommand command, final AprCalculator aprCalculator) {

        final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(20);

        final String localeAsInput = command.locale();

        String currencyCode = this.currency.getCode();
        Integer digitsAfterDecimal = this.currency.getDigitsAfterDecimal();

        final String digitsAfterDecimalParamName = "digitsAfterDecimal";
        if (command.isChangeInIntegerParameterNamed(digitsAfterDecimalParamName, digitsAfterDecimal)) {
            final Integer newValue = command.integerValueOfParameterNamed(digitsAfterDecimalParamName);
            actualChanges.put(digitsAfterDecimalParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            digitsAfterDecimal = newValue;
            this.currency = new MonetaryCurrency(currencyCode, digitsAfterDecimal);
        }

        final String currencyCodeParamName = "currencyCode";
        if (command.isChangeInStringParameterNamed(currencyCodeParamName, currencyCode)) {
            final String newValue = command.stringValueOfParameterNamed(currencyCodeParamName);
            actualChanges.put(currencyCodeParamName, newValue);
            currencyCode = newValue;
            this.currency = new MonetaryCurrency(currencyCode, digitsAfterDecimal);
        }

        final Map<String, Object> loanApplicationAttributeChanges = updateLoanApplicationAttributes(command, aprCalculator);

        actualChanges.putAll(loanApplicationAttributeChanges);

        return actualChanges;
    }

    public Map<String, Object> updateLoanApplicationAttributes(final JsonCommand command, final AprCalculator aprCalculator) {
        final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(20);

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
            final Integer newValue = command.integerValueOfParameterNamed(repaymentFrequencyTypeParamName);
            actualChanges.put(repaymentFrequencyTypeParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.repaymentPeriodFrequencyType = PeriodFrequencyType.fromInt(newValue);
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

        return actualChanges;
    }

    private void updateInterestRateDerivedFields(final AprCalculator aprCalculator) {
        this.annualNominalInterestRate = aprCalculator.calculateFrom(this.interestPeriodFrequencyType, this.nominalInterestRatePerPeriod);
    }
}