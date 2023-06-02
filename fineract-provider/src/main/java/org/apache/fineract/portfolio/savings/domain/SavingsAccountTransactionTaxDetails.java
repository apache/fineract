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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.tax.domain.TaxComponent;

@Entity
@Table(name = "m_savings_account_transaction_tax_details")
public class SavingsAccountTransactionTaxDetails extends AbstractPersistableCustom {

    @ManyToOne
    @JoinColumn(name = "savings_transaction_id", nullable = false)
    private SavingsAccountTransaction savingsAccountTransaction;

    @ManyToOne
    @JoinColumn(name = "tax_component_id", nullable = false)
    private TaxComponent taxComponent;

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal amount;

    protected SavingsAccountTransactionTaxDetails() {}

    public SavingsAccountTransactionTaxDetails(final SavingsAccountTransaction savingsAccountTransaction, final TaxComponent taxComponent,
            final BigDecimal amount) {
        this.savingsAccountTransaction = savingsAccountTransaction;
        this.taxComponent = taxComponent;
        this.amount = amount;
    }

    public TaxComponent getTaxComponent() {
        return this.taxComponent;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void updateAmount(Money amount) {
        this.amount = amount.getAmount();
    }

    public SavingsAccountTransaction getSavingsAccountTransaction() {
        return savingsAccountTransaction;
    }

    public void setSavingsAccountTransaction(SavingsAccountTransaction savingsAccountTransaction) {
        this.savingsAccountTransaction = savingsAccountTransaction;
    }
}
