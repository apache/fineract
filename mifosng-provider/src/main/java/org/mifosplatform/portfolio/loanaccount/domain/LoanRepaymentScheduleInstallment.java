package org.mifosplatform.portfolio.loanaccount.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.useradministration.domain.AppUser;

@Entity
@Table(name = "m_loan_repayment_schedule")
public final class LoanRepaymentScheduleInstallment extends AbstractAuditableCustom<AppUser, Long> {

    @ManyToOne(optional = false)
    @JoinColumn(name = "loan_id")
    private Loan    loan;

    @Column(name = "installment", nullable=false)
    private final Integer installmentNumber;
    
    @Temporal(TemporalType.DATE)
    @Column(name = "fromdate", nullable=true)
    private final Date    fromDate;
    
    @Temporal(TemporalType.DATE)
    @Column(name = "duedate", nullable=false)
    private final Date    dueDate;
    
    @Column(name = "principal_amount", scale=6, precision=19, nullable=true)
    private BigDecimal  principal;
    
    @Column(name = "principal_completed_derived", scale=6, precision=19, nullable=true)
    private BigDecimal   principalCompleted;
    
    @Column(name = "principal_writtenoff_derived", scale=6, precision=19, nullable=true)
    private BigDecimal   principalWrittenOff;
    
    @Column(name = "interest_amount", scale=6, precision=19, nullable=true)
    private BigDecimal   interest;
    
	@Column(name = "interest_completed_derived", scale = 6, precision = 19, nullable=true)
	private BigDecimal interestCompleted;
	
	@Column(name = "interest_waived_derived", scale = 6, precision = 19, nullable=true)
	private BigDecimal interestWaived;
	
	@Column(name = "interest_writtenoff_derived", scale=6, precision=19, nullable=true)
	private BigDecimal   interestWrittenOff;
	
	@Column(name = "fee_charges_amount", scale=6, precision=19, nullable=true)
    private BigDecimal   feeCharges;
    
    @Column(name = "fee_charges_completed_derived", scale=6, precision=19, nullable=true)
    private BigDecimal   feeChargesCompleted;
    
    @Column(name = "fee_charges_writtenoff_derived", scale=6, precision=19, nullable=true)
    private BigDecimal   feeChargesWrittenOff;
    
    @Column(name = "fee_charges_waived_derived", scale=6, precision=19, nullable=true)
    private BigDecimal   feeChargesWaived;
    
    @Column(name = "penalty_charges_amount", scale=6, precision=19, nullable=true)
    private BigDecimal   penaltyCharges;
    
    @Column(name = "penalty_charges_completed_derived", scale=6, precision=19, nullable=true)
    private BigDecimal   penaltyChargesCompleted;
    
    @Column(name = "penalty_charges_writtenoff_derived", scale=6, precision=19, nullable=true)
    private BigDecimal   penaltyChargesWrittenOff;
    
    @Column(name = "penalty_charges_waived_derived", scale=6, precision=19, nullable=true)
    private BigDecimal   penaltyChargesWaived;
    
    @Column(name="completed_derived", nullable=false)
    private boolean completed;

    protected LoanRepaymentScheduleInstallment() {
    	this.installmentNumber = null;
    	this.fromDate = null;
    	this.dueDate = null;
        this.completed = false;
    }

    public LoanRepaymentScheduleInstallment(
    		final Loan loan, 
    		final Integer installmentNumber,
    		final LocalDate fromDate, 
            final LocalDate dueDate, 
            final BigDecimal principal, 
            final BigDecimal interest, 
            final BigDecimal feeCharges,
            final BigDecimal penaltyCharges) {
        this.loan = loan;
        this.installmentNumber = installmentNumber;
        this.fromDate = fromDate.toDateMidnight().toDate();
        this.dueDate = dueDate.toDateMidnight().toDate();
        this.principal = defaultToNullIfZero(principal);
        this.interest = defaultToNullIfZero(interest);
        this.feeCharges = defaultToNullIfZero(feeCharges);
        this.penaltyCharges = defaultToNullIfZero(penaltyCharges);
        this.completed = false;
    }

	private BigDecimal defaultToNullIfZero(final BigDecimal value) {
		BigDecimal result = value;
		if (BigDecimal.ZERO.compareTo(value) == 0) {
			result = null;
		}
		return result;
	}

	public Loan getLoan() {
        return this.loan;
    }

    public Integer getInstallmentNumber() {
        return this.installmentNumber;
    }

    public LocalDate getFromDate() {
    	LocalDate fromLocalDate = null;
    	if (this.fromDate != null) {
    		fromLocalDate = new LocalDate(this.fromDate);
    	}
    	
    	return fromLocalDate;
    }
    
    public LocalDate getDueDate() {
        return new LocalDate(this.dueDate);
    }

	public Money getPrincipal(final MonetaryCurrency currency) {
		return Money.of(currency, this.principal);
	}
	
	public Money getPrincipalCompleted(final MonetaryCurrency currency) {
		return Money.of(currency, this.principalCompleted);
	}
	
	public Money getPrincipalWrittenOff(final MonetaryCurrency currency) {
		return Money.of(currency, this.principalWrittenOff);
	}
	
	public Money getPrincipalOutstanding(final MonetaryCurrency currency) {
		final Money principalAccountedFor = getPrincipalCompleted(currency).plus(getPrincipalWrittenOff(currency));
		return getPrincipal(currency).minus(principalAccountedFor);
	}
	
	public Money getInterest(final MonetaryCurrency currency) {
		return Money.of(currency, this.interest);
	}
	
	public Money getInterestCompleted(final MonetaryCurrency currency) {
		return Money.of(currency, this.interestCompleted);
	}
	
	public Money getInterestWaived(final MonetaryCurrency currency) {
		return Money.of(currency, this.interestWaived);
	}
	
	public Money getInterestWrittenOff(final MonetaryCurrency currency) {
		return Money.of(currency, this.interestWrittenOff);
	}
	
	public Money getInterestOutstanding(final MonetaryCurrency currency) {
		final Money interestAccountedFor = getInterestCompleted(currency).plus(getInterestWaived(currency)).plus(getInterestWrittenOff(currency));
		return getInterest(currency).minus(interestAccountedFor);
	}
	
	public Money getFeeCharges(final MonetaryCurrency currency) {
		return Money.of(currency, this.feeCharges);
	}
	
	public Money getFeeChargesCompleted(final MonetaryCurrency currency) {
		return Money.of(currency, this.feeChargesCompleted);
	}
	
	public Money getFeeChargesWaived(final MonetaryCurrency currency) {
		return Money.of(currency, this.feeChargesWaived);
	}
	
	public Money getFeeChargesWrittenOff(final MonetaryCurrency currency) {
		return Money.of(currency, this.feeChargesWrittenOff);
	}
	
	public Money getFeeChargesOutstanding(final MonetaryCurrency currency) {
		final Money feeChargesAccountedFor = getFeeChargesCompleted(currency).plus(getFeeChargesWaived(currency)).plus(getFeeChargesWrittenOff(currency));
		return getFeeCharges(currency).minus(feeChargesAccountedFor);
	}
	
	public Money getPenaltyCharges(final MonetaryCurrency currency) {
		return Money.of(currency, this.penaltyCharges);
	}
	
	public Money getPenaltyChargesCompleted(final MonetaryCurrency currency) {
		return Money.of(currency, this.penaltyChargesCompleted);
	}
	
	public Money getPenaltyChargesWaived(final MonetaryCurrency currency) {
		return Money.of(currency, this.penaltyChargesWaived);
	}
	
	public Money getPenaltyChargesWrittenOff(final MonetaryCurrency currency) {
		return Money.of(currency, this.penaltyChargesWrittenOff);
	}
	
	public Money getPenaltyChargesOutstanding(final MonetaryCurrency currency) {
		final Money feeChargesAccountedFor = getPenaltyChargesCompleted(currency).plus(getPenaltyChargesWaived(currency)).plus(getPenaltyChargesWrittenOff(currency));
		return getPenaltyCharges(currency).minus(feeChargesAccountedFor);
	}

	public boolean isInterestDue(final MonetaryCurrency currency) {
		return getInterestOutstanding(currency).isGreaterThanZero();
	}
	
	public Money getTotalPrincipalAndInterest(final MonetaryCurrency currency) {
		return getPrincipal(currency).plus(getInterest(currency));
	}
	
	public Money getTotalOutstanding(final MonetaryCurrency currency) {
		return getPrincipalOutstanding(currency).plus(getInterestOutstanding(currency)).plus(getFeeChargesOutstanding(currency)).plus(getPenaltyChargesOutstanding(currency));
	}
	
	public void updateLoan(final Loan loan) {
		this.loan = loan;
	}
	
	public boolean isFullyCompleted() {
		return this.completed;
	}

	public boolean isNotFullyCompleted() {
		return !this.completed;
	}
	
	public boolean isPrincipalNotCompleted(final MonetaryCurrency currency) {
		return !isPrincipalCompleted(currency);
	}
	
	public boolean isPrincipalCompleted(final MonetaryCurrency currency) {
		return getPrincipalOutstanding(currency).isZero();
	}

	public void resetDerivedComponents() {
		this.principalCompleted = null;
		this.principalWrittenOff = null;
		this.interestCompleted = null;
		this.interestWaived = null;
		this.interestWrittenOff = null;
		this.feeChargesCompleted = null;
		this.feeChargesWaived = null;
		this.feeChargesWrittenOff = null;
		this.penaltyChargesCompleted = null;
		this.penaltyChargesWaived = null;
		this.penaltyChargesWrittenOff = null;
		
		this.completed = false;
	}

	public Money payPenaltyChargesComponent(final Money transactionAmountRemaining) {
		
		final MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
		Money penaltyPortionOfTransaction = Money.zero(currency);
		
		final Money penaltyChargesDue = getPenaltyChargesOutstanding(currency);
		if (transactionAmountRemaining.isGreaterThanOrEqualTo(penaltyChargesDue)) {
			this.penaltyChargesCompleted = getPenaltyChargesCompleted(currency).plus(penaltyChargesDue).getAmount();
			penaltyPortionOfTransaction = penaltyPortionOfTransaction.plus(penaltyChargesDue);
		} else {
			this.penaltyChargesCompleted = getPenaltyChargesCompleted(currency).plus(transactionAmountRemaining).getAmount();
			penaltyPortionOfTransaction = penaltyPortionOfTransaction.plus(transactionAmountRemaining);
		}
		
		this.penaltyChargesCompleted = defaultToNullIfZero(this.penaltyChargesCompleted);
		
		this.completed = getTotalOutstanding(currency).isZero();
		
		return penaltyPortionOfTransaction;
	}
	
	public Money payFeeChargesComponent(final Money transactionAmountRemaining) {
		
		final MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
		Money feePortionOfTransaction = Money.zero(currency);
		
		final Money feeChargesDue = getFeeChargesOutstanding(currency);
		if (transactionAmountRemaining.isGreaterThanOrEqualTo(feeChargesDue)) {
			this.feeChargesCompleted = getFeeChargesCompleted(currency).plus(feeChargesDue).getAmount();
			feePortionOfTransaction = feePortionOfTransaction.plus(feeChargesDue);
		} else {
			this.feeChargesCompleted = getFeeChargesCompleted(currency).plus(transactionAmountRemaining).getAmount();
			feePortionOfTransaction = feePortionOfTransaction.plus(transactionAmountRemaining);
		}
		
		this.feeChargesCompleted = defaultToNullIfZero(this.feeChargesCompleted);
		
		this.completed = getTotalOutstanding(currency).isZero();
		
		return feePortionOfTransaction;
	}
	
	public Money payInterestComponent(final Money transactionAmountRemaining) {
		
		final MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
		Money interestPortionOfTransaction = Money.zero(currency);
		
		final Money interestDue = getInterestOutstanding(currency);
		if (transactionAmountRemaining.isGreaterThanOrEqualTo(interestDue)) {
			this.interestCompleted = getInterestCompleted(currency).plus(interestDue).getAmount();
			interestPortionOfTransaction = interestPortionOfTransaction.plus(interestDue);
		} else {
			this.interestCompleted = getInterestCompleted(currency).plus(transactionAmountRemaining).getAmount();
			interestPortionOfTransaction = interestPortionOfTransaction.plus(transactionAmountRemaining);
		}
		
		this.interestCompleted = defaultToNullIfZero(this.interestCompleted);
		
		this.completed = getTotalOutstanding(currency).isZero();
		
		return interestPortionOfTransaction;
	}

	public Money payPrincipalComponent(final Money transactionAmountRemaining) {
		
		final MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
		Money principalPortionOfTransaction = Money.zero(currency);
		
		final Money principalDue = getPrincipalOutstanding(currency);
		if (transactionAmountRemaining.isGreaterThanOrEqualTo(principalDue)) {
			this.principalCompleted = getPrincipalCompleted(currency).plus(principalDue).getAmount();
			principalPortionOfTransaction = principalPortionOfTransaction.plus(principalDue);
		} else {
			this.principalCompleted = getPrincipalCompleted(currency).plus(transactionAmountRemaining).getAmount();
			principalPortionOfTransaction = principalPortionOfTransaction.plus(transactionAmountRemaining);
		}
		
		this.principalCompleted = defaultToNullIfZero(this.principalCompleted);
		
		this.completed = getTotalOutstanding(currency).isZero();
		
		return principalPortionOfTransaction;
	}

	public Money waiveInterestComponent(final Money transactionAmountRemaining) {
		MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
		Money waivedInterestPortionOfTransaction = Money.zero(currency);
		
		Money interestDue = getInterestOutstanding(currency);
		if (transactionAmountRemaining.isGreaterThanOrEqualTo(interestDue)) {
			this.interestWaived = getInterestWaived(currency).plus(interestDue).getAmount();
			waivedInterestPortionOfTransaction = waivedInterestPortionOfTransaction.plus(interestDue);
		} else {
			this.interestWaived = getInterestWaived(currency).plus(transactionAmountRemaining).getAmount();
			waivedInterestPortionOfTransaction = waivedInterestPortionOfTransaction.plus(transactionAmountRemaining);
		}
		
		this.interestWaived = defaultToNullIfZero(this.interestWaived);
		
		this.completed = getTotalOutstanding(currency).isZero();
		
		return waivedInterestPortionOfTransaction;
	}
	
	public Money waivePenaltyChargesComponent(final Money transactionAmountRemaining) {
		MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
		Money waivedPenaltyChargesPortionOfTransaction = Money.zero(currency);
		
		Money penanltiesDue = getPenaltyChargesOutstanding(currency);
		if (transactionAmountRemaining.isGreaterThanOrEqualTo(penanltiesDue)) {
			this.penaltyChargesWaived = getPenaltyChargesWaived(currency).plus(penanltiesDue).getAmount();
			waivedPenaltyChargesPortionOfTransaction = waivedPenaltyChargesPortionOfTransaction.plus(penanltiesDue);
		} else {
			this.penaltyChargesWaived = getPenaltyChargesWaived(currency).plus(transactionAmountRemaining).getAmount();
			waivedPenaltyChargesPortionOfTransaction = waivedPenaltyChargesPortionOfTransaction.plus(transactionAmountRemaining);
		}
		
		this.penaltyChargesWaived = defaultToNullIfZero(this.penaltyChargesWaived);
		
		this.completed = getTotalOutstanding(currency).isZero();
		
		return waivedPenaltyChargesPortionOfTransaction;
	}
	
	public Money waiveFeeChargesComponent(final Money transactionAmountRemaining) {
		MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
		Money waivedFeeChargesPortionOfTransaction = Money.zero(currency);
		
		Money feesDue = getPenaltyChargesOutstanding(currency);
		if (transactionAmountRemaining.isGreaterThanOrEqualTo(feesDue)) {
			this.feeChargesWaived = getFeeChargesWaived(currency).plus(feesDue).getAmount();
			waivedFeeChargesPortionOfTransaction = waivedFeeChargesPortionOfTransaction.plus(feesDue);
		} else {
			this.feeChargesWaived = getFeeChargesWaived(currency).plus(transactionAmountRemaining).getAmount();
			waivedFeeChargesPortionOfTransaction = waivedFeeChargesPortionOfTransaction.plus(transactionAmountRemaining);
		}
		
		this.feeChargesWaived = defaultToNullIfZero(this.feeChargesWaived);
		
		this.completed = getTotalOutstanding(currency).isZero();
		
		return waivedFeeChargesPortionOfTransaction;
	}
	
	public Money writeOffOutstandingPrincipal(final MonetaryCurrency currency) {
		
		final Money principalDue = getPrincipalOutstanding(currency);
		this.principalWrittenOff = defaultToNullIfZero(principalDue.getAmount());
		this.completed = getTotalOutstanding(currency).isZero();
		
		return principalDue;
	}
	
	public Money writeOffOutstandingInterest(final MonetaryCurrency currency) {
		
		final Money interestDue = getInterestOutstanding(currency);
		this.interestWrittenOff = defaultToNullIfZero(interestDue.getAmount());
		this.completed = getTotalOutstanding(currency).isZero();
		
		return interestDue;
	}

	public boolean isOverdueOn(final LocalDate transactionDate) {
		return this.getDueDate().isBefore(transactionDate);
	}

	public void updateChargePortion(
			final Money feeChargesDue,
			final Money feeChargesWaived,
			final Money feeChargesWrittenOff,
			final Money penaltyChargesDue,
			final Money penaltyChargesWaived,
			final Money penaltyChargesWrittenOff) {
		this.feeCharges = defaultToNullIfZero(feeChargesDue.getAmount());
		this.feeChargesWaived = defaultToNullIfZero(feeChargesWaived.getAmount());
		this.feeChargesWrittenOff = defaultToNullIfZero(feeChargesWrittenOff.getAmount());
		this.penaltyCharges = defaultToNullIfZero(penaltyChargesDue.getAmount());
		this.penaltyChargesWaived = defaultToNullIfZero(penaltyChargesWaived.getAmount());
		this.penaltyChargesWrittenOff = defaultToNullIfZero(penaltyChargesWrittenOff.getAmount());
	}
}