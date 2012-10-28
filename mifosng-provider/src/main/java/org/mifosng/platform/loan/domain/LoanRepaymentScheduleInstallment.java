package org.mifosng.platform.loan.domain;

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
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.currency.domain.Money;
import org.mifosng.platform.infrastructure.AbstractAuditableCustom;
import org.mifosng.platform.user.domain.AppUser;

@Entity
@Table(name = "m_loan_repayment_schedule")
public final class LoanRepaymentScheduleInstallment extends AbstractAuditableCustom<AppUser, Long> {

    @ManyToOne(optional = false)
    @JoinColumn(name = "loan_id")
    private Loan    loan;

    @Column(name = "installment")
    private final Integer installmentNumber;
    
    @Column(name = "principal_amount", scale=6, precision=19)
    private BigDecimal  principal;
    
    @Column(name = "interest_amount", scale=6, precision=19)
    private BigDecimal   interest;
    
    @Column(name = "fee_charges_amount", scale=6, precision=19)
    private BigDecimal   feeCharges;
    
    @Column(name = "fee_charges_completed_derived", scale=6, precision=19)
    private BigDecimal   feeChargesCompleted;
    
    @Column(name = "fee_charges_writtenoff_derived", scale=6, precision=19)
    private BigDecimal   feeChargesWrittenOff;
    
    @Column(name = "fee_charges_waived_derived", scale=6, precision=19)
    private BigDecimal   feeChargesWaived;
    
    @Column(name = "penalty_charges_amount", scale=6, precision=19)
    private BigDecimal   penaltyCharges;
    
    @Column(name = "penalty_charges_completed_derived", scale=6, precision=19)
    private BigDecimal   penaltyChargesCompleted;
    
    @Column(name = "penalty_charges_writtenoff_derived", scale=6, precision=19)
    private BigDecimal   penaltyChargesWrittenOff;
    
    @Column(name = "penalty_charges_waived_derived", scale=6, precision=19)
    private BigDecimal   penaltyChargesWaived;
    
    @Column(name = "principal_completed_derived", scale=6, precision=19)
    private BigDecimal   principalCompleted;
    
    @Column(name = "principal_writtenoff_derived", scale=6, precision=19)
    private BigDecimal   principalWrittenOff;
    
	@Column(name = "interest_completed_derived", scale = 6, precision = 19)
	private BigDecimal interestCompleted;
	
	@Column(name = "interest_waived_derived", scale = 6, precision = 19)
	private BigDecimal interestWaived;
	
	@Column(name = "interest_writtenoff_derived", scale=6, precision=19)
	private BigDecimal   interestWrittenOff;
    
    @Column(name="completed_derived")
    private boolean completed;

    @Temporal(TemporalType.DATE)
    @Column(name = "duedate")
    private final Date    dueDate;

    protected LoanRepaymentScheduleInstallment() {
        this.loan = null;
        this.installmentNumber = null;
        this.dueDate = null;
        this.principal = null;
        this.interest = null;
        this.principalCompleted = null;
        this.interestCompleted = null;
        this.completed = false;
    }

    public LoanRepaymentScheduleInstallment(
    		final Loan loan, 
    		final Integer installmentNumber,
            final LocalDate dueDate, 
            final BigDecimal principal, 
            final BigDecimal interest, 
            final BigDecimal feeCharges) {
        this.loan = loan;
        this.installmentNumber = installmentNumber;
        this.dueDate = dueDate.toDateMidnight().toDate();
        this.principal = defaultToNullIfZero(principal);
        this.interest = defaultToNullIfZero(interest);
        this.feeCharges = defaultToNullIfZero(feeCharges);
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

	public boolean isInterestDue(final MonetaryCurrency currency) {
		return getInterestOutstanding(currency).isGreaterThanZero();
	}
	
	public Money getTotalPrincipalAndInterest(final MonetaryCurrency currency) {
		return getPrincipal(currency).plus(getInterest(currency));
	}
	
	public Money getTotalOutstanding(final MonetaryCurrency currency) {
		return getPrincipalOutstanding(currency).plus(getInterestOutstanding(currency));
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
		this.principalCompleted = BigDecimal.ZERO;
		this.principalWrittenOff = BigDecimal.ZERO;
		this.interestCompleted = BigDecimal.ZERO;
		this.interestWaived = BigDecimal.ZERO;
		this.interestWrittenOff = BigDecimal.ZERO;
		this.completed = false;
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
		
		this.completed = getTotalOutstanding(currency).isZero();
		
		return waivedInterestPortionOfTransaction;
	}
	
	public Money writeOffOutstandingPrincipal(final MonetaryCurrency currency) {
		
		final Money principalDue = getPrincipalOutstanding(currency);
		this.principalWrittenOff = principalDue.getAmount();
		this.completed = getTotalOutstanding(currency).isZero();
		
		return principalDue;
	}
	
	public Money writeOffOutstandingInterest(final MonetaryCurrency currency) {
		
		final Money interestDue = getInterestOutstanding(currency);
		this.interestWrittenOff = interestDue.getAmount();
		this.completed = getTotalOutstanding(currency).isZero();
		
		return interestDue;
	}

	public boolean isOverdueOn(final LocalDate transactionDate) {
		return this.getDueDate().isBefore(transactionDate);
	}

	public void updateChargePortion(final Money feeChargesDue) {
		this.feeCharges = defaultToNullIfZero(feeChargesDue.getAmount());
	}
}