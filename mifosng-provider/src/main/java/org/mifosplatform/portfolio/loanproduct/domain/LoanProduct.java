package org.mifosplatform.portfolio.loanproduct.domain;

import java.math.BigDecimal;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.mifosng.platform.infrastructure.AbstractAuditableCustom;
import org.mifosplatform.infrastructure.configuration.domain.MonetaryCurrency;
import org.mifosplatform.infrastructure.user.domain.AppUser;
import org.mifosplatform.portfolio.charge.domain.Charge;
import org.mifosplatform.portfolio.fund.domain.Fund;
import org.mifosplatform.portfolio.loanproduct.command.LoanProductCommand;

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
@Table(name = "m_product_loan")
public class LoanProduct extends AbstractAuditableCustom<AppUser, Long> {

    @SuppressWarnings("unused")
    @ManyToOne
    @JoinColumn(name = "fund_id", nullable = true)
    private Fund fund;

    @SuppressWarnings("unused")
    @ManyToOne
    @JoinColumn(name = "loan_transaction_strategy_id", nullable = true)
    private LoanTransactionProcessingStrategy transactionProcessingStrategy;

    @SuppressWarnings("unused")
    @Column(name = "name", nullable = false)
    private String name;

    @SuppressWarnings("unused")
    @Column(name = "description")
    private String description;

    @ManyToMany
    @JoinTable(name = "m_product_loan_charge", joinColumns = @JoinColumn(name = "product_loan_id"), inverseJoinColumns = @JoinColumn(name = "charge_id"))
    private Set<Charge> charges;

    @Embedded
    private final LoanProductRelatedDetail loanProductRelatedDetail;

    protected LoanProduct() {
        this.loanProductRelatedDetail = null;
    }

    public LoanProduct(final Fund fund, final LoanTransactionProcessingStrategy transactionProcessingStrategy, final String name,
            final String description, final MonetaryCurrency currency, final BigDecimal defaultPrincipal,
            final BigDecimal defaultNominalInterestRatePerPeriod, final PeriodFrequencyType interestPeriodFrequencyType,
            final BigDecimal defaultAnnualNominalInterestRate, final InterestMethod interestMethod,
            final InterestCalculationPeriodMethod interestCalculationPeriodMethod, final Integer repayEvery,
            final PeriodFrequencyType repaymentFrequencyType, final Integer defaultNumberOfInstallments,
            final AmortizationMethod amortizationMethod, final BigDecimal inArrearsTolerance, final Set<Charge> charges) {
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
    }

    public MonetaryCurrency getCurrency() {
        return this.loanProductRelatedDetail.getCurrency();
    }

    public Set<Charge> getCharges() {
        return charges;
    }

    public void update(final LoanProductCommand command, final Fund fund, final LoanTransactionProcessingStrategy strategy,
            final Set<Charge> charges) {

        if (command.isNameChanged()) {
            this.name = command.getName();
        }

        if (command.isDescriptionChanged()) {
            this.description = command.getDescription();
        }

        if (command.isFundChanged()) {
            this.fund = fund;
        }

        if (command.isTransactionProcessingStrategyChanged()) {
            this.transactionProcessingStrategy = strategy;
        }

        if (command.isChargesChanged()) {
            this.charges = charges;
        }

        this.loanProductRelatedDetail.update(command);
    }
}