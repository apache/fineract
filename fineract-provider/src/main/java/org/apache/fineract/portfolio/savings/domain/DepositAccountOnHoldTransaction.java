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

import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.guarantor.domain.GuarantorFundingTransaction;
import org.apache.fineract.portfolio.savings.DepositAccountOnHoldTransactionType;
import org.joda.time.LocalDate;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "m_deposit_account_on_hold_transaction")
public class DepositAccountOnHoldTransaction extends AbstractPersistableCustom<Long> {

    @ManyToOne
    @JoinColumn(name = "savings_account_id", nullable = true)
    private SavingsAccount savingsAccount;

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal amount;

    @Column(name = "transaction_type_enum", nullable = false)
    private Integer transactionType;

    @Temporal(TemporalType.DATE)
    @Column(name = "transaction_date", nullable = false)
    private Date transactionDate;

    @Column(name = "is_reversed", nullable = false)
    private boolean reversed;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date", nullable = false)
    private Date createdDate;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "depositAccountOnHoldTransaction", optional = true, orphanRemoval = true)
    private GuarantorFundingTransaction guarantorFundingTransaction;

    protected DepositAccountOnHoldTransaction() {}

    private DepositAccountOnHoldTransaction(final SavingsAccount savingsAccount, final BigDecimal amount,
            final DepositAccountOnHoldTransactionType transactionType, final LocalDate transactionDate, final boolean reversed) {
        this.savingsAccount = savingsAccount;
        this.amount = amount;
        this.transactionType = transactionType.getValue();
        this.transactionDate = transactionDate.toDate();
        this.createdDate = new Date();
        this.reversed = reversed;
    }

    public static DepositAccountOnHoldTransaction hold(final SavingsAccount savingsAccount, final BigDecimal amount,
            final LocalDate transactionDate) {
        boolean reversed = false;
        return new DepositAccountOnHoldTransaction(savingsAccount, amount, DepositAccountOnHoldTransactionType.HOLD, transactionDate,
                reversed);
    }

    public static DepositAccountOnHoldTransaction release(final SavingsAccount savingsAccount, final BigDecimal amount,
            final LocalDate transactionDate) {
        boolean reversed = false;
        return new DepositAccountOnHoldTransaction(savingsAccount, amount, DepositAccountOnHoldTransactionType.RELEASE, transactionDate,
                reversed);
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public Money getAmountMoney(final MonetaryCurrency currency) {
        return Money.of(currency, this.amount);
    }

    public void reverseTransaction() {
        this.reversed = true;
        if (this.getTransactionType().isHold()) {
            this.savingsAccount.releaseFunds(this.amount);
        } else {
            this.savingsAccount.holdFunds(this.amount);
        }
    }

    public DepositAccountOnHoldTransactionType getTransactionType() {
        return DepositAccountOnHoldTransactionType.fromInt(this.transactionType);
    }

    public LocalDate getTransactionDate() {
        LocalDate transactionDate = null;
        if(this.transactionDate !=null){
            transactionDate = LocalDate.fromDateFields(this.transactionDate);
        }
        return transactionDate;
    }

}
