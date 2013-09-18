package org.mifosplatform.portfolio.account.data;

import java.math.BigDecimal;
import java.util.Locale;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.mifosplatform.portfolio.account.PortfolioAccountType;
import org.mifosplatform.portfolio.paymentdetail.domain.PaymentDetail;

public class AccountTransferDTO {

    private LocalDate transactionDate;
    private BigDecimal transactionAmount;
    private PortfolioAccountType fromAccountType;
    private PortfolioAccountType toAccountType;
    private Long fromAccountId;
    private Long toAccountId;
    private String description;
    private Locale locale;
    private DateTimeFormatter fmt;
    private PaymentDetail paymentDetail;
    private Integer fromTransferType;
    private Integer toTransferType;
    private Long chargeId;

    public AccountTransferDTO(final LocalDate transactionDate, final BigDecimal transactionAmount,
            final PortfolioAccountType fromAccountType, final PortfolioAccountType toAccountType, final Long fromAccountId,
            final Long toAccountId, final String description, final Locale locale, final DateTimeFormatter fmt,
            final PaymentDetail paymentDetail, final Integer fromTransferType, final Integer toTransferType, final Long chargeId) {
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

}
