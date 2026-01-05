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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableCustom;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.tax.api.TaxApiConstants;

@Entity
@Table(name = "m_tax_group_mappings")
public class TaxGroupMappings extends AbstractAuditableCustom {

    @ManyToOne
    @JoinColumn(name = "tax_group_id", nullable = false)
    private TaxGroup taxGroup;

    @ManyToOne
    @JoinColumn(name = "tax_component_id", nullable = false)
    private TaxComponent taxComponent;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = true)
    private LocalDate endDate;

    protected TaxGroupMappings() {}

    private TaxGroupMappings(final TaxComponent taxComponent, final LocalDate startDate, final LocalDate endDate) {
        this.taxComponent = taxComponent;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public static TaxGroupMappings createTaxGroupMappings(final TaxComponent taxComponent, final LocalDate startDate) {
        final LocalDate endDate = null;
        return new TaxGroupMappings(taxComponent, startDate, endDate);

    }

    public static TaxGroupMappings createTaxGroupMappings(final Long id, final TaxComponent taxComponent, final LocalDate endDate) {
        final LocalDate startDate = null;
        TaxGroupMappings groupMappings = new TaxGroupMappings(taxComponent, startDate, endDate);
        groupMappings.setId(id);
        return groupMappings;

    }

    public void update(final LocalDate endDate, final List<Map<String, Object>> changes) {
        if (endDate != null && this.endDate == null) {
            this.endDate = endDate;
            Map<String, Object> map = new HashMap<>(2);
            map.put(TaxApiConstants.endDateParamName, endDate);
            map.put(TaxApiConstants.taxComponentIdParamName, this.getTaxComponent().getId());
            changes.add(map);
        }
    }

    public boolean occursOnDayFromAndUpToAndIncluding(final LocalDate target) {
        return DateUtils.isAfter(target, startDate()) && (endDate == null || !DateUtils.isAfter(target, endDate()));
    }

    public TaxComponent getTaxComponent() {
        return this.taxComponent;
    }

    public LocalDate getEndDate() {
        return this.endDate;
    }

    public LocalDate startDate() {
        return this.startDate;
    }

    public LocalDate endDate() {
        return this.endDate;
    }

    public TaxGroup getTaxGroup() {
        return taxGroup;
    }

    public void setTaxGroup(TaxGroup taxGroup) {
        this.taxGroup = taxGroup;
    }
}
