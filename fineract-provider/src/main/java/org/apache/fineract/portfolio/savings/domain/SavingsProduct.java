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
package org.apache.fineract.portfolio.savings.domain;

import static org.apache.fineract.portfolio.savings.SavingsApiConstants.SAVINGS_PRODUCT_RESOURCE_NAME;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.accountingRuleParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.allowOverdraftParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.chargesParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.currencyCodeParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.daysToDormancyParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.daysToEscheatParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.daysToInactiveParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.descriptionParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.digitsAfterDecimalParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.enforceMinRequiredBalanceParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.inMultiplesOfParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.interestCalculationDaysInYearTypeParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.interestCalculationTypeParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.interestCompoundingPeriodTypeParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.interestPostingPeriodTypeParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.isDormancyTrackingActiveParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.localeParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.lockinPeriodFrequencyParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.lockinPeriodFrequencyTypeParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.minBalanceForInterestCalculationParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.minOverdraftForInterestCalculationParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.minRequiredBalanceParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.minRequiredOpeningBalanceParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.nameParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.nominalAnnualInterestRateOverdraftParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.nominalAnnualInterestRateParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.overdraftLimitParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.shortNameParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.taxGroupIdParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.withHoldTaxParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.withdrawalFeeForTransfersParamName;

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
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.fineract.accounting.common.AccountingRuleType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.interestratechart.domain.InterestRateChart;
import org.apache.fineract.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationDaysInYearType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationType;
import org.apache.fineract.portfolio.savings.SavingsPeriodFrequencyType;
import org.apache.fineract.portfolio.savings.SavingsPostingInterestPeriodType;
import org.apache.fineract.portfolio.tax.domain.TaxGroup;
import org.joda.time.LocalDate;

import com.google.gson.JsonArray;

@Entity
@Table(name = "m_savings_product", uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }, name = "sp_unq_name"),
        @UniqueConstraint(columnNames = { "short_name" }, name = "sp_unq_short_name") })
@Inheritance
@DiscriminatorColumn(name = "deposit_type_enum", discriminatorType = DiscriminatorType.INTEGER)
@DiscriminatorValue("100")
public class SavingsProduct extends AbstractPersistableCustom<Long> {

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

    @Column(name = "nominal_annual_interest_rate_overdraft", scale = 6, precision = 19, nullable = true)
    private BigDecimal nominalAnnualInterestRateOverdraft;

    @Column(name = "min_overdraft_for_interest_calculation", scale = 6, precision = 19, nullable = true)
    private BigDecimal minOverdraftForInterestCalculation;

    @Column(name = "enforce_min_required_balance")
    private boolean enforceMinRequiredBalance;

    @Column(name = "min_required_balance", scale = 6, precision = 19, nullable = true)
    private BigDecimal minRequiredBalance;

    @Column(name = "min_balance_for_interest_calculation", scale = 6, precision = 19, nullable = true)
    private BigDecimal minBalanceForInterestCalculation;

    @Column(name = "withhold_tax", nullable = false)
    private boolean withHoldTax;

    @ManyToOne
    @JoinColumn(name = "tax_group_id")
    private TaxGroup taxGroup;

	@Column(name = "is_dormancy_tracking_active")
	private Boolean isDormancyTrackingActive;

    @Column(name = "days_to_inactive")
	private Long daysToInactive;

    @Column(name = "days_to_dormancy")
	private Long daysToDormancy;

    @Column(name = "days_to_escheat")
	private Long daysToEscheat;

    public static SavingsProduct createNew(final String name, final String shortName, final String description,
            final MonetaryCurrency currency, final BigDecimal interestRate,
            final SavingsCompoundingInterestPeriodType interestCompoundingPeriodType,
            final SavingsPostingInterestPeriodType interestPostingPeriodType, final SavingsInterestCalculationType interestCalculationType,
            final SavingsInterestCalculationDaysInYearType interestCalculationDaysInYearType, final BigDecimal minRequiredOpeningBalance,
            final Integer lockinPeriodFrequency, final SavingsPeriodFrequencyType lockinPeriodFrequencyType,
            final boolean withdrawalFeeApplicableForTransfer, final AccountingRuleType accountingRuleType, final Set<Charge> charges,
            final boolean allowOverdraft, final BigDecimal overdraftLimit, final boolean enforceMinRequiredBalance,
            final BigDecimal minRequiredBalance, final BigDecimal minBalanceForInterestCalculation,
            final BigDecimal nominalAnnualInterestRateOverdraft, final BigDecimal minOverdraftForInterestCalculation,
            boolean withHoldTax, TaxGroup taxGroup, 
            final Boolean isDormancyTrackingActive, final Long daysToInactive, final Long daysToDormancy, final Long daysToEscheat) {

        return new SavingsProduct(name, shortName, description, currency, interestRate, interestCompoundingPeriodType,
                interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType, minRequiredOpeningBalance,
                lockinPeriodFrequency, lockinPeriodFrequencyType, withdrawalFeeApplicableForTransfer, accountingRuleType, charges,
                allowOverdraft, overdraftLimit, enforceMinRequiredBalance, minRequiredBalance, minBalanceForInterestCalculation,
                nominalAnnualInterestRateOverdraft, minOverdraftForInterestCalculation, withHoldTax, taxGroup,
                isDormancyTrackingActive, daysToInactive, daysToDormancy, daysToEscheat);
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
            final boolean allowOverdraft, final BigDecimal overdraftLimit, BigDecimal minBalanceForInterestCalculation,
            boolean withHoldTax, TaxGroup taxGroup) {
        this(name, shortName, description, currency, interestRate, interestCompoundingPeriodType, interestPostingPeriodType,
                interestCalculationType, interestCalculationDaysInYearType, minRequiredOpeningBalance, lockinPeriodFrequency,
                lockinPeriodFrequencyType, withdrawalFeeApplicableForTransfer, accountingRuleType, charges, allowOverdraft, overdraftLimit,
                false, null, minBalanceForInterestCalculation, null, null, withHoldTax, taxGroup, null, null, null, null);
    }

    protected SavingsProduct(final String name, final String shortName, final String description, final MonetaryCurrency currency,
            final BigDecimal interestRate, final SavingsCompoundingInterestPeriodType interestCompoundingPeriodType,
            final SavingsPostingInterestPeriodType interestPostingPeriodType, final SavingsInterestCalculationType interestCalculationType,
            final SavingsInterestCalculationDaysInYearType interestCalculationDaysInYearType, final BigDecimal minRequiredOpeningBalance,
            final Integer lockinPeriodFrequency, final SavingsPeriodFrequencyType lockinPeriodFrequencyType,
            final boolean withdrawalFeeApplicableForTransfer, final AccountingRuleType accountingRuleType, final Set<Charge> charges,
            final boolean allowOverdraft, final BigDecimal overdraftLimit, final boolean enforceMinRequiredBalance,
            final BigDecimal minRequiredBalance, BigDecimal minBalanceForInterestCalculation,
            final BigDecimal nominalAnnualInterestRateOverdraft, final BigDecimal minOverdraftForInterestCalculation,
            final boolean withHoldTax, final TaxGroup taxGroup,
            final Boolean isDormancyTrackingActive, final Long daysToInactive, final Long daysToDormancy, final Long daysToEscheat) {

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
        this.nominalAnnualInterestRateOverdraft = nominalAnnualInterestRateOverdraft;
        this.minOverdraftForInterestCalculation = minOverdraftForInterestCalculation;

        esnureOverdraftLimitsSetForOverdraftAccounts();

        this.enforceMinRequiredBalance = enforceMinRequiredBalance;
        this.minRequiredBalance = minRequiredBalance;
        this.minBalanceForInterestCalculation = minBalanceForInterestCalculation;
        this.withHoldTax = withHoldTax;
        this.taxGroup = taxGroup;
        
        if(isDormancyTrackingActive == null) {
            this.isDormancyTrackingActive = Boolean.FALSE ;
        }else {
            this.isDormancyTrackingActive = isDormancyTrackingActive;    
        }
        
        this.daysToInactive = daysToInactive;
        this.daysToDormancy = daysToDormancy;
        this.daysToEscheat = daysToEscheat;
    }

    /**
     * If overdrafts are allowed and the overdraft limit is not set, set the
     * same to Zero
     **/
    private void esnureOverdraftLimitsSetForOverdraftAccounts() {

        if (this.allowOverdraft) {
            this.overdraftLimit = this.overdraftLimit == null ? BigDecimal.ZERO : this.overdraftLimit;
            this.nominalAnnualInterestRateOverdraft = this.nominalAnnualInterestRateOverdraft == null ? BigDecimal.ZERO
                    : this.nominalAnnualInterestRateOverdraft;
            this.minOverdraftForInterestCalculation = this.minOverdraftForInterestCalculation == null ? BigDecimal.ZERO
                    : this.minOverdraftForInterestCalculation;
        }
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

        if (command.isChangeInBigDecimalParameterNamedDefaultingZeroToNull(nominalAnnualInterestRateOverdraftParamName,
                this.nominalAnnualInterestRateOverdraft)) {
            final BigDecimal newValue = command
                    .bigDecimalValueOfParameterNamedDefaultToNullIfZero(nominalAnnualInterestRateOverdraftParamName);
            actualChanges.put(nominalAnnualInterestRateOverdraftParamName, newValue);
            actualChanges.put(localeParamName, localeAsInput);
            this.nominalAnnualInterestRateOverdraft = newValue;
        }

        if (command.isChangeInBigDecimalParameterNamedDefaultingZeroToNull(minOverdraftForInterestCalculationParamName,
                this.minOverdraftForInterestCalculation)) {
            final BigDecimal newValue = command
                    .bigDecimalValueOfParameterNamedDefaultToNullIfZero(minOverdraftForInterestCalculationParamName);
            actualChanges.put(minOverdraftForInterestCalculationParamName, newValue);
            actualChanges.put(localeParamName, localeAsInput);
            this.minOverdraftForInterestCalculation = newValue;
        }

        if (!this.allowOverdraft) {
            this.overdraftLimit = null;
            this.nominalAnnualInterestRateOverdraft = null;
            this.minOverdraftForInterestCalculation = null;
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

        if (command.isChangeInBooleanParameterNamed(withHoldTaxParamName, this.withHoldTax)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(withHoldTaxParamName);
            actualChanges.put(withHoldTaxParamName, newValue);
            this.withHoldTax = newValue;
        }

        if (this.withHoldTax) {
            if (this.taxGroup == null || command.isChangeInLongParameterNamed(taxGroupIdParamName, this.taxGroup.getId())) {
                final Long newValue = command.longValueOfParameterNamed(taxGroupIdParamName);
                actualChanges.put(taxGroupIdParamName, newValue);
            }
        } else {
            this.taxGroup = null;
        }
        
        if(command.isChangeInBooleanParameterNamed(isDormancyTrackingActiveParamName, this.isDormancyTrackingActive)){
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(isDormancyTrackingActiveParamName);
            actualChanges.put(isDormancyTrackingActiveParamName, newValue);
            this.isDormancyTrackingActive = newValue;
        }

        if(command.isChangeInLongParameterNamed(daysToInactiveParamName, this.daysToInactive)){
            final Long newValue = command.longValueOfParameterNamed(daysToInactiveParamName);
            actualChanges.put(daysToInactiveParamName, newValue);
            this.daysToInactive = newValue;
        }

        if(command.isChangeInLongParameterNamed(daysToDormancyParamName, this.daysToDormancy)){
            final Long newValue = command.longValueOfParameterNamed(daysToDormancyParamName);
            actualChanges.put(daysToDormancyParamName, newValue);
            this.daysToDormancy = newValue;
        }

        if(command.isChangeInLongParameterNamed(daysToEscheatParamName, this.daysToEscheat)){
            final Long newValue = command.longValueOfParameterNamed(daysToEscheatParamName);
            actualChanges.put(daysToEscheatParamName, newValue);
            this.daysToEscheat = newValue;
        }
        
        if(this.isDormancyTrackingActive == null || !this.isDormancyTrackingActive){
        	this.daysToInactive = null;
        	this.daysToDormancy = null;
        	this.daysToEscheat = null;
        }

        validateLockinDetails();
        esnureOverdraftLimitsSetForOverdraftAccounts();

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

    public BigDecimal nominalAnnualInterestRateOverdraft() {
        return this.nominalAnnualInterestRateOverdraft;
    }

    public BigDecimal minOverdraftForInterestCalculation() {
        return this.minOverdraftForInterestCalculation;
    }

    
    public TaxGroup getTaxGroup() {
        return this.taxGroup;
    }

    
    public void setTaxGroup(TaxGroup taxGroup) {
        this.taxGroup = taxGroup;
    }

    
    public boolean withHoldTax() {
        return this.withHoldTax;
    }

    public boolean isDormancyTrackingActive() {
		return null == this.isDormancyTrackingActive? false: this.isDormancyTrackingActive;
	}

	public Long getDaysToInactive() {
		return this.daysToInactive;
	}

	public Long getDaysToDormancy() {
		return this.daysToDormancy;
	}

	public Long getDaysToEscheat() {
		return this.daysToEscheat;
	}

}