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
    private BigDecimal  principal = BigDecimal.ZERO;
    
    @Column(name = "interest_amount", scale=6, precision=19)
    private BigDecimal   interest = BigDecimal.ZERO;
    
    @Column(name = "principal_completed_derived", scale=6, precision=19)
    private BigDecimal   principalCompleted = BigDecimal.ZERO;
    
	@Column(name = "interest_completed_derived", scale = 6, precision = 19)
	private BigDecimal interestCompleted = BigDecimal.ZERO;
	
	@Column(name = "interest_waived_derived", scale = 6, precision = 19)
	private BigDecimal interestWaived = BigDecimal.ZERO;
    
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

	public Money getPrincipal(final MonetaryCurrency currency) {
		return Money.of(currency, this.principal);
	}
	
	public Money getPrincipalCompleted(final MonetaryCurrency currency) {
		return Money.of(currency, this.principalCompleted);
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

	public boolean isInterestDue(final MonetaryCurrency currency) {
		return getInterest(currency).minus(getInterestCompleted(currency).plus(getInterestWaived(currency))).isGreaterThanZero();
	}
	
	public Money getTotal(MonetaryCurrency currency) {
		return getPrincipal(currency).plus(getInterest(currency));
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
		return getPrincipal(currency).minus(getPrincipalCompleted(currency)).isZero();
	}

	public Money getTotalDue(MonetaryCurrency currency) {
		final Money totalPaidOrWaived = getInterestWaived(currency).plus(getTotalCompleted(currency));
		return getTotal(currency).minus(totalPaidOrWaived);
	}

	private Money getTotalCompleted(MonetaryCurrency currency) {
		return getPrincipalCompleted(currency).plus(getInterestCompleted(currency));
	}

	public void resetDerivedComponents() {
		this.principalCompleted = BigDecimal.ZERO;
		this.interestCompleted = BigDecimal.ZERO;
		this.interestWaived = BigDecimal.ZERO;
		this.completed = false;
	}

	public Money payInterestComponent(final Money transactionAmountRemaining) {
		
		MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
		
		Money interestPortionOfTransaction = Money.zero(currency);
		
		Money interestExpected = getInterest(currency);
		Money interestDue = interestExpected.minus(getInterestCompleted(currency));
		if (transactionAmountRemaining.isGreaterThanOrEqualTo(interestDue)) {
			this.interestCompleted = getInterestCompleted(currency).plus(interestDue).getAmount();
			interestPortionOfTransaction = interestPortionOfTransaction.plus(interestDue);
		} else {
			this.interestCompleted = getInterestCompleted(currency).plus(transactionAmountRemaining).getAmount();
			interestPortionOfTransaction = interestPortionOfTransaction.plus(transactionAmountRemaining);
		}
		
		this.completed = getTotalDue(currency).isZero();
		
		return interestPortionOfTransaction;
	}

	public Money payPrincipalComponent(final Money transactionAmountRemaining) {
		
		MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
		Money principalPortionOfTransaction = Money.zero(currency);
		
		Money principalExpected = getPrincipal(currency);
		Money principalDue = principalExpected.minus(getPrincipalCompleted(currency));
		if (transactionAmountRemaining.isGreaterThanOrEqualTo(principalDue)) {
			this.principalCompleted = getPrincipalCompleted(currency).plus(principalDue).getAmount();
			principalPortionOfTransaction = principalPortionOfTransaction.plus(principalDue);
		} else {
			this.principalCompleted = getPrincipalCompleted(currency).plus(transactionAmountRemaining).getAmount();
			principalPortionOfTransaction = principalPortionOfTransaction.plus(transactionAmountRemaining);
		}
		
		this.completed = getTotalDue(currency).isZero();
		
		return principalPortionOfTransaction;
	}

	public Money waiveInterestComponent(final Money transactionAmountRemaining) {
		MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
		Money waivedInterestPortionOfTransaction = Money.zero(currency);
		
		Money interestExpected = getInterest(currency);
		Money interestDue = interestExpected.minus(getInterestCompleted(currency));
		if (transactionAmountRemaining.isGreaterThanOrEqualTo(interestDue)) {
			this.interestWaived = getInterestWaived(currency).plus(interestDue).getAmount();
			waivedInterestPortionOfTransaction = waivedInterestPortionOfTransaction.plus(interestDue);
		} else {
			this.interestWaived = getInterestWaived(currency).plus(transactionAmountRemaining).getAmount();
			waivedInterestPortionOfTransaction = waivedInterestPortionOfTransaction.plus(transactionAmountRemaining);
		}
		
		this.completed = getTotalDue(currency).isZero();
		
		return waivedInterestPortionOfTransaction;
	}

	public boolean isOverdueOn(final LocalDate transactionDate) {
		return this.getDueDate().isBefore(transactionDate);
	}
}