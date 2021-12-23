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
package org.apache.fineract.accounting.provisioning.domain;

import java.math.BigDecimal;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.provisioning.domain.ProvisioningCategory;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;

@Entity
@Table(name = "m_loanproduct_provisioning_entry")
public class LoanProductProvisioningEntry extends AbstractPersistableCustom {

    @ManyToOne(optional = false)
    @JoinColumn(name = "history_id", referencedColumnName = "id", nullable = false)
    private ProvisioningEntry entry;

    @Column(name = "criteria_id", nullable = false)
    private Long criteriaId;

    @ManyToOne
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;

    @Column(name = "currency_code", length = 3)
    private String currencyCode;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private LoanProduct loanProduct;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private ProvisioningCategory provisioningCategory;

    @Column(name = "overdue_in_days", nullable = false)
    private Long overdueInDays;

    @Column(name = "reseve_amount", nullable = false)
    private BigDecimal reservedAmount;

    @ManyToOne
    @JoinColumn(name = "liability_account", nullable = false)
    private GLAccount liabilityAccount;

    @ManyToOne
    @JoinColumn(name = "expense_account", nullable = false)
    private GLAccount expenseAccount;

    protected LoanProductProvisioningEntry() {}

    public LoanProductProvisioningEntry(final LoanProduct loanProduct, final Office office, final String currencyCode,
            final ProvisioningCategory provisioningCategory, final Long overdueInDays, final BigDecimal reservedAmount,
            final GLAccount liabilityAccount, final GLAccount expenseAccount, Long criteriaId) {
        this.loanProduct = loanProduct;
        this.office = office;
        this.currencyCode = currencyCode;
        this.provisioningCategory = provisioningCategory;
        this.overdueInDays = overdueInDays;
        this.reservedAmount = reservedAmount;
        this.liabilityAccount = liabilityAccount;
        this.expenseAccount = expenseAccount;
        this.criteriaId = criteriaId;
    }

    public void setProvisioningEntry(ProvisioningEntry provisioningEntry) {
        this.entry = provisioningEntry;
    }

    public BigDecimal getReservedAmount() {
        return this.reservedAmount;
    }

    public void addReservedAmount(BigDecimal value) {
        this.reservedAmount = this.reservedAmount.add(value);
    }

    public Office getOffice() {
        return this.office;
    }

    public GLAccount getLiabilityAccount() {
        return this.liabilityAccount;
    }

    public String getCurrencyCode() {
        return this.currencyCode;
    }

    public GLAccount getExpenseAccount() {
        return this.expenseAccount;
    }

    // TODO Note that this domain class does equals() & hashCode() on getId()
    // for @JoinColumn attributes, which not all other classes do...

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof LoanProductProvisioningEntry)) {
            return false;
        }
        LoanProductProvisioningEntry other = (LoanProductProvisioningEntry) obj;
        return Objects.equals(other.entry.getId(), this.entry.getId()) && Objects.equals(other.criteriaId, this.criteriaId)
                && Objects.equals(other.office.getId(), this.office.getId()) && Objects.equals(other.currencyCode, this.currencyCode)
                && Objects.equals(other.loanProduct.getId(), this.loanProduct.getId())
                && Objects.equals(other.provisioningCategory.getId(), this.provisioningCategory.getId())
                && Objects.equals(other.overdueInDays, this.overdueInDays) && Objects.equals(other.reservedAmount, this.reservedAmount)
                && Objects.equals(other.liabilityAccount.getId(), this.liabilityAccount.getId())
                && Objects.equals(other.expenseAccount.getId(), this.expenseAccount.getId());
    }

    @Override
    public int hashCode() {
        // NOT return Objects.hash(entry, criteriaId, office, currencyCode,
        // loanProduct, provisioningCategory, overdueInDays, reservedAmount,
        // liabilityAccount, expenseAccount);
        // to remain consistent with the implementation in equals(), also use
        // getId() here.
        return Objects.hash(entry.getId(), criteriaId, office.getId(), currencyCode, loanProduct.getId(), provisioningCategory.getId(),
                overdueInDays, reservedAmount, liabilityAccount.getId(), expenseAccount.getId());
    }

    public int partialHashCode() {
        // this is used to group together all the entries that have similar parameters (excluding the amount reserved)
        // rather than a check for if the objects are the same based on their values, this tells if they are similar
        return Objects.hash(entry.getId(), criteriaId, office.getId(), currencyCode, loanProduct.getId(), provisioningCategory.getId(),
                overdueInDays, liabilityAccount.getId(), expenseAccount.getId());
    }
}
