/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_savings_account_charge_paid_by")
public class SavingsAccountChargePaidBy extends AbstractPersistable<Long> {

    @ManyToOne
    @JoinColumn(name = "savings_account_transaction_id", nullable = false)
    private SavingsAccountTransaction savingsAccountTransaction;

    @ManyToOne
    @JoinColumn(name = "savings_account_charge_id", nullable = false)
    private SavingsAccountCharge savingsAccountCharge;

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal amount;

    protected SavingsAccountChargePaidBy() {

    }

    public static SavingsAccountChargePaidBy instance(final SavingsAccountTransaction savingsAccountTransaction,
            final SavingsAccountCharge savingsAccountCharge, final BigDecimal amount) {
        return new SavingsAccountChargePaidBy(savingsAccountTransaction, savingsAccountCharge, amount);
    }

    private SavingsAccountChargePaidBy(final SavingsAccountTransaction savingsAccountTransaction,
            final SavingsAccountCharge savingsAccountCharge, final BigDecimal amount) {
        this.savingsAccountTransaction = savingsAccountTransaction;
        this.savingsAccountCharge = savingsAccountCharge;
        this.amount = amount;
    }

    public SavingsAccountTransaction getSavingsAccountTransaction() {
        return this.savingsAccountTransaction;
    }

    public void setSavingsAccountTransaction(final SavingsAccountTransaction savingsAccountTransaction) {
        this.savingsAccountTransaction = savingsAccountTransaction;
    }

    public SavingsAccountCharge getSavingsAccountCharge() {
        return this.savingsAccountCharge;
    }

    public void setSavingsAccountCharge(final SavingsAccountCharge savingsAccountCharge) {
        this.savingsAccountCharge = savingsAccountCharge;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setAmount(final BigDecimal amount) {
        this.amount = amount;
    }

    public boolean isFeeCharge() {
        return (this.savingsAccountCharge == null) ? false : this.savingsAccountCharge.isFeeCharge();
    }

    public boolean isPenaltyCharge() {
        return (this.savingsAccountCharge == null) ? false : this.savingsAccountCharge.isPenaltyCharge();
    }

    public boolean canOverriteSavingAccountRules() {
        return (this.savingsAccountCharge == null) ? false : this.savingsAccountCharge.canOverriteSavingAccountRules();
    }
}
