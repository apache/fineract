package org.mifosplatform.portfolio.savingsdepositaccount.domain;

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
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_deposit_account_transaction")
public class DepositAccountTransaction extends AbstractPersistable<Long> {

    @SuppressWarnings("unused")
    @ManyToOne(optional = false)
    @JoinColumn(name = "deposit_account_id", nullable = false)
    private DepositAccount depositAccount;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "transaction_type_enum", nullable = false)
    private DepositAccountTransactionType typeOf;

    @SuppressWarnings("unused")
    @OneToOne(optional = true, cascade = { CascadeType.PERSIST })
    @JoinColumn(name = "contra_id")
    private DepositAccountTransaction contra;

    @Temporal(TemporalType.DATE)
    @Column(name = "transaction_date", nullable = false)
    private final Date dateOf;

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private final BigDecimal amount;

    @SuppressWarnings("unused")
    @Column(name = "interest", scale = 6, precision = 19, nullable = false)
    private final BigDecimal interest;

    @SuppressWarnings("unused")
    @Column(name = "total", scale = 6, precision = 19, nullable = false)
    private final BigDecimal total;

    protected DepositAccountTransaction() {
        this.depositAccount = null;
        this.typeOf = null;
        this.amount = null;
        this.dateOf = null;
        this.interest = null;
        this.total = null;
    }

    private DepositAccountTransaction(DepositAccountTransactionType type, final BigDecimal amount, final LocalDate date,
            final BigDecimal interest) {
        this.typeOf = type;
        this.amount = amount;
        this.dateOf = date.toDateMidnight().toDate();
        this.interest = interest;
        this.total = amount.doubleValue() == 0 ? new BigDecimal(0) : amount.add(interest);
    }
    
	private DepositAccountTransaction(DepositAccountTransactionType type, final BigDecimal amount, final LocalDate date, final BigDecimal interest, final BigDecimal total) {
		this.typeOf = type;
        this.amount = amount;
		this.dateOf = date.toDateMidnight().toDate();
		this.interest = interest;
		this.total = total;
	}

    public Date getDateOf() {
        return dateOf;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public DepositAccountTransactionType getTypeOf() {
        return typeOf;
    }

    public Money getAmount(MonetaryCurrency currency) {
        return Money.of(currency, this.amount);
    }

    public LocalDate getTransactionDate() {
        return new LocalDate(this.dateOf);
    }

    public boolean isDeposit() {
        return DepositAccountTransactionType.DEPOSIT.equals(typeOf);
    }

    public static DepositAccountTransaction deposit(Money amount, LocalDate paymentDate, Money interest) {
        return new DepositAccountTransaction(DepositAccountTransactionType.DEPOSIT,
                amount == null ? new BigDecimal(0) : amount.getAmount(), paymentDate, interest == null ? new BigDecimal(0)
                        : interest.getAmount());
    }

   /* public static DepositAccountTransaction withdraw(Money amount, LocalDate paymentDate, Money interest) {
        return new DepositAccountTransaction(DepositAccountTransactionType.WITHDRAW, amount == null ? new BigDecimal(0)
                : amount.getAmount(), paymentDate, interest == null ? new BigDecimal(0) : interest.getAmount());
    }

    public static DepositAccountTransaction postInterest(Money depositAmount, LocalDate paymentDate, Money interest) {
        return new DepositAccountTransaction(DepositAccountTransactionType.INTEREST_POSTING, depositAmount == null ? new BigDecimal(0)
                : depositAmount.getAmount(), paymentDate, interest == null ? new BigDecimal(0) : interest.getAmount());
    }*/
    
    public static DepositAccountTransaction withdraw(Money amount, LocalDate paymentDate, Money interest, BigDecimal total) {
		return new DepositAccountTransaction(DepositAccountTransactionType.WITHDRAW, amount == null ? new BigDecimal(0) : amount.getAmount(), paymentDate, interest==null ? new BigDecimal(0) : interest.getAmount(),total);

	}
	
	public static DepositAccountTransaction postInterest(Money depositAmount, LocalDate paymentDate, Money interest, BigDecimal total) {
		return new DepositAccountTransaction(DepositAccountTransactionType.INTEREST_POSTING, depositAmount == null ? new BigDecimal(0) : depositAmount.getAmount(), paymentDate, interest==null ? new BigDecimal(0) : interest.getAmount(),total);

	}

    public void updateAccount(DepositAccount depositAccount) {
        this.depositAccount = depositAccount;
    }

}