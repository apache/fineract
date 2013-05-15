/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanproduct.data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.mifosplatform.accounting.glaccount.data.GLAccountData;
import org.mifosplatform.accounting.producttoaccountmapping.data.PaymentTypeToGLAccountMapper;
import org.mifosplatform.infrastructure.codes.data.CodeValueData;
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

    // terms
    private final CurrencyData currency;
    private final BigDecimal principal;
    private final BigDecimal minPrincipal;
    private final BigDecimal maxPrincipal;
    private final Integer numberOfRepayments;
    private final Integer minNumberOfRepayments;
    private final Integer maxNumberOfRepayments;
    private final Integer repaymentEvery;
    private final EnumOptionData repaymentFrequencyType;
    private final BigDecimal interestRatePerPeriod;
    private final BigDecimal minInterestRatePerPeriod;
    private final BigDecimal maxInterestRatePerPeriod;
    private final EnumOptionData interestRateFrequencyType;
    private final BigDecimal annualInterestRate;

    // settings
    private final EnumOptionData amortizationType;
    private final EnumOptionData interestType;
    private final EnumOptionData interestCalculationPeriodType;
    private final BigDecimal inArrearsTolerance;
    private final Long transactionProcessingStrategyId;
    private final String transactionProcessingStrategyName;

    // charges
    private final Collection<ChargeData> charges;

    // accounting
    private final EnumOptionData accountingRule;
    @SuppressWarnings("unused")
    private final Map<String, Object> accountingMappings;
    @SuppressWarnings("unused")
    private final Collection<PaymentTypeToGLAccountMapper> paymentChannelToFundSourceMappings;

    // template related
    private final Collection<FundData> fundOptions;
    private final Collection<CodeValueData> paymentTypeOptions;
    private final Collection<CurrencyData> currencyOptions;
    private final List<EnumOptionData> repaymentFrequencyTypeOptions;
    private final List<EnumOptionData> interestRateFrequencyTypeOptions;
    private final List<EnumOptionData> amortizationTypeOptions;
    private final List<EnumOptionData> interestTypeOptions;
    private final List<EnumOptionData> interestCalculationPeriodTypeOptions;
    private final Collection<TransactionProcessingStrategyData> transactionProcessingStrategyOptions;
    private final Collection<ChargeData> chargeOptions;
    private final List<EnumOptionData> accountingRuleOptions;
    private final Map<String, List<GLAccountData>> accountingMappingOptions;

    /**
     * Used when returning lookup information about loan product for dropdowns.
     */
    public static LoanProductData lookup(final Long id, final String name) {
        final String description = null;
        final CurrencyData currency = null;
        final BigDecimal principal = null;
        final BigDecimal minPrincipal = null;
        final BigDecimal maxPrincipal = null;
        final BigDecimal tolerance = null;
        final Integer numberOfRepayments = null;
        final Integer minNumberOfRepayments = null;
        final Integer maxNumberOfRepayments = null;
        final Integer repaymentEvery = null;
        final BigDecimal interestRatePerPeriod = null;
        final BigDecimal minInterestRatePerPeriod = null;
        final BigDecimal maxInterestRatePerPeriod = null;
        final BigDecimal annualInterestRate = null;
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

        return new LoanProductData(id, name, description, currency, principal, minPrincipal, maxPrincipal, tolerance, numberOfRepayments,
                minNumberOfRepayments, maxNumberOfRepayments, repaymentEvery, interestRatePerPeriod, minInterestRatePerPeriod,
                maxInterestRatePerPeriod, annualInterestRate, repaymentFrequencyType, interestRateFrequencyType, amortizationType,
                interestType, interestCalculationPeriodType, fundId, fundName, transactionProcessingStrategyId,
                transactionProcessingStrategyName, charges, accountingType);
    }

    public static LoanProductData lookupWithCurrency(final Long id, final String name, final CurrencyData currency) {
        final String description = null;
        final BigDecimal principal = null;
        final BigDecimal minPrincipal = null;
        final BigDecimal maxPrincipal = null;
        final BigDecimal tolerance = null;
        final Integer numberOfRepayments = null;
        final Integer minNumberOfRepayments = null;
        final Integer maxNumberOfRepayments = null;
        final Integer repaymentEvery = null;
        final BigDecimal interestRatePerPeriod = null;
        final BigDecimal minInterestRatePerPeriod = null;
        final BigDecimal maxInterestRatePerPeriod = null;
        final BigDecimal annualInterestRate = null;
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

        return new LoanProductData(id, name, description, currency, principal, minPrincipal, maxPrincipal, tolerance, numberOfRepayments,
                minNumberOfRepayments, maxNumberOfRepayments, repaymentEvery, interestRatePerPeriod, minInterestRatePerPeriod,
                maxInterestRatePerPeriod, annualInterestRate, repaymentFrequencyType, interestRateFrequencyType, amortizationType,
                interestType, interestCalculationPeriodType, fundId, fundName, transactionProcessingStrategyId,
                transactionProcessingStrategyName, charges, accountingType);
    }

    public static LoanProductData sensibleDefaultsForNewLoanProductCreation() {
        final Long id = null;
        final String name = null;
        final String description = null;
        final CurrencyData currency = null;
        final BigDecimal principal = null;
        final BigDecimal minPrincipal = null;
        final BigDecimal maxPrincipal = null;
        final BigDecimal tolerance = null;
        final Integer numberOfRepayments = null;
        final Integer minNumberOfRepayments = null;
        final Integer maxNumberOfRepayments = null;
        final Integer repaymentEvery = null;
        final BigDecimal interestRatePerPeriod = null;
        final BigDecimal minInterestRatePerPeriod = null;
        final BigDecimal maxInterestRatePerPeriod = null;
        final BigDecimal annualInterestRate = null;
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

        return new LoanProductData(id, name, description, currency, principal, minPrincipal, maxPrincipal, tolerance, numberOfRepayments,
                minNumberOfRepayments, maxNumberOfRepayments, repaymentEvery, interestRatePerPeriod, minInterestRatePerPeriod,
                maxInterestRatePerPeriod, annualInterestRate, repaymentFrequencyType, interestRateFrequencyType, amortizationType,
                interestType, interestCalculationPeriodType, fundId, fundName, transactionProcessingStrategyId,
                transactionProcessingStrategyName, charges, accountingType);
    }

    public static LoanProductData withAccountingDetails(final LoanProductData productData, final Map<String, Object> accountingMappings,
            final Collection<PaymentTypeToGLAccountMapper> paymentChannelToFundSourceMappings) {

        return new LoanProductData(productData, productData.chargeOptions, productData.paymentTypeOptions, productData.currencyOptions,
                productData.amortizationTypeOptions, productData.interestTypeOptions, productData.interestCalculationPeriodTypeOptions,
                productData.repaymentFrequencyTypeOptions, productData.interestRateFrequencyTypeOptions, productData.fundOptions,
                productData.transactionProcessingStrategyOptions, productData.accountingMappingOptions, productData.accountingRuleOptions,
                accountingMappings, paymentChannelToFundSourceMappings);
    }

    public LoanProductData(final Long id, final String name, final String description, final CurrencyData currency,
            final BigDecimal principal, final BigDecimal minPrincipal, final BigDecimal maxPrincipal, final BigDecimal tolerance,
            final Integer numberOfRepayments, final Integer minNumberOfRepayments, final Integer maxNumberOfRepayments,
            final Integer repaymentEvery, final BigDecimal interestRatePerPeriod, final BigDecimal minInterestRatePerPeriod,
            final BigDecimal maxInterestRatePerPeriod, final BigDecimal annualInterestRate, final EnumOptionData repaymentFrequencyType,
            final EnumOptionData interestRateFrequencyType, final EnumOptionData amortizationType, final EnumOptionData interestType,
            final EnumOptionData interestCalculationPeriodType, final Long fundId, final String fundName,
            final Long transactionProcessingStrategyId, final String transactionProcessingStrategyName,
            final Collection<ChargeData> charges, final EnumOptionData accountingType) {

        this.id = id;
        this.name = name;
        this.description = description;
        if (currency == null) {
            if (this.currencyOptions != null && this.currencyOptions.size() == 1) {
                this.currency = new ArrayList<CurrencyData>(this.currencyOptions).get(0);
            } else {
                this.currency = CurrencyData.blank();
            }
        } else {
            this.currency = currency;
        }
        this.principal = principal;
        this.minPrincipal = minPrincipal;
        this.maxPrincipal = maxPrincipal;
        this.inArrearsTolerance = tolerance;
        this.numberOfRepayments = numberOfRepayments;
        this.minNumberOfRepayments = minNumberOfRepayments;
        this.maxNumberOfRepayments = maxNumberOfRepayments;
        this.repaymentEvery = repaymentEvery;
        this.interestRatePerPeriod = interestRatePerPeriod;
        this.minInterestRatePerPeriod = minInterestRatePerPeriod;
        this.maxInterestRatePerPeriod = maxInterestRatePerPeriod;
        this.annualInterestRate = annualInterestRate;
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
        this.paymentTypeOptions = null;
        this.currencyOptions = null;
        this.fundOptions = null;
        this.transactionProcessingStrategyOptions = null;
        this.amortizationTypeOptions = null;
        this.interestTypeOptions = null;
        this.interestCalculationPeriodTypeOptions = null;
        this.repaymentFrequencyTypeOptions = null;
        this.interestRateFrequencyTypeOptions = null;

        this.accountingMappingOptions = null;
        this.accountingRuleOptions = null;
        this.accountingMappings = null;
        this.paymentChannelToFundSourceMappings = null;
    }

    public LoanProductData(final LoanProductData productData, final Collection<ChargeData> chargeOptions,
            final Collection<CodeValueData> paymentTypeOptions, final Collection<CurrencyData> currencyOptions,
            final List<EnumOptionData> amortizationTypeOptions, final List<EnumOptionData> interestTypeOptions,
            final List<EnumOptionData> interestCalculationPeriodTypeOptions, final List<EnumOptionData> repaymentFrequencyTypeOptions,
            final List<EnumOptionData> interestRateFrequencyTypeOptions, final Collection<FundData> fundOptions,
            final Collection<TransactionProcessingStrategyData> transactionStrategyOptions,
            final Map<String, List<GLAccountData>> accountingMappingOptions, final List<EnumOptionData> accountingRuleOptions,
            final Map<String, Object> accountingMappings, final Collection<PaymentTypeToGLAccountMapper> paymentChannelToFundSourceMappings) {
        this.id = productData.id;
        this.name = productData.name;
        this.description = productData.description;
        this.fundId = productData.fundId;
        this.fundName = productData.fundName;

        this.principal = productData.principal;
        this.minPrincipal = productData.minPrincipal;
        this.maxPrincipal = productData.maxPrincipal;
        this.inArrearsTolerance = productData.inArrearsTolerance;
        this.numberOfRepayments = productData.numberOfRepayments;
        this.minNumberOfRepayments = productData.minNumberOfRepayments;
        this.maxNumberOfRepayments = productData.maxNumberOfRepayments;
        this.repaymentEvery = productData.repaymentEvery;
        this.interestRatePerPeriod = productData.interestRatePerPeriod;
        this.minInterestRatePerPeriod = productData.minInterestRatePerPeriod;
        this.maxInterestRatePerPeriod = productData.maxInterestRatePerPeriod;
        this.annualInterestRate = productData.annualInterestRate;
        this.repaymentFrequencyType = productData.repaymentFrequencyType;
        this.interestRateFrequencyType = productData.interestRateFrequencyType;
        this.amortizationType = productData.amortizationType;
        this.interestType = productData.interestType;
        this.interestCalculationPeriodType = productData.interestCalculationPeriodType;

        this.charges = nullIfEmpty(productData.charges());
        this.accountingRule = productData.accountingRule;

        this.chargeOptions = chargeOptions;
        this.paymentTypeOptions = paymentTypeOptions;
        this.currencyOptions = currencyOptions;
        this.currency = productData.currency;
        this.fundOptions = fundOptions;
        this.transactionProcessingStrategyOptions = transactionStrategyOptions;
        if (this.transactionProcessingStrategyOptions != null && this.transactionProcessingStrategyOptions.size() == 1) {
            final List<TransactionProcessingStrategyData> listOfOptions = new ArrayList<TransactionProcessingStrategyData>(
                    this.transactionProcessingStrategyOptions);

            this.transactionProcessingStrategyId = listOfOptions.get(0).id();
            this.transactionProcessingStrategyName = listOfOptions.get(0).name();
        } else {
            this.transactionProcessingStrategyId = productData.transactionProcessingStrategyId;
            this.transactionProcessingStrategyName = productData.transactionProcessingStrategyName;
        }

        this.amortizationTypeOptions = amortizationTypeOptions;
        this.interestTypeOptions = interestTypeOptions;
        this.interestCalculationPeriodTypeOptions = interestCalculationPeriodTypeOptions;
        this.repaymentFrequencyTypeOptions = repaymentFrequencyTypeOptions;
        this.interestRateFrequencyTypeOptions = interestRateFrequencyTypeOptions;

        this.accountingMappingOptions = accountingMappingOptions;
        this.accountingRuleOptions = accountingRuleOptions;

        if (accountingMappings == null || accountingMappings.isEmpty()) {
            this.accountingMappings = null;
        } else {
            this.accountingMappings = accountingMappings;
        }

        this.paymentChannelToFundSourceMappings = paymentChannelToFundSourceMappings;
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

    public BigDecimal getMinPrincipal() {
        return this.minPrincipal;
    }

    public BigDecimal getMaxPrincipal() {
        return this.maxPrincipal;
    }

    public BigDecimal getInArrearsTolerance() {
        return inArrearsTolerance;
    }

    public Integer getNumberOfRepayments() {
        return numberOfRepayments;
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

    public Collection<ChargeData> getChargeOptions() {
        return chargeOptions;
    }

    @Override
    public boolean equals(final Object obj) {
        LoanProductData loanProductData = (LoanProductData) obj;
        return loanProductData.id.equals(this.id);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
}