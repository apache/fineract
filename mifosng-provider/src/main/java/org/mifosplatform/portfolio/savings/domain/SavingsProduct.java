/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import static org.mifosplatform.portfolio.savings.SavingsApiConstants.SAVINGS_PRODUCT_RESOURCE_NAME;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.accountingRuleParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.allowOverdraftParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.chargesParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.currencyCodeParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.descriptionParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.digitsAfterDecimalParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.enforceMinRequiredBalanceParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.inMultiplesOfParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.interestCalculationDaysInYearTypeParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.interestCalculationTypeParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.interestCompoundingPeriodTypeParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.interestPostingPeriodTypeParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.localeParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.lockinPeriodFrequencyParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.lockinPeriodFrequencyTypeParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.minBalanceForInterestCalculationParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.minRequiredBalanceParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.minRequiredOpeningBalanceParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.nameParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.nominalAnnualInterestRateParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.overdraftLimitParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.shortNameParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.withdrawalFeeForTransfersParamName;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.joda.time.LocalDate;
import org.mifosplatform.accounting.common.AccountingRuleType;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.charge.domain.Charge;
import org.mifosplatform.portfolio.interestratechart.domain.InterestRateChart;
import org.mifosplatform.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.mifosplatform.portfolio.savings.SavingsInterestCalculationDaysInYearType;
import org.mifosplatform.portfolio.savings.SavingsInterestCalculationType;
import org.mifosplatform.portfolio.savings.SavingsPeriodFrequencyType;
import org.mifosplatform.portfolio.savings.SavingsPostingInterestPeriodType;
import org.springframework.data.jpa.domain.AbstractPersistable;

import com.google.gson.JsonArray;

@Entity
@Table(name = "m_savings_product", uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }, name = "sp_unq_name"),
        @UniqueConstraint(columnNames = { "short_name" }, name = "sp_unq_short_name") })
@Inheritance
@DiscriminatorColumn(name = "deposit_type_enum", discriminatorType = DiscriminatorType.INTEGER)
@DiscriminatorValue("100")
public class SavingsProduct extends AbstractPersistable<Long> {

    @Column(name = "name", nullable = false, unique = true)
    protected String name;

    @Column(name = "short_name", nullable = false, unique = true)
    protected String shortName;

    @Column(name = "description", length = 500, nullable = false)
    protected String description;

    @Embedded
    protected MonetaryCurrency currency;

    @Column(name = "nominal_annual_interest_rate", scale = 6, precision = 19, nullable = false)
    protected BigDecimal nominalAnnualInterestRate;

    /**
     * The interest period is the span of time at the end of which savings in a
     * client's account earn interest.
     * 
     * A value from the {@link SavingsCompoundingInterestPeriodType}
     * enumeration.
     */
    @Column(name = "interest_compounding_period_enum", nullable = false)
    protected Integer interestCompoundingPeriodType;

    /**
     * A value from the {@link SavingsPostingInterestPeriodType} enumeration.
     */
    @Column(name = "interest_posting_period_enum", nullable = false)
    protected Integer interestPostingPeriodType;

    /**
     * A value from the {@link SavingsInterestCalculationType} enumeration.
     */
    @Column(name = "interest_calculation_type_enum", nullable = false)
    protected Integer interestCalculationType;

    /**
     * A value from the {@link SavingsInterestCalculationDaysInYearType}
     * enumeration.
     */
    @Column(name = "interest_calculation_days_in_year_type_enum", nullable = false)
    protected Integer interestCalculationDaysInYearType;

    @Column(name = "min_required_opening_balance", scale = 6, precision = 19, nullable = true)
    protected BigDecimal minRequiredOpeningBalance;

    @Column(name = "lockin_period_frequency", nullable = true)
    protected Integer lockinPeriodFrequency;

    @Column(name = "lockin_period_frequency_enum", nullable = true)
    protected Integer lockinPeriodFrequencyType;

    /**
     * A value from the {@link AccountingRuleType} enumeration.
     */
    @Column(name = "accounting_type", nullable = false)
    protected Integer accountingRule;

    @Column(name = "withdrawal_fee_for_transfer")
    protected boolean withdrawalFeeApplicableForTransfer;

    @ManyToMany
    @JoinTable(name = "m_savings_product_charge", joinColumns = @JoinColumn(name = "savings_product_id"), inverseJoinColumns = @JoinColumn(name = "charge_id"))
    protected Set<Charge> charges;

    @Column(name = "allow_overdraft")
    private boolean allowOverdraft;

    @Column(name = "overdraft_limit", scale = 6, precision = 19, nullable = true)
    private BigDecimal overdraftLimit;

    @Column(name = "enforce_min_required_balance")
    private boolean enforceMinRequiredBalance;

    @Column(name = "min_required_balance", scale = 6, precision = 19, nullable = true)
    private BigDecimal minRequiredBalance;

    @Column(name = "min_balance_for_interest_calculation", scale = 6, precision = 19, nullable = true)
    private BigDecimal minBalanceForInterestCalculation;

    public static SavingsProduct createNew(final String name, final String shortName, final String description,
            final MonetaryCurrency currency, final BigDecimal interestRate,
            final SavingsCompoundingInterestPeriodType interestCompoundingPeriodType,
            final SavingsPostingInterestPeriodType interestPostingPeriodType, final SavingsInterestCalculationType interestCalculationType,
            final SavingsInterestCalculationDaysInYearType interestCalculationDaysInYearType, final BigDecimal minRequiredOpeningBalance,
            final Integer lockinPeriodFrequency, final SavingsPeriodFrequencyType lockinPeriodFrequencyType,
            final boolean withdrawalFeeApplicableForTransfer, final AccountingRuleType accountingRuleType, final Set<Charge> charges,
            final boolean allowOverdraft, final BigDecimal overdraftLimit, final boolean enforceMinRequiredBalance,
            final BigDecimal minRequiredBalance, final BigDecimal minBalanceForInterestCalculation) {

        return new SavingsProduct(name, shortName, description, currency, interestRate, interestCompoundingPeriodType,
                interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType, minRequiredOpeningBalance,
                lockinPeriodFrequency, lockinPeriodFrequencyType, withdrawalFeeApplicableForTransfer, accountingRuleType, charges,
                allowOverdraft, overdraftLimit, enforceMinRequiredBalance, minRequiredBalance, minBalanceForInterestCalculation);
    }

    protected SavingsProduct() {
        this.name = null;
        this.description = null;
    }

    protected SavingsProduct(final String name, final String shortName, final String description, final MonetaryCurrency currency,
            final BigDecimal interestRate, final SavingsCompoundingInterestPeriodType interestCompoundingPeriodType,
            final SavingsPostingInterestPeriodType interestPostingPeriodType, final SavingsInterestCalculationType interestCalculationType,
            final SavingsInterestCalculationDaysInYearType interestCalculationDaysInYearType, final BigDecimal minRequiredOpeningBalance,
            final Integer lockinPeriodFrequency, final SavingsPeriodFrequencyType lockinPeriodFrequencyType,
            final boolean withdrawalFeeApplicableForTransfer, final AccountingRuleType accountingRuleType, final Set<Charge> charges,
            final boolean allowOverdraft, final BigDecimal overdraftLimit, BigDecimal minBalanceForInterestCalculation) {
        this(name, shortName, description, currency, interestRate, interestCompoundingPeriodType, interestPostingPeriodType,
                interestCalculationType, interestCalculationDaysInYearType, minRequiredOpeningBalance, lockinPeriodFrequency,
                lockinPeriodFrequencyType, withdrawalFeeApplicableForTransfer, accountingRuleType, charges, allowOverdraft, overdraftLimit,
                false, null, minBalanceForInterestCalculation);
    }

    protected SavingsProduct(final String name, final String shortName, final String description, final MonetaryCurrency currency,
            final BigDecimal interestRate, final SavingsCompoundingInterestPeriodType interestCompoundingPeriodType,
            final SavingsPostingInterestPeriodType interestPostingPeriodType, final SavingsInterestCalculationType interestCalculationType,
            final SavingsInterestCalculationDaysInYearType interestCalculationDaysInYearType, final BigDecimal minRequiredOpeningBalance,
            final Integer lockinPeriodFrequency, final SavingsPeriodFrequencyType lockinPeriodFrequencyType,
            final boolean withdrawalFeeApplicableForTransfer, final AccountingRuleType accountingRuleType, final Set<Charge> charges,
            final boolean allowOverdraft, final BigDecimal overdraftLimit, final boolean enforceMinRequiredBalance,
            final BigDecimal minRequiredBalance, BigDecimal minBalanceForInterestCalculation) {

        this.name = name;
        this.shortName = shortName;
        this.description = description;

        this.currency = currency;
        this.nominalAnnualInterestRate = interestRate;
        this.interestCompoundingPeriodType = interestCompoundingPeriodType.getValue();
        this.interestPostingPeriodType = interestPostingPeriodType.getValue();
        this.interestCalculationType = interestCalculationType.getValue();
        this.interestCalculationDaysInYearType = interestCalculationDaysInYearType.getValue();

        if (minRequiredOpeningBalance != null) {
            this.minRequiredOpeningBalance = Money.of(currency, minRequiredOpeningBalance).getAmount();
        }

        this.lockinPeriodFrequency = lockinPeriodFrequency;
        if (lockinPeriodFrequency != null && lockinPeriodFrequencyType != null) {
            this.lockinPeriodFrequencyType = lockinPeriodFrequencyType.getValue();
        }

        this.withdrawalFeeApplicableForTransfer = withdrawalFeeApplicableForTransfer;

        if (accountingRuleType != null) {
            this.accountingRule = accountingRuleType.getValue();
        }

        if (charges != null) {
            this.charges = charges;
        }

        validateLockinDetails();
        this.allowOverdraft = allowOverdraft;
        this.overdraftLimit = overdraftLimit;

        this.enforceMinRequiredBalance = enforceMinRequiredBalance;
        this.minRequiredBalance = minRequiredBalance;
        this.minBalanceForInterestCalculation = minBalanceForInterestCalculation;
    }

    public MonetaryCurrency currency() {
        return this.currency.copy();
    }

    public BigDecimal nominalAnnualInterestRate() {
        return this.nominalAnnualInterestRate;
    }

    public SavingsCompoundingInterestPeriodType interestCompoundingPeriodType() {
        return SavingsCompoundingInterestPeriodType.fromInt(this.interestCompoundingPeriodType);
    }

    public SavingsPostingInterestPeriodType interestPostingPeriodType() {
        return SavingsPostingInterestPeriodType.fromInt(this.interestPostingPeriodType);
    }

    public SavingsInterestCalculationType interestCalculationType() {
        return SavingsInterestCalculationType.fromInt(this.interestCalculationType);
    }

    public SavingsInterestCalculationDaysInYearType interestCalculationDaysInYearType() {
        return SavingsInterestCalculationDaysInYearType.fromInt(this.interestCalculationDaysInYearType);
    }

    public BigDecimal minRequiredOpeningBalance() {
        return this.minRequiredOpeningBalance;
    }

    public Integer lockinPeriodFrequency() {
        return this.lockinPeriodFrequency;
    }

    public SavingsPeriodFrequencyType lockinPeriodFrequencyType() {
        SavingsPeriodFrequencyType type = null;
        if (this.lockinPeriodFrequencyType != null) {
            type = SavingsPeriodFrequencyType.fromInt(this.lockinPeriodFrequencyType);
        }
        return type;
    }

    public Map<String, Object> update(final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(10);

        final String localeAsInput = command.locale();

        if (command.isChangeInStringParameterNamed(nameParamName, this.name)) {
            final String newValue = command.stringValueOfParameterNamed(nameParamName);
            actualChanges.put(nameParamName, newValue);
            this.name = newValue;
        }

        if (command.isChangeInStringParameterNamed(shortNameParamName, this.name)) {
            final String newValue = command.stringValueOfParameterNamed(shortNameParamName);
            actualChanges.put(shortNameParamName, newValue);
            this.shortName = newValue;
        }

        if (command.isChangeInStringParameterNamed(descriptionParamName, this.description)) {
            final String newValue = command.stringValueOfParameterNamed(descriptionParamName);
            actualChanges.put(descriptionParamName, newValue);
            this.description = newValue;
        }

        Integer digitsAfterDecimal = this.currency.getDigitsAfterDecimal();
        if (command.isChangeInIntegerParameterNamed(digitsAfterDecimalParamName, digitsAfterDecimal)) {
            final Integer newValue = command.integerValueOfParameterNamed(digitsAfterDecimalParamName);
            actualChanges.put(digitsAfterDecimalParamName, newValue);
            actualChanges.put(localeParamName, localeAsInput);
            digitsAfterDecimal = newValue;
            this.currency = new MonetaryCurrency(this.currency.getCode(), digitsAfterDecimal, this.currency.getCurrencyInMultiplesOf());
        }

        String currencyCode = this.currency.getCode();
        if (command.isChangeInStringParameterNamed(currencyCodeParamName, currencyCode)) {
            final String newValue = command.stringValueOfParameterNamed(currencyCodeParamName);
            actualChanges.put(currencyCodeParamName, newValue);
            currencyCode = newValue;
            this.currency = new MonetaryCurrency(currencyCode, this.currency.getDigitsAfterDecimal(),
                    this.currency.getCurrencyInMultiplesOf());
        }

        Integer inMultiplesOf = this.currency.getCurrencyInMultiplesOf();
        if (command.isChangeInIntegerParameterNamed(inMultiplesOfParamName, inMultiplesOf)) {
            final Integer newValue = command.integerValueOfParameterNamed(inMultiplesOfParamName);
            actualChanges.put(inMultiplesOfParamName, newValue);
            actualChanges.put(localeParamName, localeAsInput);
            inMultiplesOf = newValue;
            this.currency = new MonetaryCurrency(this.currency.getCode(), this.currency.getDigitsAfterDecimal(), inMultiplesOf);
        }

        if (command.isChangeInBigDecimalParameterNamed(nominalAnnualInterestRateParamName, this.nominalAnnualInterestRate)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(nominalAnnualInterestRateParamName);
            actualChanges.put(nominalAnnualInterestRateParamName, newValue);
            actualChanges.put(localeParamName, localeAsInput);
            this.nominalAnnualInterestRate = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(interestCompoundingPeriodTypeParamName, this.interestCompoundingPeriodType)) {
            final Integer newValue = command.integerValueOfParameterNamed(interestCompoundingPeriodTypeParamName);
            actualChanges.put(interestCompoundingPeriodTypeParamName, newValue);
            this.interestCompoundingPeriodType = SavingsCompoundingInterestPeriodType.fromInt(newValue).getValue();
        }

        if (command.isChangeInIntegerParameterNamed(interestPostingPeriodTypeParamName, this.interestPostingPeriodType)) {
            final Integer newValue = command.integerValueOfParameterNamed(interestPostingPeriodTypeParamName);
            actualChanges.put(interestPostingPeriodTypeParamName, newValue);
            this.interestPostingPeriodType = SavingsPostingInterestPeriodType.fromInt(newValue).getValue();
        }

        if (command.isChangeInIntegerParameterNamed(interestCalculationTypeParamName, this.interestCalculationType)) {
            final Integer newValue = command.integerValueOfParameterNamed(interestCalculationTypeParamName);
            actualChanges.put(interestCalculationTypeParamName, newValue);
            this.interestCalculationType = SavingsInterestCalculationType.fromInt(newValue).getValue();
        }

        if (command.isChangeInIntegerParameterNamed(interestCalculationDaysInYearTypeParamName, this.interestCalculationDaysInYearType)) {
            final Integer newValue = command.integerValueOfParameterNamed(interestCalculationDaysInYearTypeParamName);
            actualChanges.put(interestCalculationDaysInYearTypeParamName, newValue);
            this.interestCalculationDaysInYearType = SavingsInterestCalculationDaysInYearType.fromInt(newValue).getValue();
        }

        if (command.isChangeInBigDecimalParameterNamedDefaultingZeroToNull(minRequiredOpeningBalanceParamName,
                this.minRequiredOpeningBalance)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamedDefaultToNullIfZero(minRequiredOpeningBalanceParamName);
            actualChanges.put(minRequiredOpeningBalanceParamName, newValue);
            actualChanges.put(localeParamName, localeAsInput);
            this.minRequiredOpeningBalance = newValue;
        }

        if (command.isChangeInIntegerParameterNamedDefaultingZeroToNull(lockinPeriodFrequencyParamName, this.lockinPeriodFrequency)) {
            final Integer newValue = command.integerValueOfParameterNamedDefaultToNullIfZero(lockinPeriodFrequencyParamName);
            actualChanges.put(lockinPeriodFrequencyParamName, newValue);
            actualChanges.put(localeParamName, localeAsInput);
            this.lockinPeriodFrequency = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(lockinPeriodFrequencyTypeParamName, this.lockinPeriodFrequencyType)) {
            final Integer newValue = command.integerValueOfParameterNamed(lockinPeriodFrequencyTypeParamName);
            actualChanges.put(lockinPeriodFrequencyTypeParamName, newValue);
            this.lockinPeriodFrequencyType = newValue != null ? SavingsPeriodFrequencyType.fromInt(newValue).getValue() : newValue;
        }

        // set period type to null if frequency is null
        if (this.lockinPeriodFrequency == null) {
            this.lockinPeriodFrequencyType = null;
        }

        if (command.isChangeInBooleanParameterNamed(withdrawalFeeForTransfersParamName, this.withdrawalFeeApplicableForTransfer)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(withdrawalFeeForTransfersParamName);
            actualChanges.put(withdrawalFeeForTransfersParamName, newValue);
            this.withdrawalFeeApplicableForTransfer = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(accountingRuleParamName, this.accountingRule)) {
            final Integer newValue = command.integerValueOfParameterNamed(accountingRuleParamName);
            actualChanges.put(accountingRuleParamName, newValue);
            this.accountingRule = newValue;
        }

        // charges
        if (command.hasParameter(chargesParamName)) {
            final JsonArray jsonArray = command.arrayOfParameterNamed(chargesParamName);
            if (jsonArray != null) {
                actualChanges.put(chargesParamName, command.jsonFragment(chargesParamName));
            }
        }

        if (command.isChangeInBooleanParameterNamed(allowOverdraftParamName, this.allowOverdraft)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(allowOverdraftParamName);
            actualChanges.put(allowOverdraftParamName, newValue);
            this.allowOverdraft = newValue;
        }

        if (command.isChangeInBigDecimalParameterNamedDefaultingZeroToNull(overdraftLimitParamName, this.overdraftLimit)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamedDefaultToNullIfZero(overdraftLimitParamName);
            actualChanges.put(overdraftLimitParamName, newValue);
            actualChanges.put(localeParamName, localeAsInput);
            this.overdraftLimit = newValue;
        }
        if (!this.allowOverdraft) {
            this.overdraftLimit = null;
        }

        if (command.isChangeInBooleanParameterNamed(enforceMinRequiredBalanceParamName, this.enforceMinRequiredBalance)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(enforceMinRequiredBalanceParamName);
            actualChanges.put(enforceMinRequiredBalanceParamName, newValue);
            this.enforceMinRequiredBalance = newValue;
        }

        if (command.isChangeInBigDecimalParameterNamedDefaultingZeroToNull(minRequiredBalanceParamName, this.minRequiredBalance)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamedDefaultToNullIfZero(minRequiredBalanceParamName);
            actualChanges.put(minRequiredBalanceParamName, newValue);
            actualChanges.put(localeParamName, localeAsInput);
            this.minRequiredBalance = newValue;
        }

        if (command.isChangeInBigDecimalParameterNamedDefaultingZeroToNull(minBalanceForInterestCalculationParamName,
                this.minBalanceForInterestCalculation)) {
            final BigDecimal newValue = command
                    .bigDecimalValueOfParameterNamedDefaultToNullIfZero(minBalanceForInterestCalculationParamName);
            actualChanges.put(minBalanceForInterestCalculationParamName, newValue);
            actualChanges.put(localeParamName, localeAsInput);
            this.minBalanceForInterestCalculation = newValue;
        }

        validateLockinDetails();

        return actualChanges;
    }

    private void validateLockinDetails() {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_PRODUCT_RESOURCE_NAME);

        if (this.lockinPeriodFrequency == null) {
            baseDataValidator.reset().parameter(lockinPeriodFrequencyTypeParamName).value(this.lockinPeriodFrequencyType).ignoreIfNull()
                    .inMinMaxRange(0, 3);

            if (this.lockinPeriodFrequencyType != null) {
                baseDataValidator.reset().parameter(lockinPeriodFrequencyParamName).value(this.lockinPeriodFrequency).notNull()
                        .integerZeroOrGreater();
            }
        } else {
            baseDataValidator.reset().parameter(lockinPeriodFrequencyParamName).value(this.lockinPeriodFrequencyType)
                    .integerZeroOrGreater();
            baseDataValidator.reset().parameter(lockinPeriodFrequencyTypeParamName).value(this.lockinPeriodFrequencyType).notNull()
                    .inMinMaxRange(0, 3);
        }

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }

    public boolean isCashBasedAccountingEnabled() {
        return AccountingRuleType.CASH_BASED.getValue().equals(this.accountingRule);
    }

    // TODO this entire block is currently unnecessary as Savings does not have
    // accrual accounting
    public boolean isAccrualBasedAccountingEnabled() {
        return isUpfrontAccrualAccounting() || isPeriodicAccrualAccounting();
    }

    public boolean isPeriodicAccrualAccounting() {
        return AccountingRuleType.ACCRUAL_PERIODIC.getValue().equals(this.accountingRule);
    }

    public boolean isUpfrontAccrualAccounting() {
        return AccountingRuleType.ACCRUAL_UPFRONT.getValue().equals(this.accountingRule);
    }

    public Integer getAccountingType() {
        return this.accountingRule;
    }

    public boolean update(final Set<Charge> newSavingsProductCharges) {
        if (newSavingsProductCharges == null) { return false; }

        boolean updated = false;
        if (this.charges != null) {
            final Set<Charge> currentSetOfCharges = new HashSet<>(this.charges);
            final Set<Charge> newSetOfCharges = new HashSet<>(newSavingsProductCharges);

            if (!(currentSetOfCharges.equals(newSetOfCharges))) {
                updated = true;
                this.charges = newSavingsProductCharges;
            }
        } else {
            updated = true;
            this.charges = newSavingsProductCharges;
        }
        return updated;
    }

    public BigDecimal overdraftLimit() {
        return this.overdraftLimit;
    }

    public boolean isWithdrawalFeeApplicableForTransfer() {
        return this.withdrawalFeeApplicableForTransfer;
    }

    public boolean isAllowOverdraft() {
        return this.allowOverdraft;
    }

    public BigDecimal minRequiredBalance() {
        return this.minRequiredBalance;
    }

    public boolean isMinRequiredBalanceEnforced() {
        return this.enforceMinRequiredBalance;
    }

    public Set<Charge> charges() {
        return this.charges;
    }

    public InterestRateChart applicableChart(@SuppressWarnings("unused") final LocalDate target) {
        return null;
    }

    public InterestRateChart findChart(@SuppressWarnings("unused") Long chartId) {
        return null;
    }

    public BigDecimal minBalanceForInterestCalculation() {
        return this.minBalanceForInterestCalculation;
    }

    public String getShortName() {
        return this.shortName;
    }

}