/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.provisioning.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.mifosplatform.accounting.glaccount.domain.GLAccount;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_provisioning_criteria_definition")
public class ProvisioningCriteriaDefinition extends AbstractPersistable<Long> {

    @ManyToOne(optional = false)
    @JoinColumn(name = "criteria_id", referencedColumnName = "id", nullable = false)
    private ProvisioningCriteria criteria;
    
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private ProvisioningCategory provisioningCategory;

    @Column(name = "min_age", nullable = false)
    private Long minimumAge;

    @Column(name = "max_age", nullable = false)
    private Long maximumAge;

    @Column(name = "provision_percentage", nullable = false)
    private BigDecimal provisioningPercentage;

    @ManyToOne
    @JoinColumn(name = "liability_account", nullable = false)
    private GLAccount liabilityAccount;

    @ManyToOne
    @JoinColumn(name = "expense_account", nullable = false)
    private GLAccount expenseAccount;

    protected ProvisioningCriteriaDefinition() {
        
    }
    
    private ProvisioningCriteriaDefinition(ProvisioningCriteria criteria, ProvisioningCategory provisioningCategory, Long minimumAge,
            Long maximumAge, BigDecimal provisioningPercentage, GLAccount liabilityAccount, GLAccount expenseAccount) {
        this.criteria = criteria;
        this.provisioningCategory = provisioningCategory;
        this.minimumAge = minimumAge;
        this.maximumAge = maximumAge;
        this.provisioningPercentage = provisioningPercentage;
        this.liabilityAccount = liabilityAccount;
        this.expenseAccount = expenseAccount;
    }

    public static ProvisioningCriteriaDefinition newPrivisioningCriteria(ProvisioningCriteria criteria,
            ProvisioningCategory provisioningCategory, Long minimumAge, Long maximumAge, BigDecimal provisioningPercentage,
            GLAccount liabilityAccount, GLAccount expenseAccount) {

        return new ProvisioningCriteriaDefinition(criteria, provisioningCategory, minimumAge, maximumAge, provisioningPercentage,
                liabilityAccount, expenseAccount);
    }
    
    public void update(Long minAge, Long maxAge, BigDecimal percentage, GLAccount lia, GLAccount exp) {
        this.minimumAge = minAge ;
        this.maximumAge = maxAge ;
        this.provisioningPercentage = percentage ;
        this.liabilityAccount = lia ;
        this.expenseAccount = exp ;
    }
}
