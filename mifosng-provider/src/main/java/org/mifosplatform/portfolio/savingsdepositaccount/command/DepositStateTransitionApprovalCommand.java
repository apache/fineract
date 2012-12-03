package org.mifosplatform.portfolio.savingsdepositaccount.command;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

public class DepositStateTransitionApprovalCommand {

    private final Long accountId;
    private final Long productId;
    private final LocalDate eventDate;
    private final BigDecimal depositAmount;
    private final BigDecimal maturityInterestRate;
    private final Integer tenureInMonths;
    private final Integer interestCompoundedEveryPeriodType;
    private final Integer interestCompoundedEvery;

    private final String note;

    public DepositStateTransitionApprovalCommand(final Long resourceIdentifier, final Long productId, final LocalDate eventDate,
            final Integer tenureInMonths, final BigDecimal depositAmount, final Integer interestCompoundedEveryPeriodType,
            final Integer interestCompoundedEvery, final String note, final BigDecimal maturityInterestRate) {

        this.accountId = resourceIdentifier;
        this.eventDate = eventDate;
        this.depositAmount = depositAmount;
        this.maturityInterestRate = maturityInterestRate;
        this.tenureInMonths = tenureInMonths;
        this.interestCompoundedEveryPeriodType = interestCompoundedEveryPeriodType;
        this.productId = productId;
        this.interestCompoundedEvery = interestCompoundedEvery;

        this.note = note;

    }

    public Long getAccountId() {
        return accountId;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public BigDecimal getDepositAmount() {
        return depositAmount;
    }

    public BigDecimal getMaturityInterestRate() {
        return maturityInterestRate;
    }

    public Integer getTenureInMonths() {
        return tenureInMonths;
    }

    public Integer getInterestCompoundedEveryPeriodType() {
        return interestCompoundedEveryPeriodType;
    }

    public Long getProductId() {
        return productId;
    }

    public Integer getInterestCompoundedEvery() {
        return interestCompoundedEvery;
    }

    public String getNote() {
        return note;
    }
}