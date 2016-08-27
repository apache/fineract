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
package org.apache.fineract.organisation.provisioning.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.organisation.provisioning.domain.ProvisioningAmountType;
import org.apache.fineract.organisation.provisioning.service.ProvisioningEnumerations;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;

@SuppressWarnings("unused")
public class ProvisioningCriteriaData implements Comparable<ProvisioningCriteriaData>, Serializable {

    private final Long criteriaId;
    private final String criteriaName;
    private final String createdBy;
    private final Collection<LoanProductData> loanProducts;
    private Collection<LoanProductData> selectedLoanProducts ;
    private final Collection<ProvisioningCriteriaDefinitionData> definitions;
    private final Collection<GLAccountData> glAccounts;
    private final Collection <EnumOptionData> provisioningAmountTypeOptions;
    private final EnumOptionData provisioningAmountType;
    

    private ProvisioningCriteriaData(final Long criteriaId, final String criteriaName, final Collection<LoanProductData> loanProducts,
            Collection<ProvisioningCriteriaDefinitionData> definitions, Collection<GLAccountData> glAccounts, final String createdBy,
            final EnumOptionData provisioningAmountType) {
        this.criteriaId = criteriaId;
        this.criteriaName = criteriaName;
        this.loanProducts = loanProducts;
        this.definitions = definitions;
        this.glAccounts = glAccounts;
        this.createdBy = createdBy;
        this.provisioningAmountTypeOptions = null;
        this.provisioningAmountType = provisioningAmountType;
    }
    
    private ProvisioningCriteriaData(final Long criteriaId, final String criteriaName, final Collection<LoanProductData> loanProducts,
            Collection<ProvisioningCriteriaDefinitionData> definitions, Collection<GLAccountData> glAccounts, final String createdBy) {
        this.criteriaId = criteriaId;
        this.criteriaName = criteriaName;
        this.loanProducts = loanProducts;
        this.definitions = definitions;
        this.glAccounts = glAccounts;
        this.createdBy = createdBy;
        this.provisioningAmountTypeOptions = null;
        this.provisioningAmountType = null;
    }
    
    private ProvisioningCriteriaData(final Long criteriaId, final String criteriaName, final Collection<LoanProductData> loanProducts,
            Collection<ProvisioningCriteriaDefinitionData> definitions, Collection<GLAccountData> glAccounts, final String createdBy,
            final Collection <EnumOptionData> provisioningAmountTypeOptions) {
        this.criteriaId = criteriaId;
        this.criteriaName = criteriaName;
        this.loanProducts = loanProducts;
        this.definitions = definitions;
        this.glAccounts = glAccounts;
        this.createdBy = createdBy;
        this.provisioningAmountTypeOptions = provisioningAmountTypeOptions;
        this.provisioningAmountType = null;
    }


    private ProvisioningCriteriaData(ProvisioningCriteriaData data, final Collection<LoanProductData> loanProducts,
            Collection<GLAccountData> glAccounts, final Collection<EnumOptionData> provisioningAmountTypeOptions) {
        this.criteriaId = data.criteriaId;
        this.criteriaName = data.criteriaName;
        this.selectedLoanProducts = data.loanProducts ;
        this.loanProducts = loanProducts;
        this.loanProducts.removeAll(selectedLoanProducts) ;
        this.definitions = data.definitions;
        this.glAccounts = glAccounts;
        this.createdBy = data.createdBy;
        this.provisioningAmountTypeOptions = provisioningAmountTypeOptions;
        this.provisioningAmountType = data.provisioningAmountType;
    }
    
    public ProvisioningCriteriaData( final String criteriaName,final EnumOptionData provisioningAmountType) {
        this.criteriaId = null;
        this.criteriaName = criteriaName;
        this.selectedLoanProducts = null ;
        this.loanProducts = null;
        this.definitions = null;
        this.glAccounts = null;
        this.createdBy = null;
        this.provisioningAmountTypeOptions = null;
        this.provisioningAmountType = provisioningAmountType;
    }

	public static ProvisioningCriteriaData toLookup(final Long criteriaId, ProvisioningCriteriaData data, 
			final Collection<LoanProductData> loanProducts, final List<ProvisioningCriteriaDefinitionData> definitions) {
		Collection<GLAccountData> glAccounts = null;
		String createdBy = null;
		return new ProvisioningCriteriaData(criteriaId, data.criteriaName, loanProducts, definitions, glAccounts, createdBy, data.provisioningAmountType);
	}

    public static ProvisioningCriteriaData toLookup(final Long criteriaId, final String criteriaName, String createdBy) {
        Collection<GLAccountData> glAccounts = null;
        Collection<LoanProductData> loanProducts = null;
        List<ProvisioningCriteriaDefinitionData> definitions = null;
        return new ProvisioningCriteriaData(criteriaId, criteriaName, loanProducts, definitions, glAccounts, createdBy);
    }

	public static ProvisioningCriteriaData toTemplate(final Collection<ProvisioningCriteriaDefinitionData> definitions,
			final Collection<LoanProductData> loanProducts, final Collection<GLAccountData> glAccounts,
			final Collection<EnumOptionData> provisioningAmountTypeOptions) {
        Long criteriaId = null;
        String criteriaName = null;
        String createdBy = null;
        EnumOptionData provisioningAmountType = null;
        return new ProvisioningCriteriaData(criteriaId, criteriaName, loanProducts, definitions, glAccounts, createdBy,  provisioningAmountTypeOptions);
    }
    
    public static ProvisioningCriteriaData toTemplate(final ProvisioningCriteriaData data, final Collection<ProvisioningCriteriaDefinitionData> definitions,
            final Collection<LoanProductData> loanProducts, final Collection<GLAccountData> glAccounts, final Collection<EnumOptionData> provisioningAmountTypeOptions) {
        return new ProvisioningCriteriaData(data, loanProducts, glAccounts, provisioningAmountTypeOptions);
    }

    @Override
    public int compareTo(ProvisioningCriteriaData obj) {
        if (obj == null ) { return -1; }
        return obj.criteriaId.compareTo(this.criteriaId);
    }
}
