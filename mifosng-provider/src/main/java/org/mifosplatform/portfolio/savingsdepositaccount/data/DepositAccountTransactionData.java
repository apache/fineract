package org.mifosplatform.portfolio.savingsdepositaccount.data;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;

/**
 * Immutable data object to represent transactions on a deposit account.
 */
public class DepositAccountTransactionData {

    private final Long transactionId;
    private final EnumOptionData transactionType;
    private final LocalDate transactionDate;
    private final BigDecimal transactionAmount;
    private final Long accountId;
    private final BigDecimal interestAmount;
    private final BigDecimal total;

    public DepositAccountTransactionData(final Long transactionId, final Long accountId, final EnumOptionData transactionType,
            final LocalDate transactionDate, final BigDecimal transactionAmount, final BigDecimal interstAmount, final BigDecimal total) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.transactionType = transactionType;
        this.transactionDate = transactionDate;
        this.transactionAmount = transactionAmount;
        this.interestAmount = interstAmount;
        this.total = total;
    }

    public Long getAccountId() {
        return accountId;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public EnumOptionData getTransactionType() {
        return transactionType;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public BigDecimal getTransactionAmount() {
        return transactionAmount;
    }

    public BigDecimal getInterestAmount() {
        return interestAmount;
    }

    public BigDecimal getTotal() {
        return total;
    }
}