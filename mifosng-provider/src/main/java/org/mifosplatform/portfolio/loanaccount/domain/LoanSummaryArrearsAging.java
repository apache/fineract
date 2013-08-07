/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.springframework.data.domain.Persistable;

/**
 * <p>
 * A specialist view of arrears & aging summary details of a {@link Loan}.
 * <p>
 * 
 * <p>
 * This is intentionally split from the loan summary concept and maps to its own
 * table that contains only arrears and aging information. The reason is to make
 * any overnight batch update of these details simpler and improve performance.
 * </p>
 */
@Entity
@Table(name = "m_loan_arrears_aging")
@org.hibernate.annotations.GenericGenerator(name = "loan-primarykey", strategy = "foreign", parameters = { //
@org.hibernate.annotations.Parameter(name = "property", value = "loan") //
})
public final class LoanSummaryArrearsAging implements Persistable<Long> {

    @Id
    @GeneratedValue(generator = "loan-primarykey")
    @Column(name = "loan_id", unique = true, nullable = false)
    private Long loanId;

    @Column(name = "principal_overdue_derived", scale = 6, precision = 19)
    private BigDecimal totalPrincipalOverdue;

    @Column(name = "interest_overdue_derived", scale = 6, precision = 19)
    private BigDecimal totalInterestOverdue;

    @Column(name = "fee_charges_overdue_derived", scale = 6, precision = 19)
    private BigDecimal totalFeeChargesOverdue;

    @Column(name = "penalty_charges_overdue_derived", scale = 6, precision = 19)
    private BigDecimal totalPenaltyChargesOverdue;

    @Column(name = "total_overdue_derived", scale = 6, precision = 19)
    private BigDecimal totalOverdue;

    @Temporal(TemporalType.DATE)
    @Column(name = "overdue_since_date_derived")
    private Date overdueSinceDate;

    @OneToOne(optional = false)
    @PrimaryKeyJoinColumn
    private Loan loan;

    protected LoanSummaryArrearsAging() {
        //
    }

    public LoanSummaryArrearsAging(final Loan loan) {
        this.loan = loan;
        zeroFields();
    }

    @Override
    public Long getId() {
        return this.loanId;
    }

    @Override
    public boolean isNew() {
        return null == getId();
    }

    public void zeroFields() {
        this.totalPrincipalOverdue = BigDecimal.ZERO;
        this.totalInterestOverdue = BigDecimal.ZERO;
        this.totalFeeChargesOverdue = BigDecimal.ZERO;
        this.totalPenaltyChargesOverdue = BigDecimal.ZERO;
        this.totalOverdue = BigDecimal.ZERO;
        this.overdueSinceDate = null;
    }

    public void updateSummary(final MonetaryCurrency currency, final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
            final LoanSummaryWrapper summaryWrapper) {

        this.totalPrincipalOverdue = summaryWrapper.calculateTotalPrincipalOverdueOn(repaymentScheduleInstallments, currency,
                DateUtils.getLocalDateOfTenant()).getAmount();
        this.totalInterestOverdue = summaryWrapper.calculateTotalInterestOverdueOn(repaymentScheduleInstallments, currency,
                DateUtils.getLocalDateOfTenant()).getAmount();
        this.totalFeeChargesOverdue = summaryWrapper.calculateTotalFeeChargesOverdueOn(repaymentScheduleInstallments, currency,
                DateUtils.getLocalDateOfTenant()).getAmount();
        this.totalPenaltyChargesOverdue = summaryWrapper.calculateTotalPenaltyChargesOverdueOn(repaymentScheduleInstallments, currency,
                DateUtils.getLocalDateOfTenant()).getAmount();

        final Money totalOverdue = Money.of(currency, this.totalPrincipalOverdue).plus(this.totalInterestOverdue)
                .plus(this.totalFeeChargesOverdue).plus(this.totalPenaltyChargesOverdue);
        this.totalOverdue = totalOverdue.getAmount();

        final LocalDate overdueSinceLocalDate = summaryWrapper.determineOverdueSinceDateFrom(repaymentScheduleInstallments, currency,
                DateUtils.getLocalDateOfTenant());
        if (overdueSinceLocalDate != null) {
            this.overdueSinceDate = overdueSinceLocalDate.toDate();
        } else {
            this.overdueSinceDate = null;
        }
    }

    public boolean isNotInArrears(final MonetaryCurrency currency) {
        return Money.of(currency, this.totalOverdue).isZero();
    }
}