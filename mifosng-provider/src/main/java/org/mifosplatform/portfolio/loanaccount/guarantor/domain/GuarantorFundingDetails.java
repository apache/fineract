/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.guarantor.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.mifosplatform.portfolio.account.domain.AccountAssociations;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.savings.domain.SavingsAccount;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_guarantor_funding_details")
public class GuarantorFundingDetails extends AbstractPersistable<Long> {

    @ManyToOne
    @JoinColumn(name = "guarantor_id", nullable = false)
    private Guarantor guarantor;

    @ManyToOne
    @JoinColumn(name = "account_associations_id", nullable = false)
    private AccountAssociations accountAssociations;

    @Column(name = "status_enum", nullable = false)
    private Integer status;

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal amount;

    @Column(name = "amount_released_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal amountReleased;

    @Column(name = "amount_remaining_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal amountRemaining;

    @Column(name = "amount_transfered_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal amountTransfered;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "guarantorFundingDetails", orphanRemoval = true)
    private final List<GuarantorFundingTransaction> guarantorFundingTransactions = new ArrayList<>();

    protected GuarantorFundingDetails() {}

    public GuarantorFundingDetails(final AccountAssociations accountAssociations, final Integer status, final BigDecimal amount) {
        this.accountAssociations = accountAssociations;
        this.status = status;
        this.amount = amount;
        this.amountRemaining = amount;
    }

    public void updateGuarantor(final Guarantor guarantor) {
        this.guarantor = guarantor;
    }

    public void updateStatus(final GuarantorFundStatusType guarantorFundStatusType) {
        this.status = guarantorFundStatusType.getValue();
    }

    public GuarantorFundStatusType getStatus() {
        return GuarantorFundStatusType.fromInt(this.status);
    }

    public SavingsAccount getLinkedSavingsAccount() {
        return accountAssociations.linkedSavingsAccount();
    }

    public Loan getLoanAccount() {
        return this.guarantor.getLoan();
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public BigDecimal getAmountReleased() {
        return this.amountReleased == null ? BigDecimal.ZERO : this.amountReleased;
    }

    public BigDecimal getAmountRemaining() {
        return this.amountRemaining == null ? BigDecimal.ZERO : this.amountRemaining;
    }

    public BigDecimal getAmountTransfered() {
        return this.amountTransfered == null ? BigDecimal.ZERO : this.amountTransfered;
    }

    public void releaseFunds(final BigDecimal amount) {
        this.amountReleased = getAmountReleased().add(amount);
        this.amountRemaining = getAmountRemaining().subtract(amount);
        if (this.amountRemaining.compareTo(BigDecimal.ZERO) == 0) {
            this.updateStatus(GuarantorFundStatusType.COMPLETED);
        }
    }

    public void undoReleaseFunds(final BigDecimal amount) {
        this.amountReleased = getAmountReleased().subtract(amount);
        this.amountRemaining = getAmountRemaining().add(amount);
        if (getStatus().isCompleted() && this.amountRemaining.compareTo(BigDecimal.ZERO) == 1) {
            this.updateStatus(GuarantorFundStatusType.ACTIVE);
        }
    }

    public void withdrawFunds(final BigDecimal amount) {
        this.amountTransfered = amount;
    }

    public void addGuarantorFundingTransactions(final GuarantorFundingTransaction guarantorFundingTransaction) {
        this.guarantorFundingTransactions.add(guarantorFundingTransaction);
    }

    public void undoAllTransactions() {
        for (GuarantorFundingTransaction fundingTransaction : this.guarantorFundingTransactions) {
            fundingTransaction.reverseTransaction();
        }
    }
}
