package org.mifosplatform.portfolio.savings.data;

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.mifosplatform.portfolio.paymentdetail.domain.PaymentDetail;

public class SavingsAccountTransactionDTO {

    private final DateTimeFormatter formatter;
    private final LocalDate transactionDate;
    private final BigDecimal transactionAmount;
    private final List<Long> existingTransactionIds;
    private final List<Long> existingReversedTransactionIds;
    private final PaymentDetail paymentDetail;

    public SavingsAccountTransactionDTO(final DateTimeFormatter formatter, final LocalDate transactionDate,
            final BigDecimal transactionAmount, final List<Long> existingTransactionIds, final List<Long> existingReversedTransactionIds,
            final PaymentDetail paymentDetail) {
        this.formatter = formatter;
        this.transactionDate = transactionDate;
        this.transactionAmount = transactionAmount;
        this.existingTransactionIds = existingTransactionIds;
        this.existingReversedTransactionIds = existingReversedTransactionIds;
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

    public List<Long> getExistingTransactionIds() {
        return this.existingTransactionIds;
    }

    public List<Long> getExistingReversedTransactionIds() {
        return this.existingReversedTransactionIds;
    }

    public PaymentDetail getPaymentDetail() {
        return this.paymentDetail;
    }
}
