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

import static org.apache.fineract.infrastructure.core.service.DateUtils.getSystemZoneId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.savings.DepositAccountOnHoldTransactionType;

@Entity
@Table(name = "m_deposit_account_on_hold_transaction")
public class DepositAccountOnHoldTransaction extends AbstractAuditableWithUTCDateTimeCustom {

    @ManyToOne
    @JoinColumn(name = "savings_account_id", nullable = true)
    private SavingsAccount savingsAccount;

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal amount;

    @Column(name = "transaction_type_enum", nullable = false)
    private Integer transactionType;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "is_reversed", nullable = false)
    private boolean reversed;

    @Deprecated
    @Column(name = "created_date", nullable = true)
    private LocalDateTime createdDateToRemove;

    protected DepositAccountOnHoldTransaction() {}

    private DepositAccountOnHoldTransaction(final SavingsAccount savingsAccount, final BigDecimal amount,
            final DepositAccountOnHoldTransactionType transactionType, final LocalDate transactionDate, final boolean reversed) {
        this.savingsAccount = savingsAccount;
        this.amount = amount;
        this.transactionType = transactionType.getValue();
        this.transactionDate = transactionDate;
        this.createdDateToRemove = null; // #audit backward compatibility deprecated
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

    public SavingsAccount getSavingsAccount() {
        return savingsAccount;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public Money getAmount(final MonetaryCurrency currency) {
        return Money.of(currency, this.amount);
    }

    public DepositAccountOnHoldTransactionType getTransactionType() {
        return DepositAccountOnHoldTransactionType.fromInt(this.transactionType);
    }

    public LocalDate getTransactionDate() {
        return this.transactionDate;
    }

    public MonetaryCurrency getCurrency() {
        return getSavingsAccount().getCurrency();
    }

    public void reverseTransaction() {
        this.reversed = true;
        if (this.getTransactionType().isHold()) {
            this.savingsAccount.releaseFunds(this.amount);
        } else {
            this.savingsAccount.holdFunds(this.amount);
        }
    }

    @Override
    public Optional<OffsetDateTime> getCreatedDate() {
        // #audit backward compatibility keep system datetime
        return Optional.ofNullable(super.getCreatedDate()
                .orElse(createdDateToRemove == null ? null : createdDateToRemove.atZone(getSystemZoneId()).toOffsetDateTime()));
    }
}
