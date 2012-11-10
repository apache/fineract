package org.mifosplatform.portfolio.savingsaccount.command;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

public class CalculateSavingScheduleCommand {

    private final Long productId;
    private final BigDecimal deposit;
    private final Integer payEvery;
    private final Integer paymentFrequencyType;
    private final BigDecimal interestRate;
    private final LocalDate paymentsStartingFromDate;
    private final Integer tenure;

    public CalculateSavingScheduleCommand(final Long productId, final BigDecimal deposit, final Integer payEvery,
            final Integer paymentFrequencyType, final BigDecimal interestRate, final LocalDate paymentsStartingFromDate,
            final Integer tenure) {

        this.productId = productId;
        this.deposit = deposit;
        this.payEvery = payEvery;
        this.paymentFrequencyType = paymentFrequencyType;
        this.interestRate = interestRate;
        this.paymentsStartingFromDate = paymentsStartingFromDate;
        this.tenure = tenure;

    }

    public Long getProductId() {
        return this.productId;
    }

    public BigDecimal getDeposit() {
        return this.deposit;
    }

    public Integer getPayEvery() {
        return this.payEvery;
    }

    public Integer getPaymentFrequencyType() {
        return this.paymentFrequencyType;
    }

    public BigDecimal getInterestRate() {
        return this.interestRate;
    }

    public LocalDate getPaymentsStartingFromDate() {
        return this.paymentsStartingFromDate;
    }

    public Integer getTenure() {
        return this.tenure;
    }


}
