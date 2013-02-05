package org.mifosplatform.portfolio.loanproduct.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
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
import org.mifosplatform.portfolio.charge.domain.Charge;
import org.mifosplatform.portfolio.fund.domain.Fund;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.AprCalculator;
import org.springframework.data.jpa.domain.AbstractPersistable;

/**
 * Loan products allow for categorisation of an organisations loans into
 * something meaningful to them.
 * 
 * They provide a means of simplifying creation/maintenance of loans. They can
 * also allow for product comparison to take place when reporting.
 * 
 * They allow for constraints to be added at product level.
 */
@Entity
@Table(name = "m_product_loan" , uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }, name = "unq_name")})
public class LoanProduct extends AbstractPersistable<Long> {

    @ManyToOne
    @JoinColumn(name = "fund_id", nullable = true)
    private Fund fund;

    @ManyToOne
    @JoinColumn(name = "loan_transaction_strategy_id", nullable = true)
    private LoanTransactionProcessingStrategy transactionProcessingStrategy;

    @Column(name = "name", nullable = false , unique = true)
    private String name;

    @Column(name = "description")
    private String description;

    @ManyToMany
    @JoinTable(name = "m_product_loan_charge", joinColumns = @JoinColumn(name = "product_loan_id"), inverseJoinColumns = @JoinColumn(name = "charge_id"))
    private Set<Charge> charges;

    @Embedded
    private final LoanProductRelatedDetail loanProductRelatedDetail;

    @Column(name = "accounting_type", nullable = false)
    private Integer accountingType;

    public static LoanProduct assembleFromJson(final Fund fund, final LoanTransactionProcessingStrategy loanTransactionProcessingStrategy,
            final Set<Charge> productCharges, final JsonCommand command, final AprCalculator aprCalculator) {

        final String name = command.stringValueOfParameterNamed("name");
        final String description = command.stringValueOfParameterNamed("description");
        final String currencyCode = command.stringValueOfParameterNamed("currencyCode");
        final Integer digitsAfterDecimal = command.integerValueOfParameterNamed("digitsAfterDecimal");

        final MonetaryCurrency currency = new MonetaryCurrency(currencyCode, digitsAfterDecimal);
        final BigDecimal principal = command.bigDecimalValueOfParameterNamed("principal");

        final InterestMethod interestMethod = InterestMethod.fromInt(command.integerValueOfParameterNamed("interestType"));
        final InterestCalculationPeriodMethod interestCalculationPeriodMethod = InterestCalculationPeriodMethod.fromInt(command
                .integerValueOfParameterNamed("interestCalculationPeriodType"));
        final AmortizationMethod amortizationMethod = AmortizationMethod.fromInt(command.integerValueOfParameterNamed("amortizationType"));
        final PeriodFrequencyType repaymentFrequencyType = PeriodFrequencyType.fromInt(command
                .integerValueOfParameterNamed("repaymentFrequencyType"));
        final PeriodFrequencyType interestFrequencyType = PeriodFrequencyType.fromInt(command
                .integerValueOfParameterNamed("interestRateFrequencyType"));
        final BigDecimal interestRatePerPeriod = command.bigDecimalValueOfParameterNamed("interestRatePerPeriod");
        final BigDecimal annualInterestRate = aprCalculator.calculateFrom(interestFrequencyType, interestRatePerPeriod);

        final Integer repaymentEvery = command.integerValueOfParameterNamed("repaymentEvery");
        final Integer numberOfRepayments = command.integerValueOfParameterNamed("numberOfRepayments");
        final BigDecimal inArrearsTolerance = command.bigDecimalValueOfParameterNamed("inArrearsTolerance");
        final AccountingRuleType accountingRuleType = AccountingRuleType.fromInt(command.integerValueOfParameterNamed("accountingType"));

        return new LoanProduct(fund, loanTransactionProcessingStrategy, name, description, currency, principal, interestRatePerPeriod,
                interestFrequencyType, annualInterestRate, interestMethod, interestCalculationPeriodMethod, repaymentEvery,
                repaymentFrequencyType, numberOfRepayments, amortizationMethod, inArrearsTolerance, productCharges, accountingRuleType);
    }

    protected LoanProduct() {
        this.loanProductRelatedDetail = null;
    }

    public LoanProduct(final Fund fund, final LoanTransactionProcessingStrategy transactionProcessingStrategy, final String name,
            final String description, final MonetaryCurrency currency, final BigDecimal defaultPrincipal,
            final BigDecimal defaultNominalInterestRatePerPeriod, final PeriodFrequencyType interestPeriodFrequencyType,
            final BigDecimal defaultAnnualNominalInterestRate, final InterestMethod interestMethod,
            final InterestCalculationPeriodMethod interestCalculationPeriodMethod, final Integer repayEvery,
            final PeriodFrequencyType repaymentFrequencyType, final Integer defaultNumberOfInstallments,
            final AmortizationMethod amortizationMethod, final BigDecimal inArrearsTolerance, final Set<Charge> charges,
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

        if (accountingRuleType != null) {
            this.accountingType = accountingRuleType.getValue();
        }
    }

    public MonetaryCurrency getCurrency() {
        return this.loanProductRelatedDetail.getCurrency();
    }

    public Set<Charge> getCharges() {
        return charges;
    }

    public void update(final Fund fund) {
        this.fund = fund;
    }

    public void update(final LoanTransactionProcessingStrategy strategy) {
        this.transactionProcessingStrategy = strategy;
    }

    public void update(final Set<Charge> charges) {
        this.charges = charges;
    }

    public Integer getAccountingType() {
        return this.accountingType;
    }

    public Map<String, Object> update(final JsonCommand command, final AprCalculator aprCalculator) {

        final Map<String, Object> actualChanges = this.loanProductRelatedDetail.update(command, aprCalculator);

        final String accountingTypeParamName = "accountingType";
        if (command.isChangeInIntegerParameterNamed(accountingTypeParamName, this.accountingType)) {
            final Integer newValue = command.integerValueOfParameterNamed(accountingTypeParamName);
            actualChanges.put(accountingTypeParamName, newValue);
            this.accountingType = newValue;
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
        if (command.isChangeInArrayParameterNamed(chargesParamName, getChargesIds())) {
            final String[] newValue = command.arrayValueOfParameterNamed(chargesParamName);
            actualChanges.put(chargesParamName, newValue);
        }

        return actualChanges;
    }

    private String[] getChargesIds() {
        final List<String> chargeIds = new ArrayList<String>();

        if (this.charges != null) {
            for (Charge charge : this.charges) {
                chargeIds.add(charge.getId().toString());
            }
        }

        return chargeIds.toArray(new String[chargeIds.size()]);
    }

    public boolean isAccountingEnabled() {
        return !AccountingRuleType.NONE.getValue().equals(this.accountingType);
    }

    public boolean isCashBasedAccountingEnabled() {
        return AccountingRuleType.CASH_BASED.getValue().equals(this.accountingType);
    }

    public boolean isAccrualBasedAccountingEnabled() {
        return AccountingRuleType.ACCRUAL_BASED.getValue().equals(this.accountingType);
    }
}