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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableCustom;
import org.apache.fineract.organisation.provisioning.constants.ProvisioningCriteriaConstants;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.useradministration.domain.AppUser;

@Entity
@Table(name = "m_provisioning_criteria", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "criteria_name" }, name = "criteria_name") })
public class ProvisioningCriteria extends AbstractAuditableCustom {

    @Column(name = "criteria_name", nullable = false)
    private String criteriaName;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "criteria", orphanRemoval = true, fetch = FetchType.EAGER)
    Set<ProvisioningCriteriaDefinition> provisioningCriteriaDefinition = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "criteria", orphanRemoval = true, fetch = FetchType.EAGER)
    Set<LoanProductProvisionCriteria> loanProductMapping = new HashSet<>();

    public String getCriteriaName() {
        return this.criteriaName;
    }

    public void setCriteriaName(String criteriaName) {
        this.criteriaName = criteriaName;
    }

    protected ProvisioningCriteria() {

    }

    public ProvisioningCriteria(String criteriaName, AppUser createdBy, LocalDateTime createdDate, AppUser lastModifiedBy,
            LocalDateTime lastModifiedDate) {
        this.criteriaName = criteriaName;
        setCreatedBy(createdBy.getId());
        setCreatedDate(createdDate);
        setLastModifiedBy(lastModifiedBy.getId());
        setLastModifiedDate(lastModifiedDate);
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
        if (command.isChangeInStringParameterNamed(ProvisioningCriteriaConstants.JSON_CRITERIANAME_PARAM, criteriaName)) {
            final String valueAsInput = command.stringValueOfParameterNamed(ProvisioningCriteriaConstants.JSON_CRITERIANAME_PARAM);
            actualChanges.put(ProvisioningCriteriaConstants.JSON_CRITERIANAME_PARAM, valueAsInput);
            this.criteriaName = valueAsInput;
        }

        Set<LoanProductProvisionCriteria> temp = new HashSet<>();
        Set<LoanProduct> productsTemp = new HashSet<>();

        for (LoanProductProvisionCriteria mapping : loanProductMapping) {
            if (!loanProducts.contains(mapping.getLoanProduct())) {
                temp.add(mapping);
            } else {
                productsTemp.add(mapping.getLoanProduct());
            }
        }
        loanProductMapping.removeAll(temp);

        for (LoanProduct loanProduct : loanProducts) {
            if (!productsTemp.contains(loanProduct)) {
                this.loanProductMapping.add(new LoanProductProvisionCriteria(this, loanProduct));
            }
        }

        actualChanges.put(ProvisioningCriteriaConstants.JSON_LOANPRODUCTS_PARAM, loanProductMapping);
        return actualChanges;
    }

    /**
     * Index the existing definitions by their natural key, {@code categoryId}, for direct lookup during an update.
     * <p>
     * The public UPDATE payload keys each definition by {@code categoryId} (one definition per provisioning category),
     * not by the surrogate {@code id}, which it never carries — matching on the always-null surrogate id is what threw
     * a {@link NullPointerException} (HTTP 500) on every update. Callers resolve the definition to mutate with a single
     * {@code get(categoryId)} instead of rescanning the whole set per incoming definition.
     */
    public Map<Long, ProvisioningCriteriaDefinition> getDefinitionsByCategoryId() {
        // (criteria_id, category_id) is not DB-unique, so a criteria could in principle hold two definitions for the
        // same category. Keep the first (the prior linear scan also matched the first and returned) rather than letting
        // Collectors.toMap throw IllegalStateException on a duplicate key — an update must not 500 on this edge case.
        return provisioningCriteriaDefinition.stream().collect(
                Collectors.toMap(ProvisioningCriteriaDefinition::getCategoryId, Function.identity(), (existing, duplicate) -> existing));
    }
}
