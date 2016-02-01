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
package org.apache.fineract.organisation.provisioning.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepository;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.provisioning.constants.ProvisioningCriteriaConstants;
import org.apache.fineract.organisation.provisioning.domain.LoanProductProvisionCriteria;
import org.apache.fineract.organisation.provisioning.domain.ProvisioningCategory;
import org.apache.fineract.organisation.provisioning.domain.ProvisioningCategoryRepository;
import org.apache.fineract.organisation.provisioning.domain.ProvisioningCriteria;
import org.apache.fineract.organisation.provisioning.domain.ProvisioningCriteriaDefinition;
import org.apache.fineract.organisation.provisioning.exception.ProvisioningCriteriaOverlappingDefinitionException;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.apache.fineract.useradministration.domain.AppUser;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Service
public class ProvisioningCriteriaAssembler {

    private final FromJsonHelper fromApiJsonHelper;
    private final ProvisioningCategoryRepository provisioningCategoryRepository;
    private final LoanProductRepository loanProductRepository;
    private final GLAccountRepository glAccountRepository;
    private final PlatformSecurityContext platformSecurityContext;

    @Autowired
    public ProvisioningCriteriaAssembler(final FromJsonHelper fromApiJsonHelper,
            final ProvisioningCategoryRepository provisioningCategoryRepository, final LoanProductRepository loanProductRepository,
            final GLAccountRepository glAccountRepository, final PlatformSecurityContext platformSecurityContext) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.provisioningCategoryRepository = provisioningCategoryRepository;
        this.loanProductRepository = loanProductRepository;
        this.glAccountRepository = glAccountRepository;
        this.platformSecurityContext = platformSecurityContext;
    }

    public List<LoanProduct> parseLoanProducts(final JsonElement jsonElement) {
        List<LoanProduct> loanProducts = new ArrayList<>();
        if (fromApiJsonHelper.parameterExists(ProvisioningCriteriaConstants.JSON_LOANPRODUCTS_PARAM, jsonElement)) {
            JsonArray jsonloanProducts = this.fromApiJsonHelper.extractJsonArrayNamed(ProvisioningCriteriaConstants.JSON_LOANPRODUCTS_PARAM,
                    jsonElement);
            for (JsonElement element : jsonloanProducts) {
                Long productId = this.fromApiJsonHelper.extractLongNamed("id", element.getAsJsonObject());
                loanProducts.add(loanProductRepository.findOne(productId));
            }
        } else {
            loanProducts = loanProductRepository.findAll();
        }
        return loanProducts ;
    }
    
    private void validateRange(Set<ProvisioningCriteriaDefinition> criteriaDefinitions) {
        List<ProvisioningCriteriaDefinition> def = new ArrayList<>() ;
        def.addAll(criteriaDefinitions) ;
        
        for (int i = 0; i < def.size(); i++) {
            for (int j = i + 1; j < def.size(); j++) {
                if (def.get(i).isOverlapping(def.get(j))) {
                    throw new ProvisioningCriteriaOverlappingDefinitionException() ;
                }
            }
        }
    }
    
    public ProvisioningCriteria fromParsedJson(final JsonElement jsonElement) {
        ProvisioningCriteria provisioningCriteria = createCriteria(jsonElement);
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(jsonElement.getAsJsonObject());
        List<LoanProduct> loanProducts = parseLoanProducts(jsonElement) ;
        
        Set<ProvisioningCriteriaDefinition> criteriaDefinitions = new HashSet<>();
        JsonArray jsonProvisioningCriteria = this.fromApiJsonHelper.extractJsonArrayNamed(
                ProvisioningCriteriaConstants.JSON_PROVISIONING_DEFINITIONS_PARAM, jsonElement);
        for (JsonElement element : jsonProvisioningCriteria) {
            JsonObject jsonObject = element.getAsJsonObject();
            ProvisioningCriteriaDefinition provisioningCriteriaData = createProvisioningCriteriaDefinitions(jsonObject, locale,
                    provisioningCriteria);
            criteriaDefinitions.add(provisioningCriteriaData);
        }
        validateRange(criteriaDefinitions) ;
        Set<LoanProductProvisionCriteria> mapping = new HashSet<>();
        for (LoanProduct loanProduct : loanProducts) {
            mapping.add(new LoanProductProvisionCriteria(provisioningCriteria, loanProduct));
        }
        provisioningCriteria.setProvisioningCriteriaDefinitions(criteriaDefinitions);
        provisioningCriteria.setLoanProductProvisioningCriteria(mapping);
        return provisioningCriteria;
    }

    private ProvisioningCriteria createCriteria(final JsonElement jsonElement) {
        final String criteriaName = this.fromApiJsonHelper.extractStringNamed(ProvisioningCriteriaConstants.JSON_CRITERIANAME_PARAM, jsonElement);
        AppUser modifiedBy = null;
        DateTime modifiedOn = null;
        ProvisioningCriteria criteria = new ProvisioningCriteria(criteriaName, platformSecurityContext.authenticatedUser(), new DateTime(),
                modifiedBy, modifiedOn);
        return criteria;
    }

    private ProvisioningCriteriaDefinition createProvisioningCriteriaDefinitions(JsonObject jsonObject, Locale locale,
            ProvisioningCriteria criteria) {
        Long categoryId = this.fromApiJsonHelper.extractLongNamed(ProvisioningCriteriaConstants.JSON_CATEOGRYID_PARAM, jsonObject);
        Long minimumAge = this.fromApiJsonHelper.extractLongNamed(ProvisioningCriteriaConstants.JSON_MINIMUM_AGE_PARAM, jsonObject);
        Long maximumAge = this.fromApiJsonHelper.extractLongNamed(ProvisioningCriteriaConstants.JSON_MAXIMUM_AGE_PARAM, jsonObject);
        BigDecimal provisioningpercentage = this.fromApiJsonHelper.extractBigDecimalNamed(ProvisioningCriteriaConstants.JSON_PROVISIONING_PERCENTAGE_PARAM,
                jsonObject, locale);
        Long liabilityAccountId = this.fromApiJsonHelper.extractLongNamed(ProvisioningCriteriaConstants.JSON_LIABILITY_ACCOUNT_PARAM, jsonObject);
        Long expenseAccountId = this.fromApiJsonHelper.extractLongNamed(ProvisioningCriteriaConstants.JSON_EXPENSE_ACCOUNT_PARAM, jsonObject);

        ProvisioningCategory provisioningCategory = provisioningCategoryRepository.findOne(categoryId);
        GLAccount liabilityAccount = glAccountRepository.findOne(liabilityAccountId);
        GLAccount expenseAccount = glAccountRepository.findOne(expenseAccountId);
        return ProvisioningCriteriaDefinition.newPrivisioningCriteria(criteria, provisioningCategory, minimumAge, maximumAge,
                provisioningpercentage, liabilityAccount, expenseAccount);
    }
}
