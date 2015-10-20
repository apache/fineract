/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.provisioning.domain;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.joda.time.DateTime;
import org.mifosplatform.accounting.glaccount.domain.GLAccount;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.mifosplatform.organisation.provisioning.constants.ProvisioningCriteriaConstants;
import org.mifosplatform.organisation.provisioning.data.ProvisioningCriteriaDefinitionData;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProduct;
import org.mifosplatform.useradministration.domain.AppUser;

@Entity
@Table(name = "m_provisioning_criteria", uniqueConstraints = { @UniqueConstraint(columnNames = { "criteria_name" }, name = "criteria_name") })
public class ProvisioningCriteria extends AbstractAuditableCustom<AppUser, Long> {

    @Column(name = "criteria_name", nullable = false)
    private String criteriaName;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "criteria", orphanRemoval = true)
    Set<ProvisioningCriteriaDefinition> provisioningCriteriaDefinition = new HashSet<>();

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "criteria", orphanRemoval = true)
    Set<LoanProductProvisionCriteria> loanProductMapping = new HashSet<>();

    public String getCriteriaName() {
        return this.criteriaName;
    }

    public void setCriteriaName(String criteriaName) {
        this.criteriaName = criteriaName;
    }

    protected ProvisioningCriteria() {
        
    }
    
    public ProvisioningCriteria(String criteriaName, AppUser createdBy, DateTime createdDate, AppUser lastModifiedBy, DateTime lastModifiedDate) {
        this.criteriaName = criteriaName;
        setCreatedBy(createdBy) ;
        setCreatedDate(createdDate) ;
        setLastModifiedBy(lastModifiedBy) ;
        setLastModifiedDate(lastModifiedDate) ;
    }

    public void setProvisioningCriteriaDefinitions(Set<ProvisioningCriteriaDefinition> provisioningCriteriaDefinition) {
        this.provisioningCriteriaDefinition.clear();
        this.provisioningCriteriaDefinition.addAll(provisioningCriteriaDefinition);
    }

    public void setLoanProductProvisioningCriteria(Set<LoanProductProvisionCriteria> loanProductMapping) {
        this.loanProductMapping.clear();
        this.loanProductMapping.addAll(loanProductMapping);
    }
    
    public Map<String, Object> update(JsonCommand command, List<LoanProduct> loanProducts) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(7);
        if(command.isChangeInStringParameterNamed(ProvisioningCriteriaConstants.JSON_CRITERIANAME_PARAM, criteriaName)) {
            final String valueAsInput = command.stringValueOfParameterNamed(ProvisioningCriteriaConstants.JSON_CRITERIANAME_PARAM);
            actualChanges.put(ProvisioningCriteriaConstants.JSON_CRITERIANAME_PARAM, valueAsInput);
            this.criteriaName = valueAsInput ;
        }

        Set<LoanProductProvisionCriteria> temp = new HashSet<>() ;
        Set<LoanProduct> productsTemp = new HashSet<>() ;
        
        for(LoanProductProvisionCriteria mapping: loanProductMapping) {
            if(!loanProducts.contains(mapping.getLoanProduct())) {
                temp.add(mapping) ;
            }else {
                productsTemp.add(mapping.getLoanProduct()) ;
            }
        }
        loanProductMapping.removeAll(temp) ;
        
        for(LoanProduct loanProduct: loanProducts) {
            if(!productsTemp.contains(loanProduct)) {
                this.loanProductMapping.add( new LoanProductProvisionCriteria(this, loanProduct)) ;     
            }
        }
        
        actualChanges.put(ProvisioningCriteriaConstants.JSON_LOANPRODUCTS_PARAM, loanProductMapping);
        return actualChanges ;
    }
    
    public void update(ProvisioningCriteriaDefinitionData data, GLAccount liability, GLAccount expense) {
        for(ProvisioningCriteriaDefinition def: provisioningCriteriaDefinition) {
            if(data.getId() == def.getId()) {
                def.update(data.getMinAge(), data.getMaxAge(), data.getProvisioningPercentage(), liability, expense) ;
                break ;
            }
        }
    }
}
