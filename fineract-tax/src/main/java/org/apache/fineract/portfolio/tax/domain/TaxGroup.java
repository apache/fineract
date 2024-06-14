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
package org.apache.fineract.portfolio.tax.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableCustom;
import org.apache.fineract.portfolio.tax.api.TaxApiConstants;
import org.apache.fineract.portfolio.tax.exception.TaxMappingNotFoundException;

@Entity
@Table(name = "m_tax_group")
public class TaxGroup extends AbstractAuditableCustom {

    @Column(name = "name", length = 100)
    private String name;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER, mappedBy = "taxGroup")
    private Set<TaxGroupMappings> taxGroupMappings = new HashSet<>();

    protected TaxGroup() {

    }

    private TaxGroup(final String name, final Set<TaxGroupMappings> taxGroupMappings) {
        this.name = name;
        this.taxGroupMappings = taxGroupMappings;
        taxGroupMappings.forEach(m -> m.setTaxGroup(this));
    }

    public static TaxGroup createTaxGroup(final String name, final Set<TaxGroupMappings> taxGroupMappings) {
        return new TaxGroup(name, taxGroupMappings);
    }

    public Map<String, Object> update(final JsonCommand command, final Set<TaxGroupMappings> taxGroupMappings) {
        final Map<String, Object> changes = new HashMap<>();

        if (command.isChangeInStringParameterNamed(TaxApiConstants.nameParamName, this.name)) {
            final String newValue = command.stringValueOfParameterNamed(TaxApiConstants.nameParamName);
            changes.put(TaxApiConstants.nameParamName, newValue);
            this.name = StringUtils.defaultIfEmpty(newValue, null);
        }

        List<Long> taxComponentList = new ArrayList<>();
        final List<Map<String, Object>> modifications = new ArrayList<>();

        for (TaxGroupMappings groupMappings : taxGroupMappings) {
            TaxGroupMappings mappings = findOneBy(groupMappings);
            if (mappings == null) {
                this.taxGroupMappings.add(groupMappings);
                taxComponentList.add(groupMappings.getTaxComponent().getId());
            } else {
                mappings.update(groupMappings.getEndDate(), modifications);
            }
        }

        if (!taxComponentList.isEmpty()) {
            changes.put("addComponents", taxComponentList);
        }
        if (!modifications.isEmpty()) {
            changes.put("modifiedComponents", modifications);
        }

        return changes;
    }

    public TaxGroupMappings findOneBy(final TaxGroupMappings groupMapping) {
        if (groupMapping.getId() != null) {
            for (TaxGroupMappings groupMappings : this.taxGroupMappings) {
                if (groupMappings.getId().equals(groupMapping.getId())) {
                    return groupMappings;
                }
            }
            throw new TaxMappingNotFoundException(groupMapping.getId());
        }
        return null;
    }

    public Set<TaxGroupMappings> getTaxGroupMappings() {
        return this.taxGroupMappings;
    }

    public String getName() {
        return this.name;
    }

}
