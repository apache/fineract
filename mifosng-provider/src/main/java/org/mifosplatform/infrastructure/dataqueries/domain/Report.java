/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.dataqueries.domain;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.charge.domain.Charge;
import org.mifosplatform.portfolio.fund.domain.Fund;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.AprCalculator;
import org.springframework.data.jpa.domain.AbstractPersistable;

import com.google.gson.JsonArray;
/*
@Entity
@Table(name = "stretchy_report", uniqueConstraints = { @UniqueConstraint(columnNames = { "report_name" }, name = "unq_report_name") })
public class Report extends AbstractPersistable<Long> {

@Column(name = "report_name", nullable = false, unique = true)
private String reportName;

@Column(name = "report_type", nullable = false)
private String reportType;

@Column(name = "report_subtype")
private String reportSubType;

@Column(name = "report_category")
private String reportCategory;

@Column(name = "description")
private String description;

@Column(name = "core_report", nullable = false)
private boolean core_Report;

//only defines if report should appear in reference app UI List
@Column(name = "use_report", nullable = false)
private boolean useReport;

@Column(name = "report_sql")
private String reportSql;


    @ManyToMany
    @JoinTable(name = "stretchy_report_parameter", joinColumns = @JoinColumn(name = "product_loan_id"), inverseJoinColumns = @JoinColumn(name = "charge_id"))
    private List<Charge> charges;


    public static Report assembleFromJson(final Fund fund, final LoanTransactionProcessingStrategy loanTransactionProcessingStrategy,
            final List<Charge> productCharges, final JsonCommand command, final AprCalculator aprCalculator) {

        final String name = command.stringValueOfParameterNamed("name");
        final String description = command.stringValueOfParameterNamed("description");
        final String currencyCode = command.stringValueOfParameterNamed("currencyCode");
        final Integer digitsAfterDecimal = command.integerValueOfParameterNamed("digitsAfterDecimal");

        final MonetaryCurrency currency = new MonetaryCurrency(currencyCode, digitsAfterDecimal);
        final BigDecimal principal = command.bigDecimalValueOfParameterNamed("principal");
        final BigDecimal minPrincipal = command.bigDecimalValueOfParameterNamed("minPrincipal");
        final BigDecimal maxPrincipal = command.bigDecimalValueOfParameterNamed("maxPrincipal");

        final InterestMethod interestMethod = InterestMethod.fromInt(command.integerValueOfParameterNamed("interestType"));
        final InterestCalculationPeriodMethod interestCalculationPeriodMethod = InterestCalculationPeriodMethod.fromInt(command
                .integerValueOfParameterNamed("interestCalculationPeriodType"));
        final AmortizationMethod amortizationMethod = AmortizationMethod.fromInt(command.integerValueOfParameterNamed("amortizationType"));
        final PeriodFrequencyType repaymentFrequencyType = PeriodFrequencyType.fromInt(command
                .integerValueOfParameterNamed("repaymentFrequencyType"));
        final PeriodFrequencyType interestFrequencyType = PeriodFrequencyType.fromInt(command
                .integerValueOfParameterNamed("interestRateFrequencyType"));
        final BigDecimal interestRatePerPeriod = command.bigDecimalValueOfParameterNamed("interestRatePerPeriod");
        final BigDecimal minInterestRatePerPeriod = command.bigDecimalValueOfParameterNamed("minInterestRatePerPeriod");
        final BigDecimal maxInterestRatePerPeriod = command.bigDecimalValueOfParameterNamed("maxInterestRatePerPeriod");
        final BigDecimal annualInterestRate = aprCalculator.calculateFrom(interestFrequencyType, interestRatePerPeriod);

        final Integer repaymentEvery = command.integerValueOfParameterNamed("repaymentEvery");
        final Integer numberOfRepayments = command.integerValueOfParameterNamed("numberOfRepayments");
        final Integer minNumberOfRepayments = command.integerValueOfParameterNamed("minNumberOfRepayments");
        final Integer maxNumberOfRepayments = command.integerValueOfParameterNamed("maxNumberOfRepayments");
        final BigDecimal inArrearsTolerance = command.bigDecimalValueOfParameterNamed("inArrearsTolerance");
        final AccountingRuleType accountingRuleType = AccountingRuleType.fromInt(command.integerValueOfParameterNamed("accountingRule"));

        return new Report(fund, loanTransactionProcessingStrategy, name, description, currency, principal, minPrincipal, maxPrincipal,
                interestRatePerPeriod, minInterestRatePerPeriod, maxInterestRatePerPeriod, interestFrequencyType, annualInterestRate, interestMethod, interestCalculationPeriodMethod,
                repaymentEvery, repaymentFrequencyType, numberOfRepayments, minNumberOfRepayments, maxNumberOfRepayments, amortizationMethod, inArrearsTolerance, productCharges,
                accountingRuleType);
    }

    protected Report() {
        this.loanProductRelatedDetail = null;
        this.loanProductMinMaxConstraints = null;
    }

    public Report(final Fund fund, final LoanTransactionProcessingStrategy transactionProcessingStrategy, final String name,
            final String description, final MonetaryCurrency currency, final BigDecimal defaultPrincipal,
            final BigDecimal defaultMinPrincipal, final BigDecimal defaultMaxPrincipal,
            final BigDecimal defaultNominalInterestRatePerPeriod, final BigDecimal defaultMinNominalInterestRatePerPeriod,
            final BigDecimal defaultMaxNominalInterestRatePerPeriod, final PeriodFrequencyType interestPeriodFrequencyType,
            final BigDecimal defaultAnnualNominalInterestRate, final InterestMethod interestMethod,
            final InterestCalculationPeriodMethod interestCalculationPeriodMethod, final Integer repayEvery,
            final PeriodFrequencyType repaymentFrequencyType, final Integer defaultNumberOfInstallments,
            final Integer defaultMinNumberOfInstallments, final Integer defaultMaxNumberOfInstallments,
            final AmortizationMethod amortizationMethod, final BigDecimal inArrearsTolerance, final List<Charge> charges,
            final AccountingRuleType accountingRuleType) {
        this.fund = fund;
        this.transactionProcessingStrategy = transactionProcessingStrategy;
        this.name = name.trim();
        if (StringUtils.isNotBlank(description)) {
            this.description = description.trim();
        } else {
            this.description = null;
        }

        if (charges != null) {
            this.charges = charges;
        }

        this.loanProductRelatedDetail = new LoanProductRelatedDetail(currency, defaultPrincipal, defaultNominalInterestRatePerPeriod,
                interestPeriodFrequencyType, defaultAnnualNominalInterestRate, interestMethod, interestCalculationPeriodMethod, repayEvery,
                repaymentFrequencyType, defaultNumberOfInstallments, amortizationMethod, inArrearsTolerance);

        this.loanProductMinMaxConstraints = new LoanProductMinMaxConstraints(defaultMinPrincipal, defaultMaxPrincipal,
                defaultMinNominalInterestRatePerPeriod, defaultMaxNominalInterestRatePerPeriod, defaultMinNumberOfInstallments,
                defaultMaxNumberOfInstallments);
        
        if (accountingRuleType != null) {
            this.accountingRule = accountingRuleType.getValue();
        }
    }

    public MonetaryCurrency getCurrency() {
        return this.loanProductRelatedDetail.getCurrency();
    }

    public boolean update(final List<Charge> newProductCharges) {
        boolean updated = false;
        if (this.charges != null) {
            final Set<Charge> setOfCharges = new HashSet<Charge>(this.charges);

            updated = setOfCharges.addAll(newProductCharges);
            if (updated) {
                this.charges = newProductCharges;
            }
        }
        return updated;
    }

    public Integer getAccountingType() {
        return this.accountingRule;
    }

    public Map<String, Object> update(final JsonCommand command, final AprCalculator aprCalculator) {

        final Map<String, Object> actualChanges = this.loanProductRelatedDetail.update(command, aprCalculator);
        actualChanges.putAll(this.loanProductMinMaxConstraints().update(command));

        final String accountingTypeParamName = "accountingRule";
        if (command.isChangeInIntegerParameterNamed(accountingTypeParamName, this.accountingRule)) {
            final Integer newValue = command.integerValueOfParameterNamed(accountingTypeParamName);
            actualChanges.put(accountingTypeParamName, newValue);
            this.accountingRule = newValue;
        }

        final String nameParamName = "name";
        if (command.isChangeInStringParameterNamed(nameParamName, this.name)) {
            final String newValue = command.stringValueOfParameterNamed(nameParamName);
            actualChanges.put(nameParamName, newValue);
            this.name = newValue;
        }

        final String descriptionParamName = "description";
        if (command.isChangeInStringParameterNamed(descriptionParamName, this.description)) {
            final String newValue = command.stringValueOfParameterNamed(descriptionParamName);
            actualChanges.put(descriptionParamName, newValue);
            this.description = newValue;
        }

        Long existingFundId = null;
        if (this.fund != null) {
            existingFundId = this.fund.getId();
        }
        final String fundIdParamName = "fundId";
        if (command.isChangeInLongParameterNamed(fundIdParamName, existingFundId)) {
            final Long newValue = command.longValueOfParameterNamed(fundIdParamName);
            actualChanges.put(fundIdParamName, newValue);
        }

        Long existingStrategyId = null;
        if (this.transactionProcessingStrategy != null) {
            existingStrategyId = this.transactionProcessingStrategy.getId();
        }
        final String transactionProcessingStrategyParamName = "transactionProcessingStrategyId";
        if (command.isChangeInLongParameterNamed(transactionProcessingStrategyParamName, existingStrategyId)) {
            final Long newValue = command.longValueOfParameterNamed(transactionProcessingStrategyParamName);
            actualChanges.put(transactionProcessingStrategyParamName, newValue);
        }

        final String chargesParamName = "charges";
        if (command.hasParameter(chargesParamName)) {
            JsonArray jsonArray = command.arrayOfParameterNamed(chargesParamName);
            if (jsonArray != null) {

            }
            actualChanges.put(chargesParamName, command.jsonFragment(chargesParamName));
        }

        return actualChanges;
    }

    public boolean isCashBasedAccountingEnabled() {
        return AccountingRuleType.CASH_BASED.getValue().equals(this.accountingRule);
    }

    public boolean isAccrualBasedAccountingEnabled() {
        return AccountingRuleType.ACCRUAL_BASED.getValue().equals(this.accountingRule);
    }
    
    public Money getPrincipalAmount(){
        return this.loanProductRelatedDetail.getPrincipal();
    }
    
    public Money getMinPrincipalAmount(){
        return Money.of(this.loanProductRelatedDetail.getCurrency(), this.loanProductMinMaxConstraints().getMinPrincipal());
    }
    
    public Money getMaxPrincipalAmount(){
        return Money.of(this.loanProductRelatedDetail.getCurrency(), this.loanProductMinMaxConstraints().getMaxPrincipal());
    }
    
    public BigDecimal getNominalInterestRatePerPeriod() {
        return this.loanProductRelatedDetail.getNominalInterestRatePerPeriod();
    }
    
    public BigDecimal getMinNominalInterestRatePerPeriod() {
        return this.loanProductMinMaxConstraints().getMinNominalInterestRatePerPeriod();
    }
    
    public BigDecimal getMaxNominalInterestRatePerPeriod(){
        return this.loanProductMinMaxConstraints().getMaxNominalInterestRatePerPeriod();
    }
    
    public Integer getNumberOfRepayments(){
        return this.loanProductRelatedDetail().getNumberOfRepayments();
    }
    
    public Integer getMinNumberOfRepayments() {
        return this.loanProductMinMaxConstraints().getMinNumberOfRepayments();
    }

    public Integer getMaxNumberOfRepayments() {
        return this.loanProductMinMaxConstraints().getMaxNumberOfRepayments();
    }
    
    public LoanProductRelatedDetail loanProductRelatedDetail(){
        return this.loanProductRelatedDetail;
    }
    
    public LoanProductMinMaxConstraints loanProductMinMaxConstraints() {
        //If all min and max fields are null then loanProductMinMaxConstraints initialising to null
        //Reset LoanProductMinMaxConstraints with null values.
        this.loanProductMinMaxConstraints = (this.loanProductMinMaxConstraints == null) ? new LoanProductMinMaxConstraints(null, null, null, null, null, null)
                : this.loanProductMinMaxConstraints;
        return loanProductMinMaxConstraints;
    }
}
*/