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

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
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
public final class LoanSummaryArrearsAging implements Persistable<Long> {

    @Id
    @GeneratedValue(generator = "SharedPrimaryKeyGenerator")
    @GenericGenerator(name = "SharedPrimaryKeyGenerator", strategy = "foreign", parameters = @Parameter(name = "property", value = "loan"))
    @Column(name = "loan_id", unique = true, nullable = false)
    private Long id;

    @Column(name = "principal_overdue_derived", scale = 6, precision = 19)
    private BigDecimal totalPrincipalOverdue;

    @Column(name = "interest_overdue_derived", scale = 6, precision = 19)
    private BigDecimal totalInterestOverdue;

    @Column(name = "fee_charges_overdue_derived", scale = 6, precision = 19)
    private BigDecimal totalFeeChargesOverdue;

    @Column(name = "penalty_charges_overdue_derived", scale = 6, precision = 19)
    private BigDecimal totalPenaltyChargesOverdue;

    @SuppressWarnings("unused")
    @Column(name = "total_overdue_derived", scale = 6, precision = 19)
    private BigDecimal totalOverdue;

    @SuppressWarnings("unused")
    @Temporal(TemporalType.DATE)
    @Column(name = "overdue_since_date_derived")
    private Date overdueSinceDate;

    @OneToOne
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
        return this.id;
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
}