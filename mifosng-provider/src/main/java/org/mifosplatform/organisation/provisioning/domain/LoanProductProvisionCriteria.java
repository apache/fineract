/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.provisioning.domain;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.mifosplatform.portfolio.loanproduct.domain.LoanProduct;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_loanproduct_provisioning_mapping", uniqueConstraints = { @UniqueConstraint(columnNames = { "product_id" }, name = "product_id") })
public class LoanProductProvisionCriteria extends AbstractPersistable<Long> {

    @ManyToOne(optional = false)
    @JoinColumn(name = "criteria_id", referencedColumnName = "id", nullable = false)
    private ProvisioningCriteria criteria;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", referencedColumnName = "id", nullable = false)
    private LoanProduct loanProduct;
    
    protected LoanProductProvisionCriteria() {
        
    }
    
    public LoanProductProvisionCriteria(ProvisioningCriteria criteria, LoanProduct loanProduct) {
        this.criteria = criteria ;
        this.loanProduct = loanProduct ;
    }

    public LoanProduct getLoanProduct() {
        return this.loanProduct ;
    }
}
