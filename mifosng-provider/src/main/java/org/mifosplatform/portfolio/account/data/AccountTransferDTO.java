package org.mifosplatform.portfolio.account.data;

import java.math.BigDecimal;
import java.util.Locale;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.mifosplatform.portfolio.account.PortfolioAccountType;
import org.mifosplatform.portfolio.paymentdetail.domain.PaymentDetail;

public class AccountTransferDTO {

    private final LocalDate transactionDate;
    private final BigDecimal transactionAmount;
    private final PortfolioAccountType fromAccountType;
    private final PortfolioAccountType toAccountType;
    private final Long fromAccountId;
    private final Long toAccountId;
    private final String description;
    private final Locale locale;
    private final DateTimeFormatter fmt;
    private final PaymentDetail paymentDetail;
    private final Integer fromTransferType;
    private final Integer toTransferType;
    private final Long chargeId;
    private final Integer loanInstallmentNumber;

    public AccountTransferDTO(final LocalDate transactionDate, final BigDecimal transactionAmount,
            final PortfolioAccountType fromAccountType, final PortfolioAccountType toAccountType, final Long fromAccountId,
            final Long toAccountId, final String description, final Locale locale, final DateTimeFormatter fmt,
            final PaymentDetail paymentDetail, final Integer fromTransferType, final Integer toTransferType, final Long chargeId, Integer loanInstallmentNumber) {
        this.transactionDate = transactionDate;
        this.transactionAmount = transactionAmount;
        this.fromAccountType = fromAccountType;
        this.toAccountType = toAccountType;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.description = description;
        this.locale = locale;
        this.fmt = fmt;
        this.paymentDetail = paymentDetail;
        this.fromTransferType = fromTransferType;
        this.toTransferType = toTransferType;
        this.chargeId = chargeId;
        this.loanInstallmentNumber = loanInstallmentNumber;
    }

    public LocalDate getTransactionDate() {
        return this.transactionDate;
    }

    public BigDecimal getTransactionAmount() {
        return this.transactionAmount;
    }

    public PortfolioAccountType getFromAccountType() {
        return this.fromAccountType;
    }

    public PortfolioAccountType getToAccountType() {
        return this.toAccountType;
    }

    public Long getFromAccountId() {
        return this.fromAccountId;
    }

    public Long getToAccountId() {
        return this.toAccountId;
    }

    public String getDescription() {
        return this.description;
    }

    public Locale getLocale() {
        return this.locale;
    }

    public DateTimeFormatter getFmt() {
        return this.fmt;
    }

    public PaymentDetail getPaymentDetail() {
        return this.paymentDetail;
    }

    public Integer getFromTransferType() {
        return this.fromTransferType;
    }

    public Integer getToTransferType() {
        return this.toTransferType;
    }

    public Long getChargeId() {
        return this.chargeId;
    }

    
    public Integer getLoanInstallmentNumber() {
        return this.loanInstallmentNumber;
    }

}
