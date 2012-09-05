package org.mifosng.platform.saving.domain;

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
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name="m_deposit_account_transaction")
public class DepositAccountTransaction extends AbstractPersistable<Long> {
	
	@ManyToOne(optional = false)
    @JoinColumn(name = "deposit_account_id", nullable=false)
    private DepositAccount depositAccount;
	
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "transaction_type_enum", nullable = false)
	private DepositTransactionType typeOf;
	
	@OneToOne(optional=true, cascade={CascadeType.PERSIST})
	@JoinColumn(name="contra_id")
	private DepositAccountTransaction contra;
	
	@Temporal(TemporalType.DATE)
    @Column(name = "transaction_date", nullable=false)
    private final Date  dateOf;
	
	@Column(name = "amount", scale = 6, precision = 19, nullable = false)
	private final BigDecimal amount;
	
	protected DepositAccountTransaction(){
		
		this.depositAccount=null;
		this.typeOf=null;
		this.amount=null;
		this.dateOf=null;
		
	}
	
	private DepositAccountTransaction(DepositTransactionType type, final BigDecimal amount, final LocalDate date) {
		
		this.typeOf = type;
        this.amount = amount;
		this.dateOf = date.toDateMidnight().toDate();
   
	}

	public Date getDateOf() {
		return dateOf;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public DepositTransactionType getTypeOf() {
		return typeOf;
	}

	
	public Money getAmount(MonetaryCurrency currency) {
		return Money.of(currency, this.amount);
	}


	
	@DateTimeFormat(style="-M")
    public LocalDate getTransactionDate() {
        return new LocalDate(this.dateOf);
    }

	public DepositAccountTransaction getContra() {
		return contra;
	}
	
	public boolean isNotContra() {
		return this.contra == null;
	}
	
	public boolean isDeposit(){
		return DepositTransactionType.DEPOSIT.equals(typeOf) && isNotContra();
	}

	public static DepositAccountTransaction deposit(Money amount, LocalDate paymentDate) {
		return new DepositAccountTransaction(DepositTransactionType.DEPOSIT, amount.getAmount(), paymentDate);
	}
	 
	public static DepositAccountTransaction withdraw(Money amount, LocalDate paymentDate) {
		return new DepositAccountTransaction(DepositTransactionType.WITHDRAW, amount.getAmount(), paymentDate);
	}
	
	private static DepositAccountTransaction contra(DepositAccountTransaction originalTransaction) {
		
		DepositAccountTransaction contra = new DepositAccountTransaction(DepositTransactionType.REVERSAL, originalTransaction.getAmount().negate(), new LocalDate(originalTransaction.getDateOf()));
		contra.updateContra(originalTransaction);
		
		return contra;
	}
	
	public void updateContra(DepositAccountTransaction transaction) {
		this.contra = transaction;
	}

	public DepositAccount getDepositAccount() {
		return depositAccount;
	}

	public void setDepositAccount(DepositAccount depositAccount) {
		this.depositAccount = depositAccount;
	}

	public void updateAccount(DepositAccount depositAccount) {
		this.depositAccount=depositAccount;
		
	}

}
