/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.provisioning.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.mifosplatform.accounting.glaccount.domain.GLAccount;
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.organisation.provisioning.domain.ProvisioningCategory;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProduct;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_loanproduct_provisioning_entry")
public class LoanProductProvisioningEntry extends AbstractPersistable<Long> {

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

    protected LoanProductProvisioningEntry() {
        
    }
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
        this.criteriaId = criteriaId ;
    }

    public void setProvisioningEntry(ProvisioningEntry provisioningEntry) {
        this.entry = provisioningEntry;
    }

    public BigDecimal getReservedAmount() {
        return this.reservedAmount ;
    }
    public void addReservedAmount(BigDecimal value) {
        this.reservedAmount = this.reservedAmount.add(value) ;
    }
    
    public Office getOffice() {
        return this.office ;
    }

    public GLAccount getLiabilityAccount() {
        return this.liabilityAccount ;
    }
    
    public String getCurrencyCode() {
        return this.currencyCode ;
    }
    
    public GLAccount getExpenseAccount() {
        return this.expenseAccount ;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!obj.getClass().equals(getClass())) return false;
        LoanProductProvisioningEntry entry = (LoanProductProvisioningEntry) obj;
        return entry.loanProduct.getId().equals(this.loanProduct.getId())
                && entry.provisioningCategory.getId().equals(this.provisioningCategory.getId())
                && entry.office.getId().equals(this.office.getId())
                && entry.getCurrencyCode().equals(this.getCurrencyCode());
    }
}
