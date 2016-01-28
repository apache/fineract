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

    private ProvisioningCriteriaData(final Long criteriaId, final String criteriaName, final Collection<LoanProductData> loanProducts,
            Collection<ProvisioningCriteriaDefinitionData> definitions, Collection<GLAccountData> glAccounts, final String createdBy) {
        this.criteriaId = criteriaId;
        this.criteriaName = criteriaName;
        this.loanProducts = loanProducts;
        this.definitions = definitions;
        this.glAccounts = glAccounts;
        this.createdBy = createdBy;
    }

    private ProvisioningCriteriaData(ProvisioningCriteriaData data, final Collection<LoanProductData> loanProducts,
            Collection<GLAccountData> glAccounts) {
        this.criteriaId = data.criteriaId;
        this.criteriaName = data.criteriaName;
        this.selectedLoanProducts = data.loanProducts ;
        this.loanProducts = loanProducts;
        this.loanProducts.removeAll(selectedLoanProducts) ;
        this.definitions = data.definitions;
        this.glAccounts = glAccounts;
        this.createdBy = data.createdBy;
    }
    public static ProvisioningCriteriaData toLookup(final Long criteriaId, final String criteriaName, final Collection<LoanProductData> loanProducts,
            final List<ProvisioningCriteriaDefinitionData> definitions) {
        Collection<GLAccountData> glAccounts = null;
        String createdBy = null;
        return new ProvisioningCriteriaData(criteriaId, criteriaName, loanProducts, definitions, glAccounts, createdBy);
    }

    public static ProvisioningCriteriaData toLookup(final Long criteriaId, final String criteriaName, String createdBy) {
        Collection<GLAccountData> glAccounts = null;
        Collection<LoanProductData> loanProducts = null;
        List<ProvisioningCriteriaDefinitionData> definitions = null;
        return new ProvisioningCriteriaData(criteriaId, criteriaName, loanProducts, definitions, glAccounts, createdBy);
    }

    public static ProvisioningCriteriaData toTemplate(final Collection<ProvisioningCriteriaDefinitionData> definitions,
            final Collection<LoanProductData> loanProducts, final Collection<GLAccountData> glAccounts) {
        Long criteriaId = null;
        String criteriaName = null;
        String createdBy = null;
        return new ProvisioningCriteriaData(criteriaId, criteriaName, loanProducts, definitions, glAccounts, createdBy);
    }
    
    public static ProvisioningCriteriaData toTemplate(final ProvisioningCriteriaData data, final Collection<ProvisioningCriteriaDefinitionData> definitions,
            final Collection<LoanProductData> loanProducts, final Collection<GLAccountData> glAccounts) {
        return new ProvisioningCriteriaData(data, loanProducts, glAccounts);
    }

    @Override
    public int compareTo(ProvisioningCriteriaData obj) {
        if (obj == null ) { return -1; }
        return obj.criteriaId.compareTo(this.criteriaId);
    }
}
