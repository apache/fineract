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
package org.apache.fineract.organisation.provisioning.domain;

import java.math.BigDecimal;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_provisioning_criteria_definition")
public class ProvisioningCriteriaDefinition extends AbstractPersistableCustom<Long> {

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
    
    
    public boolean isOverlapping(ProvisioningCriteriaDefinition def) {
        return this.minimumAge <= def.maximumAge && def.minimumAge <= this.maximumAge;
    }
}
