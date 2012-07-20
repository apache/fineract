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
@Table(name = "portfolio_loan_repayment_schedule")
public class LoanRepaymentScheduleInstallment extends AbstractAuditableCustom<AppUser, Long> {

    @ManyToOne(optional = false)
    @JoinColumn(name = "loan_id")
    private Loan    loan;

    @Column(name = "installment")
    private final Integer installmentNumber;
    
    @Column(name = "principal_amount", scale=6, precision=19)
    private BigDecimal   principal;
    
    @Column(name = "interest_amount", scale=6, precision=19)
    private BigDecimal   interest;
    
    @Column(name = "principal_completed_derived", scale=6, precision=19)
    private BigDecimal   principalCompleted;
    
	@Column(name = "interest_completed_derived", scale = 6, precision = 19)
	private BigDecimal interestCompleted;
    
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

    public LoanRepaymentScheduleInstallment(final Loan loan, final Integer installmentNumber,
            final LocalDate dueDate, final BigDecimal principal, final BigDecimal interest) {
        this.loan = loan;
        this.installmentNumber = installmentNumber;
        this.dueDate = dueDate.toDateMidnight().toDate();
        this.principal = principal;
        this.principalCompleted = BigDecimal.ZERO;
        this.interest = interest;
        this.interestCompleted = BigDecimal.ZERO;
        this.completed = false;
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

	public Money updateDerivedComponents(Money totalAvailable) {
		
		Money remaining = totalAvailable.copy();
		
		boolean principalCompletedInFull = false;
		boolean interestCompletedInFull = false;
		
		Money interest = getInterest(totalAvailable.getCurrency());
		Money principal = getPrincipal(totalAvailable.getCurrency());
		
		// TODO - NEWFEATURE - configuration around order components of loan are paid off in.
		// pay off interest
		if (remaining.isGreaterThanOrEqualTo(interest)) {
			this.interestCompleted = interest.getAmount();
			interestCompletedInFull = true;
		} else {
			this.interestCompleted = remaining.getAmount();
		}
		remaining = remaining.minus(this.interestCompleted);
		
		// pay off principal
		if (remaining.isGreaterThanOrEqualTo(principal)) {
			this.principalCompleted = principal.getAmount();
			principalCompletedInFull = true;
		} else {
			this.principalCompleted = remaining.getAmount();
		}
		remaining = remaining.minus(this.principalCompleted);
		
		this.completed = (principalCompletedInFull && interestCompletedInFull);
		
		return remaining;
	}

	public Money getPrincipal(MonetaryCurrency currency) {
		return Money.of(currency, this.principal);
	}
	
	public Money getPrincipalCompleted(MonetaryCurrency currency) {
		return Money.of(currency, this.principalCompleted);
	}
	
	public Money getInterest(MonetaryCurrency currency) {
		return Money.of(currency, this.interest);
	}
	
	public Money getInterestCompleted(MonetaryCurrency currency) {
		return Money.of(currency, this.interestCompleted);
	}
	
	public Money getTotal(MonetaryCurrency currency) {
		return getPrincipal(currency).plus(getInterest(currency));
	}
	
	public void updateLoan(final Loan loan) {
		this.loan = loan;
	}

	public boolean unpaid() {
		return !this.completed;
	}

	public Money getTotalDue(MonetaryCurrency currency) {
		return getTotal(currency).minus(getTotalCompleted(currency));
	}

	private Money getTotalCompleted(MonetaryCurrency currency) {
		return getPrincipalCompleted(currency).plus(getInterestCompleted(currency));
	}

//	public void payOffWith(final LoanTransaction transaction, final MonetaryCurrency currency) {
//		Money transactionAmountRemaining = Money.of(currency, transaction.getAmount());
//		
//		Money interestDue = getInterest(currency);
//		if (transactionAmountRemaining.isGreaterThanOrEqualTo(interestDue)) {
//			this.interestCompleted = interestDue.getAmount();
//			transactionAmountRemaining = transactionAmountRemaining.minus(interestDue);
//		} else {
//			this.interestCompleted = transactionAmountRemaining.getAmount();
//		}
//		
//		Money principalDue = getPrincipal(currency);
//	}
}