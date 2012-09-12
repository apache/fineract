package org.mifosng.platform.loan.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.joda.time.LocalDate;
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.currency.domain.Money;
import org.mifosng.platform.infrastructure.AbstractAuditableCustom;
import org.mifosng.platform.user.domain.AppUser;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * All monetary transactions against a loan are modelled through this entity.
 * Disbursements, Repayments, Waivers, Write-off etc
 */
@Entity
@Table(name = "m_loan_transaction")
public class LoanTransaction extends AbstractAuditableCustom<AppUser, Long> {

    @ManyToOne(optional = false)
    @JoinColumn(name = "loan_id", nullable=false)
    private Loan        loan;

	@Column(name = "amount", scale = 6, precision = 19, nullable = false)
	private final BigDecimal amount;
	
	@Column(name = "principal_portion_derived", scale = 6, precision = 19, nullable = false)
	private BigDecimal principalPortion = BigDecimal.ZERO;
	
	@Column(name = "interest_portion_derived", scale = 6, precision = 19, nullable = false)
	private BigDecimal interestPortion = BigDecimal.ZERO;
	
	@Column(name = "interest_waived_derived", scale = 6, precision = 19)
	private BigDecimal interestWaivedPortion = BigDecimal.ZERO;
	
    @Temporal(TemporalType.DATE)
    @Column(name = "transaction_date", nullable=false)
    private final Date  dateOf;
    
    @Enumerated(EnumType.ORDINAL)
	@Column(name = "transaction_type_enum", nullable = false)
	private Integer typeOf;
    
    @OneToOne(optional=true, cascade={CascadeType.PERSIST})
    @JoinColumn(name="contra_id")
    private LoanTransaction contra;
    
    protected LoanTransaction() {
        this.loan = null;
        this.amount = null;
        this.dateOf = null;
        this.typeOf = null;
    }
    
    public static LoanTransaction disbursement(final Money amount, final LocalDate disbursementDate) {
		return new LoanTransaction(LoanTransactionType.DISBURSEMENT, amount.getAmount(), disbursementDate);
	}
    
	public static LoanTransaction repayment(final Money amount, final LocalDate paymentDate) {
		return new LoanTransaction(LoanTransactionType.REPAYMENT, amount.getAmount(), paymentDate);
	}
	
	public static LoanTransaction waiver(final Money waived, final LocalDate waiveDate) {
		return new LoanTransaction(LoanTransactionType.WAIVED, waived.getAmount(), waiveDate);
	}
	
	private static LoanTransaction contra(LoanTransaction originalTransaction) {
		LoanTransaction contra = new LoanTransaction(LoanTransactionType.REVERSAL, originalTransaction.getAmount().negate(), new LocalDate(originalTransaction.getDateOf()));
		contra.updateContra(originalTransaction);
		return contra;
	}
	
	public void updateContra(final LoanTransaction transaction) {
		this.contra = transaction;
	}

	private LoanTransaction(final LoanTransactionType type, final BigDecimal amount, final LocalDate date) {
		this.typeOf = type.getValue();
        this.amount = amount;
		this.dateOf = date.toDateMidnight().toDate();
    }

    public BigDecimal getAmount() {
        return this.amount;
    }
    
	public Money getAmount(MonetaryCurrency currency) {
		return Money.of(currency, this.amount);
	}

    @DateTimeFormat(style="-M")
    public LocalDate getTransactionDate() {
        return new LocalDate(this.dateOf);
    }

	public Date getDateOf() {
		return dateOf;
	}

	public LoanTransactionType getTypeOf() {
		return LoanTransactionType.fromInt(this.typeOf);
	}

	public boolean isRepayment() {
		return LoanTransactionType.REPAYMENT.equals(getTypeOf()) && isNotContra();
	}
	
	public boolean isNotRepayment() {
		return !isRepayment();
	}
	
	public boolean isNotContra() {
		return this.contra == null;
	}

	public boolean isDisbursement() {
		return LoanTransactionType.DISBURSEMENT.equals(getTypeOf());
	}
	
	public boolean isWaiver() {
		return LoanTransactionType.WAIVED.equals(getTypeOf()) && isNotContra();
	}
	
	public boolean isNotWaiver() {
		return !isWaiver();
	}

	public boolean isIdentifiedBy(Long identifier) {
		return this.getId().equals(identifier);
	}
	
	public boolean isBelongingToLoanOf(Loan check) {
		return this.loan.getId().equals(check.getId());
	}
	
	public boolean isNotBelongingToLoanOf(Loan check) {
		return !isBelongingToLoanOf(check);
	}

	public void contra() {
		this.contra = LoanTransaction.contra(this);
		contra.updateLoan(this.loan);
	}

    public void updateLoan(final Loan loan) {
        this.loan = loan;
    }

	public boolean isNonZero() {
		return this.amount.subtract(BigDecimal.ZERO).doubleValue() > 0;
	}
	
	/**
	 * This updates the derived fields of a loan transaction for the principal, interest and interest waived portions.
	 * 
	 * This accumulates the values passed to the already existent values for each of the portions.
	 */
	public void updateComponents(final Money principal, final Money interest, final Money interestWaivedPortion) {
		MonetaryCurrency currency = principal.getCurrency();
		this.principalPortion = getPrincipalPortion(currency).plus(principal).getAmount();
		this.interestPortion = getInterestPortion(currency).plus(interest).getAmount();
		this.interestWaivedPortion = getInterestWaivedPortion(currency).plus(interestWaivedPortion).getAmount();
	}

	public Money getPrincipalPortion(MonetaryCurrency currency) {
		return Money.of(currency, principalPortion);
	}

	public Money getInterestPortion(MonetaryCurrency currency) {
		return  Money.of(currency, interestPortion);
	}
	
	public Money getInterestWaivedPortion(MonetaryCurrency currency) {
		if (this.interestWaivedPortion == null) {
			this.interestWaivedPortion = BigDecimal.ZERO;
		}
		return  Money.of(currency, interestWaivedPortion);
	}

	public void resetDerivedComponents() {
		this.principalPortion = BigDecimal.ZERO;
		this.interestPortion = BigDecimal.ZERO;
	}
}