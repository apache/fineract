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
package org.apache.fineract.portfolio.loanaccount.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.portfolio.tax.domain.TaxComponent;

@Entity
@Table(name = "m_loan_charge_tax_details")
public class LoanChargeTaxDetails extends AbstractPersistableCustom<Long> {

    @ManyToOne
    @JoinColumn(name = "loan_charge_id", nullable = false)
    private LoanCharge loanCharge;

    @ManyToOne
    @JoinColumn(name = "tax_component_id", nullable = false)
    private TaxComponent taxComponent;

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal amount;

    @Column(name = "amount_paid", scale = 6, precision = 19)
    private BigDecimal amountPaid;

    @Column(name = "actual_amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal actualAmount;

    protected LoanChargeTaxDetails() {}

    public LoanChargeTaxDetails(final LoanCharge loanCharge, final TaxComponent taxComponent, final BigDecimal amount,
            final BigDecimal actualAmount) {
        this.loanCharge = loanCharge;
        this.taxComponent = taxComponent;
        this.amount = amount;
        this.amountPaid = BigDecimal.ZERO;
        this.actualAmount = actualAmount;

    }

    public LoanCharge getLoanCharge() {
        return this.loanCharge;
    }

    public TaxComponent getTaxComponent() {
        return this.taxComponent;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setAmount(final BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmountPaid() {
        return this.amountPaid;
    }

    public void setAmountPaid(final BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    public BigDecimal getActualTaxAmount() {
        return this.actualAmount;
    }

    public void setActualTaxAmount(final BigDecimal actualTaxAmount) {
        this.actualAmount = actualTaxAmount;
    }

}
