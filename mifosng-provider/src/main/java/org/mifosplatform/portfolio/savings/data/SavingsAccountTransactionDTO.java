package org.mifosplatform.portfolio.savings.data;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.mifosplatform.portfolio.paymentdetail.domain.PaymentDetail;

public class SavingsAccountTransactionDTO {

    private final DateTimeFormatter formatter;
    private final LocalDate transactionDate;
    private final BigDecimal transactionAmount;
    private final PaymentDetail paymentDetail;

    public SavingsAccountTransactionDTO(final DateTimeFormatter formatter, final LocalDate transactionDate,
            final BigDecimal transactionAmount, final PaymentDetail paymentDetail) {
        this.formatter = formatter;
        this.transactionDate = transactionDate;
        this.transactionAmount = transactionAmount;
        this.paymentDetail = paymentDetail;
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
}
