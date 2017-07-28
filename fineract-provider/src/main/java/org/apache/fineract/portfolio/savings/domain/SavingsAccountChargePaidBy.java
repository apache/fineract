/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.savings.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_savings_account_charge_paid_by")
public class SavingsAccountChargePaidBy extends AbstractPersistableCustom<Long> {

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
