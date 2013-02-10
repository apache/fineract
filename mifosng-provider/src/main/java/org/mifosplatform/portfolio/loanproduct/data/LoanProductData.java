package org.mifosplatform.portfolio.loanproduct.data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.mifosplatform.accounting.api.data.GLAccountData;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.portfolio.charge.data.ChargeData;
import org.mifosplatform.portfolio.fund.data.FundData;
import org.mifosplatform.portfolio.loanproduct.domain.AccountingRuleType;
import org.mifosplatform.portfolio.loanproduct.domain.AmortizationMethod;
import org.mifosplatform.portfolio.loanproduct.domain.InterestCalculationPeriodMethod;
import org.mifosplatform.portfolio.loanproduct.domain.InterestMethod;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.loanproduct.service.LoanEnumerations;

/**
 * Immutable data object to represent loan products.
 */
public class LoanProductData {

    private final Long id;
    private final String name;
    private final String description;
    private final Long fundId;
    private final String fundName;
    private final CurrencyData currency;
    private final BigDecimal principal;
    private final Integer numberOfRepayments;
    private final Integer loanTermFrequency;
    private final Integer repaymentEvery;
    private final EnumOptionData loanTermFrequencyType;
    private final EnumOptionData repaymentFrequencyType;
    private final Long transactionProcessingStrategyId;
    private final String transactionProcessingStrategyName;
    private final BigDecimal interestRatePerPeriod;
    private final EnumOptionData interestRateFrequencyType;
    private final BigDecimal annualInterestRate;
    private final EnumOptionData amortizationType;
    private final EnumOptionData interestType;
    private final EnumOptionData interestCalculationPeriodType;
    private final BigDecimal inArrearsTolerance;

    private final EnumOptionData accountingRule;

    private final Collection<ChargeData> charges;

    // template related
    private final Collection<CurrencyData> currencyOptions;
    private final Collection<FundData> fundOptions;
    private final Collection<TransactionProcessingStrategyData> transactionProcessingStrategyOptions;
    private final List<EnumOptionData> amortizationTypeOptions;
    private final List<EnumOptionData> interestTypeOptions;
    private final List<EnumOptionData> interestCalculationPeriodTypeOptions;
    private final List<EnumOptionData> repaymentFrequencyTypeOptions;
    private final List<EnumOptionData> interestRateFrequencyTypeOptions;
    private final List<EnumOptionData> loanTermFrequencyTypeOptions;
    private final Collection<ChargeData> chargeOptions;

    // accounting related template fields
    private final List<EnumOptionData> accountingRuleOptions;
    private final Map<String, List<GLAccountData>> accountingMappingOptions;

    // accounting related mapping fields
    @SuppressWarnings("unused")
    private final Map<String, Object> accountingMappings;

    /**
     * Used when returning lookup information about loan product for dropdowns.
     */
    public static LoanProductData lookup(final Long id, final String name) {
        final String description = null;
        final CurrencyData currency = null;
        final BigDecimal principal = null;
        final BigDecimal tolerance = null;
        final Integer numberOfRepayments = null;
        final Integer loanTermFrequency = null;
        final Integer repaymentEvery = null;
        final BigDecimal interestRatePerPeriod = null;
        final BigDecimal annualInterestRate = null;
        final EnumOptionData loanTermFrequencyType = null;
        final EnumOptionData repaymentFrequencyType = null;
        final EnumOptionData interestRateFrequencyType = null;
        final EnumOptionData amortizationType = null;
        final EnumOptionData interestType = null;
        final EnumOptionData interestCalculationPeriodType = null;
        final Long fundId = null;
        final String fundName = null;
        final Long transactionProcessingStrategyId = null;
        final String transactionProcessingStrategyName = null;
        final Collection<ChargeData> charges = null;
        final EnumOptionData accountingType = null;

        return new LoanProductData(id, name, description, currency, principal, tolerance, numberOfRepayments, loanTermFrequency,
                repaymentEvery, interestRatePerPeriod, annualInterestRate, loanTermFrequencyType, repaymentFrequencyType,
                interestRateFrequencyType, amortizationType, interestType, interestCalculationPeriodType, fundId, fundName,
                transactionProcessingStrategyId, transactionProcessingStrategyName, charges, accountingType);
    }

    public static LoanProductData sensibleDefaultsForNewLoanProductCreation() {
        final Long id = null;
        final String name = null;
        final String description = null;
        final CurrencyData currency = CurrencyData.blank();
        final BigDecimal principal = null;
        final BigDecimal tolerance = null;
        final Integer numberOfRepayments = null;
        final Integer loanTermFrequency = null;
        final Integer repaymentEvery = null;
        final BigDecimal interestRatePerPeriod = null;
        final BigDecimal annualInterestRate = null;
        final EnumOptionData loanTermFrequencyType = LoanEnumerations.loanTermFrequencyType(PeriodFrequencyType.MONTHS);
        final EnumOptionData repaymentFrequencyType = LoanEnumerations.repaymentFrequencyType(PeriodFrequencyType.MONTHS);
        final EnumOptionData interestRateFrequencyType = LoanEnumerations.interestRateFrequencyType(PeriodFrequencyType.MONTHS);
        final EnumOptionData amortizationType = LoanEnumerations.amortizationType(AmortizationMethod.EQUAL_INSTALLMENTS);
        final EnumOptionData interestType = LoanEnumerations.interestType(InterestMethod.DECLINING_BALANCE);
        final EnumOptionData interestCalculationPeriodType = LoanEnumerations
                .interestCalculationPeriodType(InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD);
        final Long fundId = null;
        final String fundName = null;
        final Long transactionProcessingStrategyId = null;
        final String transactionProcessingStrategyName = null;
        final Collection<ChargeData> charges = null;
        final EnumOptionData accountingType = LoanEnumerations.accountingRuleType(AccountingRuleType.NONE);

        return new LoanProductData(id, name, description, currency, principal, tolerance, numberOfRepayments, loanTermFrequency,
                repaymentEvery, interestRatePerPeriod, annualInterestRate, loanTermFrequencyType, repaymentFrequencyType,
                interestRateFrequencyType, amortizationType, interestType, interestCalculationPeriodType, fundId, fundName,
                transactionProcessingStrategyId, transactionProcessingStrategyName, charges, accountingType);
    }

    public static LoanProductData withAccountingMappings(final LoanProductData productData, final Map<String, Object> accountingMappings) {

        return new LoanProductData(productData, productData.chargeOptions, productData.currencyOptions,
                productData.amortizationTypeOptions, productData.interestTypeOptions, productData.interestCalculationPeriodTypeOptions,
                productData.loanTermFrequencyTypeOptions, productData.repaymentFrequencyTypeOptions,
                productData.interestRateFrequencyTypeOptions, productData.fundOptions, productData.transactionProcessingStrategyOptions,
                productData.accountingMappingOptions, productData.accountingRuleOptions, accountingMappings);
    }

    public LoanProductData(final Long id, final String name, final String description, final CurrencyData currency,
            final BigDecimal principal, final BigDecimal tolerance, final Integer numberOfRepayments, final Integer loanTermFrequency,
            final Integer repaymentEvery, final BigDecimal interestRatePerPeriod, final BigDecimal annualInterestRate,
            final EnumOptionData loanTermFrequencyType, final EnumOptionData repaymentFrequencyType,
            final EnumOptionData interestRateFrequencyType, final EnumOptionData amortizationType, final EnumOptionData interestType,
            final EnumOptionData interestCalculationPeriodType, final Long fundId, final String fundName,
            final Long transactionProcessingStrategyId, final String transactionProcessingStrategyName,
            final Collection<ChargeData> charges, final EnumOptionData accountingType) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.currency = currency;
        this.principal = principal;
        this.inArrearsTolerance = tolerance;
        this.numberOfRepayments = numberOfRepayments;
        this.loanTermFrequency = loanTermFrequency;
        this.repaymentEvery = repaymentEvery;
        this.interestRatePerPeriod = interestRatePerPeriod;
        this.annualInterestRate = annualInterestRate;
        this.loanTermFrequencyType = loanTermFrequencyType;
        this.repaymentFrequencyType = repaymentFrequencyType;
        this.interestRateFrequencyType = interestRateFrequencyType;
        this.amortizationType = amortizationType;
        this.interestType = interestType;
        this.interestCalculationPeriodType = interestCalculationPeriodType;
        this.fundId = fundId;
        this.fundName = fundName;
        this.transactionProcessingStrategyId = transactionProcessingStrategyId;
        this.transactionProcessingStrategyName = transactionProcessingStrategyName;
        this.charges = charges;
        this.accountingRule = accountingType;

        this.chargeOptions = null;
        this.currencyOptions = null;
        this.fundOptions = null;
        this.transactionProcessingStrategyOptions = null;
        this.amortizationTypeOptions = null;
        this.interestTypeOptions = null;
        this.interestCalculationPeriodTypeOptions = null;
        this.loanTermFrequencyTypeOptions = null;
        this.repaymentFrequencyTypeOptions = null;
        this.interestRateFrequencyTypeOptions = null;

        this.accountingMappingOptions = null;
        this.accountingRuleOptions = null;
        this.accountingMappings = null;
    }

    public LoanProductData(final LoanProductData productData, final Collection<ChargeData> chargeOptions,
            final Collection<CurrencyData> currencyOptions, final List<EnumOptionData> amortizationTypeOptions,
            final List<EnumOptionData> interestTypeOptions, final List<EnumOptionData> interestCalculationPeriodTypeOptions,
            final List<EnumOptionData> loanTermFrequencyTypeOptions, final List<EnumOptionData> repaymentFrequencyTypeOptions,
            final List<EnumOptionData> interestRateFrequencyTypeOptions, final Collection<FundData> fundOptions,
            final Collection<TransactionProcessingStrategyData> transactionProcessingStrategyOptions,
            final Map<String, List<GLAccountData>> accountingMappingOptions, final List<EnumOptionData> accountingRuleOptions,
            final Map<String, Object> accountingMappings) {
        this.id = productData.id;
        this.name = productData.name;
        this.description = productData.description;
        this.principal = productData.principal;
        this.inArrearsTolerance = productData.inArrearsTolerance;
        this.numberOfRepayments = productData.numberOfRepayments;
        this.loanTermFrequency = productData.loanTermFrequency;
        this.repaymentEvery = productData.repaymentEvery;
        this.interestRatePerPeriod = productData.interestRatePerPeriod;
        this.annualInterestRate = productData.annualInterestRate;
        this.loanTermFrequencyType = productData.loanTermFrequencyType;
        this.repaymentFrequencyType = productData.repaymentFrequencyType;
        this.interestRateFrequencyType = productData.interestRateFrequencyType;
        this.amortizationType = productData.amortizationType;
        this.interestType = productData.interestType;
        this.interestCalculationPeriodType = productData.interestCalculationPeriodType;
        this.fundId = productData.fundId;
        this.fundName = productData.fundName;
        this.transactionProcessingStrategyId = productData.transactionProcessingStrategyId;
        this.transactionProcessingStrategyName = productData.transactionProcessingStrategyName;
        this.charges = nullIfEmpty(productData.charges());
        this.accountingRule = productData.accountingRule;

        this.chargeOptions = chargeOptions;
        this.currencyOptions = currencyOptions;
        if (this.currencyOptions != null && this.currencyOptions.size() == 1) {
            this.currency = new ArrayList<CurrencyData>(this.currencyOptions).get(0);
        } else {
            this.currency = productData.currency;
        }
        this.fundOptions = fundOptions;
        this.transactionProcessingStrategyOptions = transactionProcessingStrategyOptions;
        this.amortizationTypeOptions = amortizationTypeOptions;
        this.interestTypeOptions = interestTypeOptions;
        this.interestCalculationPeriodTypeOptions = interestCalculationPeriodTypeOptions;
        this.loanTermFrequencyTypeOptions = loanTermFrequencyTypeOptions;
        this.repaymentFrequencyTypeOptions = repaymentFrequencyTypeOptions;
        this.interestRateFrequencyTypeOptions = interestRateFrequencyTypeOptions;

        this.accountingMappingOptions = accountingMappingOptions;
        this.accountingRuleOptions = accountingRuleOptions;
        this.accountingMappings = accountingMappings;
    }

    private Collection<ChargeData> nullIfEmpty(final Collection<ChargeData> charges) {
        Collection<ChargeData> chargesLocal = charges;
        if (charges == null || charges.isEmpty()) {
            chargesLocal = null;
        }
        return chargesLocal;
    }

    public Collection<ChargeData> charges() {
        Collection<ChargeData> chargesLocal = new ArrayList<ChargeData>();
        if (this.charges != null) {
            chargesLocal = this.charges;
        }
        return chargesLocal;
    }

    public EnumOptionData accountingRuleType() {
        return this.accountingRule;
    }

    public boolean hasAccountingEnabled() {
        return this.accountingRule.getId() > 1;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Long getFundId() {
        return fundId;
    }

    public String getFundName() {
        return fundName;
    }

    public Long getTransactionProcessingStrategyId() {
        return transactionProcessingStrategyId;
    }

    public String getTransactionProcessingStrategyName() {
        return transactionProcessingStrategyName;
    }

    public CurrencyData getCurrency() {
        return currency;
    }

    public BigDecimal getPrincipal() {
        return principal;
    }

    public BigDecimal getInArrearsTolerance() {
        return inArrearsTolerance;
    }

    public Integer getNumberOfRepayments() {
        return numberOfRepayments;
    }

    public Integer getLoanTermFrequency() {
        return loanTermFrequency;
    }

    public Integer getRepaymentEvery() {
        return repaymentEvery;
    }

    public BigDecimal getInterestRatePerPeriod() {
        return interestRatePerPeriod;
    }

    public BigDecimal getAnnualInterestRate() {
        return annualInterestRate;
    }

    public EnumOptionData getLoanTermFrequencyType() {
        return loanTermFrequencyType;
    }

    public EnumOptionData getRepaymentFrequencyType() {
        return repaymentFrequencyType;
    }

    public EnumOptionData getInterestRateFrequencyType() {
        return interestRateFrequencyType;
    }

    public EnumOptionData getAmortizationType() {
        return amortizationType;
    }

    public EnumOptionData getInterestType() {
        return interestType;
    }

    public EnumOptionData getInterestCalculationPeriodType() {
        return interestCalculationPeriodType;
    }

    public Collection<FundData> getFundOptions() {
        return fundOptions;
    }

    public Collection<TransactionProcessingStrategyData> getTransactionProcessingStrategyOptions() {
        return transactionProcessingStrategyOptions;
    }

    public List<EnumOptionData> getAmortizationTypeOptions() {
        return amortizationTypeOptions;
    }

    public List<EnumOptionData> getInterestTypeOptions() {
        return interestTypeOptions;
    }

    public List<EnumOptionData> getInterestCalculationPeriodTypeOptions() {
        return interestCalculationPeriodTypeOptions;
    }

    public List<EnumOptionData> getRepaymentFrequencyTypeOptions() {
        return repaymentFrequencyTypeOptions;
    }

    public List<EnumOptionData> getInterestRateFrequencyTypeOptions() {
        return interestRateFrequencyTypeOptions;
    }

    public List<EnumOptionData> getLoanTermFrequencyTypeOptions() {
        return loanTermFrequencyTypeOptions;
    }

    public Collection<ChargeData> getChargeOptions() {
        return chargeOptions;
    }
}