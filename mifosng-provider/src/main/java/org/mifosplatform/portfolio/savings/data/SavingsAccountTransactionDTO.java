package org.mifosplatform.portfolio.savings.data;

import java.math.BigDecimal;
import java.util.Date;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.mifosplatform.portfolio.paymentdetail.domain.PaymentDetail;

public class SavingsAccountTransactionDTO {

    private final DateTimeFormatter formatter;
    private final LocalDate transactionDate;
    private final BigDecimal transactionAmount;
    private final PaymentDetail paymentDetail;
    private final Date createdDate;
    private final Long savingsAccountId;

    public SavingsAccountTransactionDTO(final DateTimeFormatter formatter, final LocalDate transactionDate,
            final BigDecimal transactionAmount, final PaymentDetail paymentDetail, final Date createdDate) {
        this.formatter = formatter;
        this.transactionDate = transactionDate;
        this.transactionAmount = transactionAmount;
        this.paymentDetail = paymentDetail;
        this.createdDate = createdDate;
        this.savingsAccountId = null;
    }
    
    /** This constructor is used for bulk deposit transactions
     * @param formatter
     * @param transactionDate
     * @param transactionAmount
     * @param paymentDetail
     * @param createdDate
     * @param savingsAccountId
     */
    public SavingsAccountTransactionDTO(DateTimeFormatter formatter, LocalDate transactionDate, BigDecimal transactionAmount,
            PaymentDetail paymentDetail, Date createdDate, Long savingsAccountId) {
        this.formatter = formatter;
        this.transactionDate = transactionDate;
        this.transactionAmount = transactionAmount;
        this.paymentDetail = paymentDetail;
        this.createdDate = createdDate;
        this.savingsAccountId = savingsAccountId;
    }



    public DateTimeFormatter getFormatter() {
        return this.formatter;
    }

    public LocalDate getTransactionDate() {
        return this.transactionDate;
    }

    public BigDecimal getTransactionAmount() {
        return this.transactionAmount;
    }

    public PaymentDetail getPaymentDetail() {
        return this.paymentDetail;
    }

    public Date getCreatedDate() {
        return this.createdDate;
    }
    
    public Long getSavingsAccountId() {
        return this.savingsAccountId;
    }
    
}
